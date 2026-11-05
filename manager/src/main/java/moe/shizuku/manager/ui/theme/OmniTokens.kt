package moe.shizuku.manager.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * OmniShizuku design token system.
 *
 * Three-layer architecture following UI UX Pro Max guidance:
 *   Primitive (raw values) -> Semantic (purpose aliases) -> Component (specific usage)
 *
 * These tokens wrap MaterialTheme.colorScheme so that existing dark/light/AMOLED/dynamic
 * color theming continues to work, while adding OmniShizuku-specific semantic aliases
 * and component-level tokens for consistency across screens.
 */

object OmniTokens {

    // ---------------------------------------------------------------------------
    // Semantic color tokens (all @Composable because they read MaterialTheme)
    // ---------------------------------------------------------------------------

    /** Brand indigo #3F51B5 — primary CTAs, active states, selected tab icons. */
    @Composable fun brandPrimary() = Color(0xFF3F51B5)

    /** Success green — enabled states, successful operations. */
    @Composable fun semanticSuccess() = Color(0xFF4CAF50)

    /** Warning amber — cautions, attention needed. */
    @Composable fun semanticWarning() = Color(0xFFFF9800)

    /** Error red — destructive actions, failures. */
    @Composable fun semanticError() = Color(0xFFF44336)

    /** Info blue — neutral information. */
    @Composable fun semanticInfo() = Color(0xFF2196F3)

    /** Status indicator: connected / success. */
    @Composable fun statusConnectedColor() = semanticSuccess()

    /** Status indicator: disconnected / inactive. */
    @Composable fun statusDisconnectedColor() = MaterialTheme.colorScheme.onSurfaceVariant

    /** Status indicator: warning / attention. */
    @Composable fun statusWarningColor() = semanticWarning()

    /** Status indicator: error / failure. */
    @Composable fun statusErrorColor() = semanticError()

    /** Status indicator: info / neutral. */
    @Composable fun statusInfoColor() = semanticInfo()

    // ---------------------------------------------------------------------------
    // Semantic typography tokens (all @Composable because they read MaterialTheme)
    // ---------------------------------------------------------------------------

    /** Page titles / hero headings — 32sp, weight 400. */
    @Composable fun displayXLarge() = MaterialTheme.typography.displayLarge

    /** Section headings — 24sp, weight 500. */
    @Composable fun displayLarge() = MaterialTheme.typography.displayMedium

    /** Card titles — 20sp, weight 500. */
    @Composable fun displayMedium() = MaterialTheme.typography.displaySmall

    /** Primary body text — 16sp, weight 400. */
    @Composable fun bodyLarge() = MaterialTheme.typography.bodyLarge

    /** Secondary body — 14sp, weight 400. */
    @Composable fun bodyMedium() = MaterialTheme.typography.bodyMedium

    /** Captions, metadata — 12sp, weight 400. */
    @Composable fun bodySmall() = MaterialTheme.typography.bodySmall

    /** Button labels, list item titles — 14sp, weight 500. */
    @Composable fun labelLarge() = MaterialTheme.typography.labelLarge

    /** Chip labels, toggle labels — 12sp, weight 500. */
    @Composable fun labelMedium() = MaterialTheme.typography.labelMedium

    /** Helper text, error messages — 11sp, weight 400. */
    @Composable fun captionStyle() = MaterialTheme.typography.labelSmall

    // ---------------------------------------------------------------------------
    // Semantic spacing tokens (4px base unit) — non-Composable (dp values only)
    // ---------------------------------------------------------------------------

    val spaceXXS = 2.dp
    val spaceXS = 4.dp
    val spaceS = 8.dp
    val spaceM = 12.dp
    val spaceL = 16.dp
    val spaceXL = 20.dp
    val space2XL = 24.dp
    val space3XL = 32.dp
    val space4XL = 48.dp

    // ---------------------------------------------------------------------------
    // Semantic shape tokens — non-Composable
    // ---------------------------------------------------------------------------

    val shapeNone = RoundedCornerShape(0.dp)
    val shapeSmall = RoundedCornerShape(4.dp)
    val shapeMedium = RoundedCornerShape(8.dp)
    val shapeLarge = RoundedCornerShape(12.dp)
    val shapeFull = CircleShape
}
