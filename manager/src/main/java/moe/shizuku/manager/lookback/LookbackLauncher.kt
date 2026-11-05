package moe.shizuku.manager.lookback

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext

@Composable
fun LookbackLauncher(onBack: () -> Unit) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        context.startActivity(Intent(context, LookbackActivity::class.java))
        onBack()
    }
}
