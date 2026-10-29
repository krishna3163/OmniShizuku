package moe.shizuku.manager

import android.content.Intent
import android.os.Bundle
import moe.shizuku.manager.home.HomeActivity

class MainActivity : HomeActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ShizukuSettings.initialize(this)
        if (ShizukuSettings.isFirstLaunch()) {
            val intent = Intent(this, FirstLaunchActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
    }
}
