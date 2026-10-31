package moe.shizuku.manager.ui.compose

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import org.intellij.lang.annotations.Language
import kotlin.math.abs
import kotlin.math.min

// Directional scroll motion blur.
//
// Technique adapted from sameerasw/essentials (MIT License, Copyright (c) 2026
// sameerasw.com), file ScrollMotionBlurModifier.kt: instead of a static uniform
// blur overlay, scroll velocity is sampled per frame, smoothed and decayed,
// then fed into a 10-sample AGSL directional blur shader (API 33+). The blur
// follows the scroll direction (horizontal for tab swipes, vertical for
// up/down list scrolls) and scales with fling speed. Below API 33 a
// velocity-driven uniform blur is used as a fallback.
//
// PERF: the per-frame velocity sampler now runs only while a scroll is actually
// in progress. It used to spin a `withFrameNanos` loop for the lifetime of the
// composition, burning CPU on every frame even when the list was idle - that was
// the main source of UI lag.

@Language("AGSL")
private const val DIRECTIONAL_BLUR_AGSL = """
    uniform shader composable;
    uniform float2 resolution;
    uniform float scrollVelocity;
    uniform float isHorizontal;
    uniform float blurScale;

    half4 main(float2 fragCoord) {
        const int SAMPLES = 10;
        half4 color = half4(0.0);
        float totalWeight = 0.0;

        // Scale velocity to pixel blur magnitude with max clamping
        float blurMagnitude = clamp(scrollVelocity * 22.0 * blurScale, -60.0, 60.0);

        for (int i = 0; i < SAMPLES; i++) {
            float offset = (float(i) / float(SAMPLES - 1) - 0.5) * blurMagnitude;

            float2 sampleCoord;
            if (isHorizontal > 0.5) {
                float clampedX = clamp(fragCoord.x + offset, 0.0, resolution.x);
                sampleCoord = float2(clampedX, fragCoord.y);
            } else {
                float clampedY = clamp(fragCoord.y + offset, 0.0, resolution.y);
                sampleCoord = float2(fragCoord.x, clampedY);
            }

            // Linear decay weight for trailing effect
            float weight = 1.0 - abs(offset / (abs(blurMagnitude) + 0.001)) * 0.5;

            color += composable.eval(sampleCoord) * weight;
            totalWeight += weight;
        }

        return color / totalWeight;
    }
"""

internal fun motionBlurScale(intensity: Int, strength: Int): Float {
    val base = intensity.coerceIn(0, 100) / 100f
    if (base <= 0.001f) return 0f
    val sensitivity = 0.5f + strength.coerceIn(0, 100) / 100f
    return base * sensitivity * 2f
}

/**
 * Tracks scroll velocity of a [LazyListState] only while a scroll is in progress.
 *
 * The frame sampler is started when scrolling begins and stopped (with a decay to
 * zero) when scrolling ends, instead of running `withFrameNanos` for the whole
 * composition lifetime.
 */
@Composable
private fun rememberLazyVelocity(listState: LazyListState): Animatable<Float, *> {
    val animatedVelocity = remember { Animatable(0f) }
    val scope: CoroutineScope = rememberCoroutineScope()

    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            animatedVelocity.animateTo(0f, animationSpec = tween(60))
            return@LaunchedEffect
        }
        var prevIndex = listState.firstVisibleItemIndex
        var prevOffset = listState.firstVisibleItemScrollOffset
        var prevTimeNanos = System.nanoTime()
        scope.launch {
            while (isActive && listState.isScrollInProgress) {
                val frameStart = System.nanoTime()
                delay(16)
                val frameEnd = System.nanoTime()
                if (!listState.isScrollInProgress) break
                val currentIndex = listState.firstVisibleItemIndex
                val currentOffset = listState.firstVisibleItemScrollOffset
                val dtMs = (frameEnd - prevTimeNanos) / 1_000_000.0f
                if (dtMs in 1f..100f) {
                    val totalDelta =
                        ((currentIndex - prevIndex) * 80f) + (currentOffset - prevOffset)
                    val target = (totalDelta / dtMs).coerceIn(-3f, 3f)
                    val next = if (abs(totalDelta) > 0.1f) {
                        animatedVelocity.value * 0.35f + target * 0.65f
                    } else {
                        val decayed = animatedVelocity.value * 0.45f
                        if (abs(decayed) < 0.01f) 0f else decayed
                    }
                    animatedVelocity.snapTo(next)
                }
                prevIndex = currentIndex
                prevOffset = currentOffset
                prevTimeNanos = frameEnd
            }
            animatedVelocity.animateTo(0f, animationSpec = tween(60))
        }
    }
    return animatedVelocity
}

/**
 * Tracks pager velocity only while a page swipe is in progress.
 */
@Composable
private fun rememberPagerVelocity(pagerState: PagerState): Animatable<Float, *> {
    val animatedVelocity = remember { Animatable(0f) }
    val scope: CoroutineScope = rememberCoroutineScope()

    LaunchedEffect(pagerState.isScrollInProgress) {
        if (!pagerState.isScrollInProgress) {
            animatedVelocity.animateTo(0f, animationSpec = tween(60))
            return@LaunchedEffect
        }
        var prevPage = pagerState.currentPage
        var prevFraction = pagerState.currentPageOffsetFraction
        var prevTimeNanos = System.nanoTime()
        scope.launch {
            while (isActive && pagerState.isScrollInProgress) {
                val frameStart = System.nanoTime()
                delay(16)
                val frameEnd = System.nanoTime()
                if (!pagerState.isScrollInProgress) break
                val currentPage = pagerState.currentPage
                val currentFraction = pagerState.currentPageOffsetFraction
                val dtMs = (frameEnd - prevTimeNanos) / 1_000_000.0f
                if (dtMs in 1f..100f) {
                    val delta = ((currentPage + currentFraction) - (prevPage + prevFraction)) * 400f
                    val target = (delta / dtMs).coerceIn(-3f, 3f)
                    val next = if (abs(delta) > 0.1f) {
                        animatedVelocity.value * 0.35f + target * 0.65f
                    } else {
                        val decayed = animatedVelocity.value * 0.45f
                        if (abs(decayed) < 0.01f) 0f else decayed
                    }
                    animatedVelocity.snapTo(next)
                }
                prevPage = currentPage
                prevFraction = currentFraction
                prevTimeNanos = frameEnd
            }
            animatedVelocity.animateTo(0f, animationSpec = tween(60))
        }
    }
    return animatedVelocity
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun directionalBlurLayer(
    velocity: Animatable<Float, *>,
    isHorizontal: Boolean,
    blurScale: Float,
): Modifier {
    if (blurScale <= 0.001f) return Modifier
    val shader = remember { RuntimeShader(DIRECTIONAL_BLUR_AGSL) }
    return Modifier.graphicsLayer {
        val vel = velocity.value
        if (abs(vel) > 0.05f) {
            shader.setFloatUniform("resolution", size.width, size.height)
            shader.setFloatUniform("scrollVelocity", vel)
            shader.setFloatUniform("isHorizontal", if (isHorizontal) 1.0f else 0.0f)
            shader.setFloatUniform("blurScale", blurScale)
            renderEffect = RenderEffect
                .createRuntimeShaderEffect(shader, "composable")
                .asComposeRenderEffect()
        } else {
            renderEffect = null
        }
    }
}

private fun Modifier.velocityFallbackBlur(
    velocity: Animatable<Float, *>,
    intensity: Int,
    strength: Int,
): Modifier = composed {
    val vel = velocity.value
    val maxRadius = (intensity.coerceIn(0, 100) / 100f) * 12f *
        (0.5f + strength.coerceIn(0, 100) / 100f)
    val radius = (min(abs(vel) / 3f, 1f) * maxRadius).dp
    if (radius > 0.01.dp) this.blur(radius) else this
}

/**
 * Directional motion blur driven by a [LazyListState] (vertical up/down
 * scrolling by default, horizontal when [isHorizontal] is true).
 */
fun Modifier.scrollMotionBlur(
    listState: LazyListState,
    enabled: Boolean,
    intensity: Int,
    strength: Int,
    isHorizontal: Boolean = false,
): Modifier = composed {
    if (!enabled) return@composed this
    val scale = motionBlurScale(intensity, strength)
    if (scale <= 0.001f) return@composed this
    val velocity = rememberLazyVelocity(listState)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this.then(directionalBlurLayer(velocity, isHorizontal, scale))
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        this.velocityFallbackBlur(velocity, intensity, strength)
    } else {
        this
    }
}

/**
 * Directional motion blur driven by a [PagerState] (horizontal tab swipes).
 */
fun Modifier.scrollMotionBlur(
    pagerState: PagerState,
    enabled: Boolean,
    intensity: Int,
    strength: Int,
): Modifier = composed {
    if (!enabled) return@composed this
    val scale = motionBlurScale(intensity, strength)
    if (scale <= 0.001f) return@composed this
    val velocity = rememberPagerVelocity(pagerState)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this.then(directionalBlurLayer(velocity, true, scale))
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        this.velocityFallbackBlur(velocity, intensity, strength)
    } else {
        this
    }
}

/**
 * Glassmorphism surface with backdrop blur. Falls back to a translucent surface on older APIs.
 */
@Composable
fun glassSurface(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(16.dp),
    content: @Composable () -> Unit
) {
    val blurMod = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        modifier.blur(12.dp)
    } else {
        modifier
    }
    Surface(
        modifier = blurMod,
        shape = shape,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
        content = content
    )
}

/**
 * Glass card with blur backdrop and subtle border. Use for premium UI sections.
 */
@Composable
fun glassCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(20.dp),
    borderDp: Dp = 1.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val borderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
    Card(
        modifier = modifier
            .blur(8.dp)
            .border(borderDp, borderColor, shape),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        ),
        content = content
    )
}

/**
 * Entrance animation: fades in and slides up slightly. Use for screen content entry.
 */
@Composable
fun entranceAnimation(
    content: @Composable () -> Unit,
    delayMillis: Int = 0
) {
    val alpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 400, delayMillis = delayMillis),
        label = "EntranceAlpha"
    )
    val offset by animateFloatAsState(
        targetValue = 0f,
        animationSpec = tween(durationMillis = 500, delayMillis = delayMillis),
        label = "EntranceOffset"
    )
    Box(
        modifier = Modifier
            .graphicsLayer {
                this.alpha = alpha
                this.translationY = offset
            }
    ) {
        content()
    }
}

/**
 * Staggered entrance for lists: each item fades in with increasing delay.
 */
@Composable
fun staggeredItem(
    index: Int,
    baseDelay: Int = 50,
    content: @Composable () -> Unit
) {
    val delayMillis = index * baseDelay
    entranceAnimation(content = content, delayMillis = delayMillis)
}

