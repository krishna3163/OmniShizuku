package moe.shizuku.manager

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity

/**
 * Legacy Onboarding Activity stub.
 * All 1/11 onboarding and disclaimer screens have been removed per user request.
 * Immediately redirects to MainActivity and terminates.
 */
class FirstLaunchActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ShizukuSettings.initialize(this)
        ShizukuSettings.setFirstLaunch(false)
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
        finish()
    }
}
