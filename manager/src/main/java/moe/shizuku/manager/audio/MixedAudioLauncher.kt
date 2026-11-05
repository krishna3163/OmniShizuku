package moe.shizuku.manager.audio

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext

@Composable
fun MixedAudioLauncher(onBack: () -> Unit) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        context.startActivity(Intent(context, MixedAudioActivity::class.java))
        onBack()
    }
}
