package moe.shizuku.manager

import android.os.Bundle
import moe.shizuku.manager.home.HomeActivity

class MainActivity : HomeActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ShizukuSettings.initialize(this)
    }
}
