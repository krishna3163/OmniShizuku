package moe.shizuku.manager.ui.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.focusable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.onFocusedBoundsChanged
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import moe.shizuku.manager.ui.theme.LocalTelevision

/** The gap between the focused item and the outline drawn around it. */
private val OutlineGap = 4.dp
private val OutlineWidth = 3.dp

/** At or under this height an item with no shape of its own is taken to be a pill. */
private val PillMaxHeight = 64.dp

/**
 * What the focused item has said about its own outline: the shape to follow, or that it wants
 * none. Kept for whichever item last took focus, so an item losing focus after the next one has
 * gained it does not wipe the newer item's word.
 */
private class FocusOutline {
    var shape by mutableStateOf<Shape?>(null)
    var hidden by mutableStateOf(false)
    var owner: Any? = null
}

private val LocalFocusOutline = staticCompositionLocalOf<FocusOutline?> { null }

/**
 * An outline around whatever is focused anywhere inside [content], on a TV.
 *
 * A remote's focus is the only cursor a TV has, and a state layer on the focused item - however
 * strong - is a change of shade that has to be compared with its neighbours to be seen. An
 * outline a little way out from the item is the convention TV apps use for the same reason: it
 * is a shape, not a shade, and it reads against any colour the item itself happens to be.
 *
 * Drawn once here for the whole screen rather than by every button, switch, chip and card: the
 * focused item reports its bounds up the tree, and those are what the outline follows as focus
 * moves and as the item scrolls. An item can say which shape it is with [televisionFocusShape];
 * one that does not is drawn as a pill when it is short and with the theme's card corner when it
 * is tall, which is what the app's buttons, chips, fields and cards are. Off a TV this is only
 * [content].
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TelevisionFocusOutline(content: @Composable () -> Unit) {
    if (!LocalTelevision.current) {
        content()
        return
    }

    val state = remember { FocusOutline() }
    var container by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var focused by remember { mutableStateOf<Rect?>(null) }
    val color = MaterialTheme.colorScheme.primary
    val cardShape = MaterialTheme.shapes.large

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { container = it }
            .onFocusedBoundsChanged { coordinates ->
                val box = container
                // Clipped to what is visible, so an item half scrolled under a bar is outlined
                // where it can be seen rather than over the bar.
                focused = if (coordinates != null && box != null &&
                    coordinates.isAttached && box.isAttached
                ) {
                    box.localBoundingBoxOf(coordinates)
                } else {
                    null
                }
            }
            .drawWithContent {
                drawContent()
                val bounds = focused ?: return@drawWithContent
                if (bounds.isEmpty || state.hidden) return@drawWithContent

                val gap = OutlineGap.toPx()
                val outer = bounds.inflate(gap)
                val stroke = Stroke(width = OutlineWidth.toPx())
                val shape = state.shape
                    ?: if (bounds.height <= PillMaxHeight.toPx()) null else cardShape

                if (shape == null) {
                    drawRoundRect(
                        color = color,
                        topLeft = outer.topLeft,
                        size = outer.size,
                        cornerRadius = CornerRadius(outer.height / 2f),
                        style = stroke
                    )
                } else if (shape is CornerBasedShape) {
                    // Each corner the item's own plus the gap, so the outline runs the same
                    // distance from the item all the way round instead of pinching at the
                    // corners.
                    fun corner(radius: Float) = CornerRadius(radius + gap)
                    val path = Path().apply {
                        addRoundRect(
                            RoundRect(
                                rect = outer,
                                topLeft = corner(shape.topStart.toPx(bounds.size, this@drawWithContent)),
                                topRight = corner(shape.topEnd.toPx(bounds.size, this@drawWithContent)),
                                bottomRight = corner(shape.bottomEnd.toPx(bounds.size, this@drawWithContent)),
                                bottomLeft = corner(shape.bottomStart.toPx(bounds.size, this@drawWithContent))
                            )
                        )
                    }
                    drawPath(path, color, style = stroke)
                } else {
                    val outline = shape.createOutline(outer.size, layoutDirection, this)
                    val path = Path().apply {
                        addOutline(outline)
                        translate(outer.topLeft)
                    }
                    drawPath(path, color, style = stroke)
                }
            }
    ) {
        CompositionLocalProvider(LocalFocusOutline provides state) {
            content()
        }
    }
}

/**
 * Tells the TV focus outline what shape this item is, so the outline follows it; or, with
 * `null`, that it wants no outline at all - for rows whose focused fill already spans the width
 * and for the tab rail, whose selected pill says where the focus is. Put it directly before the
 * item's own clickable.
 *
 * It speaks for that item alone, so a button inside a row that wants no outline still gets one.
 * [includeDescendants] widens it to whatever inside the item takes focus, for a wrapper whose
 * focus is handed on to a child it draws - a text field, while the cursor is in it. Off a TV, or
 * outside a [TelevisionFocusOutline], it is nothing.
 */
@Composable
fun Modifier.televisionFocusShape(shape: Shape?, includeDescendants: Boolean = false): Modifier {
    val state = LocalFocusOutline.current ?: return this
    val token = remember { Any() }
    return onFocusChanged {
        if (if (includeDescendants) it.hasFocus else it.isFocused) {
            state.owner = token
            state.shape = shape
            state.hidden = shape == null
        } else if (state.owner === token) {
            state.owner = null
            state.shape = null
            state.hidden = false
        }
    }
}

/**
 * Lets the D-pad stop on a row that does nothing when chosen, on a TV.
 *
 * A remote moves only between things that take focus, and a page scrolls only to bring focus
 * into view. So a run of rows that are only there to be read - the versions on the home page,
 * the device information - could not be scrolled to at all: the focus jumped over them and they
 * stayed below the edge of the screen. Focusable, they are walked through like any other list,
 * with the same fill a clickable row shows. Off a TV it is nothing.
 */
@Composable
fun Modifier.televisionFocusableRow(): Modifier {
    if (!LocalTelevision.current) return this
    val interactions = remember { MutableInteractionSource() }
    return this
        .focusable(interactionSource = interactions)
        .indication(interactions, ripple())
}

/**
 * On a TV, right on this item moves the focus onto the controls inside it.
 *
 * A card that is a button of its own can hold buttons of its own too - a module's switch and its
 * actions, a module's install button. A remote moves only to what lies past the edge of the
 * focused item, and those lie within it, so with the card focused no press reached them. Right
 * is that press here: it enters the card, and back leaves it again. Where the card holds nothing
 * that can take focus, right does what it always did. Put it before the card's own clickable.
 * Off a TV it is nothing.
 */
@Composable
fun Modifier.televisionEnterOnRight(): Modifier {
    if (!LocalTelevision.current) return this
    val focusManager = LocalFocusManager.current
    var focused by remember { mutableStateOf(false) }
    return this
        .onPreviewKeyEvent { event ->
            focused && event.type == KeyEventType.KeyDown && event.key == Key.DirectionRight &&
                focusManager.moveFocus(FocusDirection.Enter)
        }
        .onFocusChanged { focused = it.isFocused }
}
