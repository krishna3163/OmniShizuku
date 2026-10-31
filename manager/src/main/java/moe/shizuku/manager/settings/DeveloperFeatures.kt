package moe.shizuku.manager.settings

import android.content.Context

object DeveloperFeatures {
    private const val PREFERENCES = "developer_features"
    private const val ESSENTIALS_HUB_UNLOCKED = "essentials_hub_unlocked"

    fun isEssentialsHubUnlocked(context: Context): Boolean =
        context.applicationContext
            .getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
            .getBoolean(ESSENTIALS_HUB_UNLOCKED, false)

    fun unlockEssentialsHub(context: Context) {
        context.applicationContext
            .getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(ESSENTIALS_HUB_UNLOCKED, true)
            .apply()
    }
}
