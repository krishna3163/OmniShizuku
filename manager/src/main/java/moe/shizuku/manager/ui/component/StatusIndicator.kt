package moe.shizuku.manager.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import moe.shizuku.manager.ui.theme.OmniTokens

/**
 * Status indicator: a colored dot with a label, showing connection/service state.
 *
 * Used throughout the app to communicate Shizuku connection, ADB availability,
 * root status, service state, etc. without fake data — only real states.
 */
enum class StatusState {
    Connected, Disconnected, Warning, Error, Info
}

@Composable
fun StatusIndicator(
    state: StatusState,
    label: String,
    modifier: Modifier = Modifier,
    showLabel: Boolean = true
) {
    val color = when (state) {
        StatusState.Connected -> OmniTokens.statusConnectedColor()
        StatusState.Disconnected -> OmniTokens.statusDisconnectedColor()
        StatusState.Warning -> OmniTokens.statusWarningColor()
        StatusState.Error -> OmniTokens.statusErrorColor()
        StatusState.Info -> OmniTokens.statusInfoColor()
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clipToBounds()
                .background(color, shape = androidx.compose.foundation.shape.CircleShape)
        )
        if (showLabel) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = OmniTokens.labelMedium(),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Section heading: consistent typography for section titles throughout the app.
 */
@Composable
fun SectionHeading(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
    ) {
        Text(
            text = text,
            style = OmniTokens.displayMedium(),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = OmniTokens.spaceM)
        )
    }
}

/**
 * Empty state: shown when a list or screen has no data to display.
 */
@Composable
fun EmptyState(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String? = null,
    onAction: (() -> Unit)? = null,
    actionLabel: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = OmniTokens.spaceXL),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        icon()
        Spacer(modifier = Modifier.height(OmniTokens.spaceL))

        Text(
            text = title,
            style = OmniTokens.bodyLarge(),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        subtitle?.let { sub ->
            Spacer(modifier = Modifier.height(OmniTokens.spaceXS))
            Text(
                text = sub,
                style = OmniTokens.bodySmall(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        if (onAction != null && actionLabel != null) {
            Spacer(modifier = Modifier.height(OmniTokens.spaceL))
            Button(
                onClick = onAction,
                modifier = Modifier.widthIn(min = 200.dp, max = 320.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = OmniTokens.brandPrimary(),
                    contentColor = Color.White
                )
            ) {
                Text(text = actionLabel!!)
            }
        }
    }
}

/**
 * Error state: shown when an operation fails or data cannot be loaded.
 */
@Composable
fun ErrorState(
    title: String = "Error",
    message: String? = null,
    onRetry: (() -> Unit)? = null,
    retryLabel: String = "Retry",
    modifier: Modifier = Modifier
) {
    EmptyState(
        icon = {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(OmniTokens.statusErrorColor(), shape = androidx.compose.foundation.shape.CircleShape)
            )
        },
        title = title,
        subtitle = message,
        onAction = onRetry,
        actionLabel = retryLabel,
        modifier = modifier
    )
}

/**
 * Loading state: shown while data is being fetched or an operation is in progress.
 */
@Composable
fun LoadingState(
    message: String = "Loading…",
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = OmniTokens.spaceXL),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.height(OmniTokens.spaceL))
        Text(
            text = message,
            style = OmniTokens.bodyMedium(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Confirmation dialog content: shown before destructive operations.
 *
 * Debloat, freeze, uninstall, and other destructive actions must require
 * explicit user confirmation before execution.
 */
@Composable
fun ConfirmationDialogContent(
    title: String,
    message: String,
    destructiveActionLabel: String,
    onDestructiveAction: () -> Unit,
    cancelLabel: String = "Cancel",
    onCancel: () -> Unit = {}
) {
    Text(
        text = title,
        style = OmniTokens.displayMedium(),
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(bottom = OmniTokens.spaceS)
    )
    Text(
        text = message,
        style = OmniTokens.bodyMedium(),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = OmniTokens.spaceL)
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End
    ) {
        TextButton(
            onClick = { onCancel() },
            modifier = Modifier.padding(end = OmniTokens.spaceS)
        ) {
            Text(text = cancelLabel)
        }
        Button(
            onClick = { onDestructiveAction() },
            colors = ButtonDefaults.buttonColors(
                containerColor = OmniTokens.statusErrorColor(),
                contentColor = Color.White
            )
        ) {
            Text(text = destructiveActionLabel)
        }
    }
}
