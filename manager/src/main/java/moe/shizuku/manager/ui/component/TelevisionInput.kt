package moe.shizuku.manager.ui.component

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
import moe.shizuku.manager.ui.theme.LocalTelevision

/**
 * A text field as a TV needs it: the keyboard waits to be asked for.
 *
 * A text field opens the keyboard the moment it is focused, which on a phone is the moment it
 * is tapped. On a TV focus arrives by walking the D-pad past things, so walking down the Apps
 * page put the whole keyboard over it on the way through the search field, and the next press
 * went to the keyboard instead of the list. Asking the keyboard not to show on focus was not
 * enough: the TV keyboard shows itself for any field that takes focus.
 *
 * So on a TV the field itself cannot take focus until it is chosen. The D-pad stops on it as
 * on any other item, OK on the remote is what puts the cursor in it - the same as tapping it
 * is on a phone - and up, down or back take the cursor out again, so a field can never be the
 * place the remote gets stuck. Apply it to the field's own modifier, with the field's shape so
 * the focus highlight follows its outline. Off a TV it is nothing.
 */
@Composable
fun Modifier.televisionTextInput(shape: Shape = RectangleShape): Modifier {
    if (!LocalTelevision.current) return this

    val focusManager = LocalFocusManager.current
    var editing by remember { mutableStateOf(false) }
    val item = remember { FocusRequester() }
    val field = remember { FocusRequester() }
    LaunchedEffect(editing) {
        // A disabled field cannot take the focus, and then there is no editing to be in: left
        // set, it would leave back answering for a field nobody is typing in.
        if (editing && !field.requestFocus()) editing = false
    }
    // Back as the system's back rather than as a key: from Android 13 an app that takes part in
    // predictive back is not sent the back key at all, only the back callback. Registered after
    // the tab rail's own back, so while the cursor is in the field this one is asked first. The
    // keyboard takes the first back for itself; this is the one after it.
    BackHandler(enabled = editing) {
        editing = false
        item.requestFocus()
    }
    return this
        .clip(shape)
        .televisionFocusShape(shape, includeDescendants = true)
        .focusRequester(item)
        .clickable { editing = true }
        .onPreviewKeyEvent { event ->
            if (!editing || event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
            when (event.key) {
                Key.DirectionUp -> focusManager.moveFocus(FocusDirection.Up)
                Key.DirectionDown -> focusManager.moveFocus(FocusDirection.Down)
                else -> false
            }
        }
        .onFocusChanged { if (editing && !it.isFocused) editing = false }
        .focusProperties { canFocus = editing }
        .focusRequester(field)
}
