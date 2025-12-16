package com.ahmedsamy.purelink

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
        super.onServiceConnected()
        clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        Toast.makeText(this, "PureLink Monitor Started", Toast.LENGTH_SHORT).show()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        checkAndCleanClipboard()
    }

    override fun onInterrupt() {}

    private fun checkAndCleanClipboard() {
        if (!clipboard.hasPrimaryClip()) return
        val item = clipboard.primaryClip?.getItemAt(0)
        val text = item?.text?.toString() ?: return

        if (isDirty(text)) {
            val cleaned = cleanUrl(text)
            val newClip = ClipData.newPlainText("PureLink Cleaned", cleaned)
            clipboard.setPrimaryClip(newClip)
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                vibrator.vibrate(50)
            }
            Toast.makeText(this, "Link Cleaned", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isDirty(url: String): Boolean {
        return url.contains("utm_") || url.contains("fbclid") || url.contains("gclid")
    }

    private fun cleanUrl(url: String): String {
        var result = url
        val trackingPattern = Regex("([?&](utm_[^=&]+|fbclid|gclid|ref|s)=[^&]*)")
        result = trackingPattern.replace(result, "")
        if (result.endsWith("?") || result.endsWith("&")) {
            result = result.dropLast(1)
        }
        return result
    }
}
