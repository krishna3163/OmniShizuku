package moe.shizuku.manager.quicktiles

import android.app.Activity
import android.os.Bundle
import android.widget.Toast

class ShortcutActionActivity : Activity() {

    companion object {
        const val ACTION_DNS_OFF = "com.rbn.qtsettings.action.DNS_OFF"
        const val ACTION_DNS_AUTO = "com.rbn.qtsettings.action.DNS_AUTO"
        const val ACTION_DNS_CLOUDFLARE = "com.rbn.qtsettings.action.DNS_CLOUDFLARE"
        const val ACTION_DNS_ADGUARD = "com.rbn.qtsettings.action.DNS_ADGUARD"
        const val ACTION_DNS_QUAD9 = "com.rbn.qtsettings.action.DNS_QUAD9"
        const val ACTION_USB_ON = "com.rbn.qtsettings.action.USB_ON"
        const val ACTION_USB_OFF = "com.rbn.qtsettings.action.USB_OFF"

        // OmniShizuku specific actions
        const val ACTION_OMNI_DNS_OFF = "moe.shizuku.manager.action.DNS_OFF"
        const val ACTION_OMNI_DNS_AUTO = "moe.shizuku.manager.action.DNS_AUTO"
        const val ACTION_OMNI_DNS_CLOUDFLARE = "moe.shizuku.manager.action.DNS_CLOUDFLARE"
        const val ACTION_OMNI_DNS_ADGUARD = "moe.shizuku.manager.action.DNS_ADGUARD"
        const val ACTION_OMNI_DNS_QUAD9 = "moe.shizuku.manager.action.DNS_QUAD9"
        const val ACTION_OMNI_USB_ON = "moe.shizuku.manager.action.USB_ON"
        const val ACTION_OMNI_USB_OFF = "moe.shizuku.manager.action.USB_OFF"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleAction(intent?.action)
        finish()
    }

    private fun handleAction(action: String?) {
        when (action) {
            ACTION_DNS_OFF, ACTION_OMNI_DNS_OFF -> {
                QuickTileSettingsManager.setPrivateDnsMode("off", null)
                Toast.makeText(this, "Private DNS disabled", Toast.LENGTH_SHORT).show()
            }
            ACTION_DNS_AUTO, ACTION_OMNI_DNS_AUTO -> {
                QuickTileSettingsManager.setPrivateDnsMode("opportunistic", null)
                Toast.makeText(this, "Private DNS set to Auto", Toast.LENGTH_SHORT).show()
            }
            ACTION_DNS_CLOUDFLARE, ACTION_OMNI_DNS_CLOUDFLARE -> {
                QuickTileSettingsManager.setPrivateDnsMode("hostname", QuickTileSettingsManager.HOSTNAME_CLOUDFLARE)
                Toast.makeText(this, "Private DNS: Cloudflare (1.1.1.1)", Toast.LENGTH_SHORT).show()
            }
            ACTION_DNS_ADGUARD, ACTION_OMNI_DNS_ADGUARD -> {
                QuickTileSettingsManager.setPrivateDnsMode("hostname", QuickTileSettingsManager.HOSTNAME_ADGUARD)
                Toast.makeText(this, "Private DNS: AdGuard", Toast.LENGTH_SHORT).show()
            }
            ACTION_DNS_QUAD9, ACTION_OMNI_DNS_QUAD9 -> {
                QuickTileSettingsManager.setPrivateDnsMode("hostname", QuickTileSettingsManager.HOSTNAME_QUAD9)
                Toast.makeText(this, "Private DNS: Quad9", Toast.LENGTH_SHORT).show()
            }
            ACTION_USB_ON, ACTION_OMNI_USB_ON -> {
                QuickTileSettingsManager.setUsbDebuggingEnabled(true)
                Toast.makeText(this, "USB Debugging enabled", Toast.LENGTH_SHORT).show()
            }
            ACTION_USB_OFF, ACTION_OMNI_USB_OFF -> {
                QuickTileSettingsManager.setUsbDebuggingEnabled(false)
                Toast.makeText(this, "USB Debugging disabled", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
