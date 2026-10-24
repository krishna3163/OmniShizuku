package moe.shizuku.manager.ui.component

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import moe.shizuku.manager.ui.theme.LocalTelevision

/**
 * The switch this app uses everywhere.
 *
 * It is Material 3's own switch carrying a thumb icon, which is the expressive treatment of
 * it: the state reads on its own without reading the row it belongs to, and the theme's
 * expressive motion scheme animates it. One component rather than a `thumbContent` at every
 * call site, so that no screen can end up with a switch that looks different from the rest,
 * and so that changing the treatment is one edit rather than fifteen.
 *
 * The parameters are named exactly as Material 3's are, which is what makes it a drop-in
 * replacement for the call sites it was introduced for.
 *
 * [focusableWhenDisabled] is for a TV, where a disabled Material 3 switch stops taking focus.
 * Where a switch is disabled only for a moment - while the change it just asked for is running -
 * the focus on it was dropped the moment it was pressed, and nothing held it afterwards. With
 * this, on a TV the switch is one focus target whether it is enabled or not, and while it is
 * disabled OK does nothing. Off a TV it changes nothing.
 */
@Composable
fun ExpressiveSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    focusableWhenDisabled: Boolean = false,
    colors: SwitchColors = SwitchDefaults.colors()
) {
    val steadyFocus = focusableWhenDisabled && onCheckedChange != null && LocalTelevision.current
    val toggle = if (steadyFocus) {
        val interactions = remember { MutableInteractionSource() }
        Modifier
            .toggleable(
                value = checked,
                interactionSource = interactions,
                indication = ripple(),
                role = Role.Switch,
                onValueChange = { if (enabled) onCheckedChange?.invoke(it) }
            )
            .semantics { if (!enabled) disabled() }
    } else {
        Modifier
    }
    Switch(
        checked = checked,
        // With a steady focus target the toggle is on the modifier, and the switch only draws.
        onCheckedChange = if (steadyFocus) null else onCheckedChange,
        modifier = modifier.then(toggle),
        enabled = enabled,
        colors = colors,
        thumbContent = {
            Icon(
                imageVector = if (checked) Icons.Rounded.Check else Icons.Rounded.Remove,
                contentDescription = null,
                modifier = Modifier.size(SwitchDefaults.IconSize)
            )
        }
    )
}
