package moe.shizuku.manager.ui.component

import androidx.compose.foundation.border
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import moe.shizuku.manager.ui.theme.LocalAmoledTheme

/**
 * The hairline a card wears on the pure black theme.
 *
 * The Amoled switch repaints every surface container role black, which is what it is for and also
 * what leaves a card and the page under it the same colour: the outline is then the only thing
 * that says where one ends. The cards this app draws itself have always drawn it; this is the same
 * line in one place, for the ones that are a plain `Card` and were missed - the modules and the
 * module repositories, whose cards are plain ones.
 *
 * [shape] defaults to the shape `Card` paints its own container with, so the line lands on the
 * card's corner rather than slightly inside or outside it. A container that is not a `Card` has to
 * be given its own shape.
 */
@Composable
fun Modifier.cardOutline(shape: Shape = CardDefaults.shape): Modifier =
    if (LocalAmoledTheme.current) {
        border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant,
            shape = shape
        )
    } else {
        this
    }
