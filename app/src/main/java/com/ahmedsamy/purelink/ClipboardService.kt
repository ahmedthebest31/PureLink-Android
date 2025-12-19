package com.ahmedsamy.purelink

import android.accessibilityservice.AccessibilityService
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ClipboardManager.OnPrimaryClipChangedListener
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast

class ClipboardService : AccessibilityService() {

    private lateinit var clipboard: ClipboardManager
    private lateinit var vibrator: Vibrator
    
    private val clipListener = OnPrimaryClipChangedListener {
        checkAndClean()
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        clipboard.addPrimaryClipChangedListener(clipListener)
        
        // التأكد إن الحالة الافتراضية "شغال" عند أول تشغيل
        val prefs = getSharedPreferences("PureLinkPrefs", Context.MODE_PRIVATE)
        if (!prefs.contains("monitoring_active")) {
            prefs.edit().putBoolean("monitoring_active", true).apply()
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        if (::clipboard.isInitialized) {
            clipboard.removePrimaryClipChangedListener(clipListener)
        }
    }

    private fun checkAndClean() {
        // 1. التحقق من مفتاح التشغيل (زرار الستارة)
        val prefs = getSharedPreferences("PureLinkPrefs", Context.MODE_PRIVATE)
        val isMonitoringActive = prefs.getBoolean("monitoring_active", true)
        
        // لو المستخدم طافيه، لا تعمل شيء
        if (!isMonitoringActive) return

        if (!clipboard.hasPrimaryClip()) return
        
        val item = try { clipboard.primaryClip?.getItemAt(0) } catch (e: Exception) { return }
        val text = item?.text?.toString() ?: return

        if (!isDirty(text)) return
        if (item.text != null && item.text.toString().contains("PureLink")) return

        val cleaned = cleanUrl(text)
        if (cleaned == text) return 

        val newClip = ClipData.newPlainText("Cleaned by PureLink", cleaned)
        clipboard.setPrimaryClip(newClip)
        
        notifyUser()
    }

    private fun notifyUser() {
        val prefs = getSharedPreferences("PureLinkPrefs", Context.MODE_PRIVATE)
        val shouldVibrate = prefs.getBoolean("vibrate", true)
        val shouldToast = prefs.getBoolean("toast", true)

        if (shouldVibrate) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                vibrator.vibrate(150)
            }
        }

        if (shouldToast) {
            Toast.makeText(this, "Cleaned! 🧹", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isDirty(url: String): Boolean {
        return url.contains("utm_") || url.contains("fbclid") || url.contains("gclid") || url.contains("si=") || url.contains("ref=")
    }

    private fun cleanUrl(url: String): String {
        var result = url
        val trackingPattern = Regex("([?&](utm_[^=&]+|fbclid|gclid|ref|s|si)=[^&]*)")
        result = trackingPattern.replace(result, "")
        if (result.endsWith("?") || result.endsWith("&")) {
            result = result.dropLast(1)
        }
        return result
    }
}
