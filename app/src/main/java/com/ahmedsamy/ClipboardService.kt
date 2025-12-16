package com.ahmedsamy

import android.accessibilityservice.AccessibilityService
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ClipboardManager.OnPrimaryClipChangedListener
import android.content.Context
import android.os.Build
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast

class ClipboardService : AccessibilityService() {

    private lateinit var clipboard: ClipboardManager
    private lateinit var vibrator: Vibrator
    private var lastCheckTime: Long = 0
    private val CHECK_COOLDOWN = 1000L // فحص واحد كل ثانية كحد أقصى

    // الطريقة 1: مستمع الحافظة (لبعض الأجهزة)
    private val clipListener = OnPrimaryClipChangedListener {
        performCheck("Listener")
    }

    override fun onServiceConnected() {
        clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        
        try {
            clipboard.addPrimaryClipChangedListener(clipListener)
        } catch (e: Exception) {
            // بعض الأجهزة بترفض التسجيل، مش مشكلة هنعتمد على الطريقة 2
        }
        
        Toast.makeText(this, "Monitor Force Started 🟢", Toast.LENGTH_SHORT).show()
    }

    // الطريقة 2: فحص إجباري مع أحداث الشاشة (زي قارئ الشاشة)
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // ده اللي هيخلي الخدمة شغالة غصب عن النظام
        // بنعمل فحص كل ثانية لو فيه نشاط على الشاشة
        val currentTime = SystemClock.elapsedRealtime()
        if (currentTime - lastCheckTime > CHECK_COOLDOWN) {
            performCheck("Event")
            lastCheckTime = currentTime
        }
    }

    override fun onInterrupt() {}

    private fun performCheck(source: String) {
        if (!::clipboard.isInitialized) return
        if (!clipboard.hasPrimaryClip()) return

        try {
            val item = clipboard.primaryClip?.getItemAt(0)
            val text = item?.text?.toString() ?: return

            if (isDirty(text)) {
                // تأكد إننا مش بننظف نفس الرابط تاني
                if (text.contains("PureLink")) return

                val cleaned = cleanUrl(text)
                if (cleaned != text) {
                    // النسخ
                    val newClip = ClipData.newPlainText("Cleaned by PureLink", cleaned)
                    clipboard.setPrimaryClip(newClip)
                    notifyUser()
                }
            }
        } catch (e: Exception) {
            // تجاهل الأخطاء العابرة
        }
    }

    private fun notifyUser() {
        val prefs = getSharedPreferences("PureLinkPrefs", Context.MODE_PRIVATE)
        val shouldVibrate = prefs.getBoolean("vibrate", true)
        val shouldToast = prefs.getBoolean("toast", true)

        if (shouldVibrate) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(200, 255)) // أقصى قوة
            } else {
                vibrator.vibrate(200)
            }
        }

        if (shouldToast) {
            Toast.makeText(this, "Link Cleaned! 🧹", Toast.LENGTH_SHORT).show()
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
