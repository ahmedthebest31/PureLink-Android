package com.ahmedsamy

import android.accessibilityservice.AccessibilityService
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast

class ClipboardService : AccessibilityService() {

    private lateinit var clipboard: ClipboardManager
    private lateinit var vibrator: Vibrator

    override fun onServiceConnected() {
        clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        checkAndClean()
    }

    override fun onInterrupt() {}

    private fun checkAndClean() {
        if (!clipboard.hasPrimaryClip()) return
        val item = clipboard.primaryClip?.getItemAt(0)
        val text = item?.text?.toString() ?: return

        if (isDirty(text)) {
            val cleaned = cleanUrl(text)
            if (cleaned == text) return 

            // النسخ الجديد
            val newClip = ClipData.newPlainText("Cleaned", cleaned)
            clipboard.setPrimaryClip(newClip)
            
            // قراءة الإعدادات
            val prefs = getSharedPreferences("PureLinkPrefs", Context.MODE_PRIVATE)
            val shouldVibrate = prefs.getBoolean("vibrate", true)
            val shouldToast = prefs.getBoolean("toast", true)

            // تنفيذ الهزاز (بقوة مضاعفة)
            if (shouldVibrate) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // Amplitude 255 = أقصى قوة للهزاز
                    vibrator.vibrate(VibrationEffect.createOneShot(200, 255))
                } else {
                    vibrator.vibrate(200)
                }
            }

            // تنفيذ التنبيه
            if (shouldToast) {
                Toast.makeText(this, "Link Cleaned 🧹", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isDirty(url: String): Boolean {
        return url.contains("utm_") || url.contains("fbclid") || url.contains("gclid") || url.contains("si=")
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