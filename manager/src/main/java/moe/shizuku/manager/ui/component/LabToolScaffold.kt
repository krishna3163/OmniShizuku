package moe.shizuku.manager.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import moe.shizuku.manager.ui.theme.OmniTokens

/**
 * Shared scaffold for Labs tool detail screens.
 * Preserves TopAppBar/back behavior while standardizing header and status messaging.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabToolScaffold(
    title: String,
    onBack: () -> Unit,
    bottomPadding: Dp = 0.dp,
    description: String? = null,
    status: LabToolStatus? = null,
    actions: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.(PaddingValues) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(title) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                }
            },
            actions = { actions?.invoke() },
            windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            if (!description.isNullOrBlank()) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
            }
            status?.let { LabToolStatusBanner(it) }
        }
        content(PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = bottomPadding + 16.dp))
    }
}

sealed interface LabToolStatus {
    data class Info(val title: String, val message: String, val icon: ImageVector? = null) : LabToolStatus
    data class Warning(val title: String, val message: String, val icon: ImageVector? = null) : LabToolStatus
    data class Error(val title: String, val message: String, val icon: ImageVector? = null) : LabToolStatus
    data class Permission(val title: String, val message: String, val actionLabel: String? = null, val onAction: (() -> Unit)? = null) : LabToolStatus
}

@Composable
fun LabToolStatusBanner(status: LabToolStatus) {
    val (container, content, title, message, actionLabel, onAction) = when (status) {
        is LabToolStatus.Info -> Sextuple(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer,
            status.title, status.message, null, null
        )
        is LabToolStatus.Warning -> Sextuple(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer,
            status.title, status.message, null, null
        )
        is LabToolStatus.Error -> Sextuple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            status.title, status.message, null, null
        )
        is LabToolStatus.Permission -> Sextuple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            status.title, status.message, status.actionLabel, status.onAction
        )
    }
    Surface(
        color = container,
        contentColor = content,
        shape = OmniTokens.shapeMedium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(message, style = MaterialTheme.typography.bodySmall)
            if (actionLabel != null && onAction != null) {
                OutlinedButton(onClick = onAction, modifier = Modifier.padding(top = 4.dp)) {
                    Text(actionLabel)
                }
            }
        }
    }
    Spacer(Modifier.height(8.dp))
}

private data class Sextuple<A, B, C, D, E, F>(val a: A, val b: B, val c: C, val d: D, val e: E, val f: F)
