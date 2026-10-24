package moe.shizuku.manager.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * A feature's icon on a tinted plate, the shape the Labs list and the hiding list both lead their
 * rows with.
 *
 * The plate is what makes a list of features scannable the way a launcher's icons are: at this size
 * the outline of an icon alone is not enough to tell one row from another at a glance, and the
 * colour is what the eye lands on first. Kept in one place because the two screens showing the same
 * features must not drift into two different sizes of the same thing.
 */
@Composable
fun FeaturePlateIcon(icon: ImageVector, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.size(40.dp),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
