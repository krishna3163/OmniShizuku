package moe.shizuku.manager.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import moe.shizuku.manager.R
import androidx.compose.material3.MaterialTheme

/**
 * YouTube-style opening splash animation customized for OmniShizuku's mascot logo.
 * Features an ethereal glowing cosmic halo, gentle spring entrance,
 * animated cyber terminal status, and a smooth fluid zoom-reveal transition into the app.
 */
@Composable
fun AppOpeningSplashScreen(
    onAnimationEnd: () -> Unit
) {
    val scale = remember { Animatable(0.72f) }
    val alpha = remember { Animatable(0f) }
    val exitScale = remember { Animatable(1f) }
    val exitAlpha = remember { Animatable(1f) }
    val textAlpha = remember { Animatable(0f) }

    // Ethereal breathing halo glow
    val infiniteTransition = rememberInfiniteTransition(label = "halo_glow")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_scale"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    // Blinking terminal cursor
    var cursorVisible by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(400)
            cursorVisible = !cursorVisible
        }
    }

    // YouTube-style sequential animation
    LaunchedEffect(Unit) {
        // Step 1: Smooth entrance pop with spring damping
        launch {
            alpha.animateTo(1f, animationSpec = tween(350, easing = LinearEasing))
        }
        launch {
            scale.animateTo(
                1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        delay(220)

        // Step 2: Fade in title & cyber prompt
        textAlpha.animateTo(1f, animationSpec = tween(320, easing = FastOutSlowInEasing))

        // Step 3: Hold briefly for brand recognition
        delay(700)

        // Step 4: YouTube-style smooth zoom-out reveal into main UI
        launch {
            exitScale.animateTo(
                1.42f,
                animationSpec = tween(360, easing = FastOutSlowInEasing)
            )
        }
        exitAlpha.animateTo(
            0f,
            animationSpec = tween(360, easing = FastOutSlowInEasing)
        )

        onAnimationEnd()
    }

    // Tap anywhere to skip instantly
    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(exitAlpha.value)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                        MaterialTheme.colorScheme.background.copy(alpha = 0.9f),
                    )
                )
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onAnimationEnd()
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .scale(scale.value * exitScale.value)
                .alpha(alpha.value),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(240.dp)
            ) {
                // Dynamic color cosmic aura glow based on wallpaper
                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .scale(glowScale)
                        .alpha(glowAlpha * exitAlpha.value)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0f)
                                )
                            ),
                            shape = CircleShape
                        )
                )

                // High-resolution mascot logo
                Image(
                    painter = painterResource(id = R.drawable.ic_splash_logo),
                    contentDescription = "OmniShizuku Mascot Logo",
                    modifier = Modifier
                        .size(170.dp)
                        .clip(RoundedCornerShape(36.dp))
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier.alpha(textAlpha.value),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "OmniShizuku",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.2.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = ">_ privileged binder engine" + if (cursorVisible) "_" else " ",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.8.sp
                )
            }
        }
    }
}
