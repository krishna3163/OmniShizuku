package moe.shizuku.manager.media

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaKeyScreen(
    bottomPadding: Dp,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var skipperEnabled by remember { mutableStateOf(MediaKeyManager.isSkipperEnabled(context)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Volume Key Track Skipper") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Screen-Off Volume Long-Press",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Long-pressing volume up/down when the screen is locked automatically skips forward or back on Spotify, YouTube Music, and media players.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Switch(
                        checked = skipperEnabled,
                        onCheckedChange = { checked ->
                            skipperEnabled = checked
                            MediaKeyManager.setSkipperEnabled(context, checked)
                        }
                    )
                }
            }

            // Quick Media Control Deck
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "HARDWARE MEDIA DISPATCH DECK",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilledTonalIconButton(
                            onClick = {
                                scope.launch {
                                    MediaKeyManager.skipPrevious()
                                    Toast.makeText(context, "Previous Track dispatched", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.size(54.dp)
                        ) {
                            Icon(Icons.Outlined.SkipPrevious, contentDescription = "Previous")
                        }

                        FilledIconButton(
                            onClick = {
                                scope.launch {
                                    MediaKeyManager.togglePlayPause()
                                    Toast.makeText(context, "Play/Pause dispatched", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.size(64.dp)
                        ) {
                            Icon(Icons.Outlined.PlayArrow, contentDescription = "Play/Pause")
                        }

                        FilledTonalIconButton(
                            onClick = {
                                scope.launch {
                                    MediaKeyManager.skipNext()
                                    Toast.makeText(context, "Next Track dispatched", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.size(54.dp)
                        ) {
                            Icon(Icons.Outlined.SkipNext, contentDescription = "Next")
                        }
                    }
                }
            }
        }
    }
}
