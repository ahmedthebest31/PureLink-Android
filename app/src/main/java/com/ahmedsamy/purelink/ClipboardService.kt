@file:Suppress("DEPRECATION")

package com.ahmedsamy.purelink

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ClipboardManager.OnPrimaryClipChangedListener
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import androidx.core.content.edit

@SuppressLint("AccessibilityPolicy")
class ClipboardService : AccessibilityService() {

    companion object {
        private const val TAG = "PureLinkService"
    }

    private lateinit var clipboard: ClipboardManager
    private lateinit var vibrator: Vibrator
    private val handler = Handler(Looper.getMainLooper())

    // Flag to prevent re-entry when we modify the clipboard ourselves
    @Volatile private var isProcessing = false

    private val clipListener = OnPrimaryClipChangedListener {
        Log.d(TAG, "Clipboard changed! isProcessing=$isProcessing")
        checkAndClean()
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "Service connected!")
        clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        clipboard.addPrimaryClipChangedListener(clipListener)

        // التأكد إن الحالة الافتراضية "شغال" عند أول تشغيل
        val prefs = getSharedPreferences("PureLinkPrefs", MODE_PRIVATE)
        if (!prefs.contains("monitoring_active")) {
            prefs.edit { putBoolean("monitoring_active", true) }
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
        // Prevent re-entry when we set the clipboard ourselves
        if (isProcessing) {
            Log.d(TAG, "Skipping - already processing")
            return
        }

        // 1. التحقق من مفتاح التشغيل (زرار الستارة)
        val prefs = getSharedPreferences("PureLinkPrefs", MODE_PRIVATE)
        val isMonitoringActive = prefs.getBoolean("monitoring_active", true)

        // لو المستخدم طافيه، لا تعمل شيء
        if (!isMonitoringActive) {
            Log.d(TAG, "Skipping - monitoring inactive")
            return
        }

        if (!clipboard.hasPrimaryClip()) {
            Log.d(TAG, "Skipping - no primary clip")
            return
        }

        // Check if this clip was already processed by us
        val clipLabel = clipboard.primaryClip?.description?.label?.toString() ?: ""
        Log.d(TAG, "Clip label: '$clipLabel'")
        if (clipLabel.contains("PureLink", ignoreCase = true)) {
            Log.d(TAG, "Skipping - already cleaned by PureLink")
            return
        }

        val item =
                try {
                    clipboard.primaryClip?.getItemAt(0)
                } catch (e: Exception) {
                    Log.e(TAG, "Error getting clip item", e)
                    return
                }
        val text = item?.text?.toString()

        if (text.isNullOrBlank()) {
            Log.d(TAG, "Skipping - text is null or blank")
            return
        }

        Log.d(TAG, "Clipboard text: $text")

        // Check if it looks like a URL
        if (!text.contains("http")) {
            Log.d(TAG, "Skipping - not a URL")
            return
        }

        if (!isDirty(text)) {
            Log.d(TAG, "Skipping - URL is clean (no tracking params)")
            return
        }

        Log.d(TAG, "URL is dirty, cleaning...")

        val cleaned = cleanUrl(text)
        Log.d(TAG, "Original: $text")
        Log.d(TAG, "Cleaned:  $cleaned")

        if (cleaned == text) {
            Log.d(TAG, "Skipping - cleaned URL is same as original")
            return
        }

        // Set flag BEFORE modifying clipboard
        isProcessing = true
        Log.d(TAG, "Setting isProcessing = true")

        val newClip = ClipData.newPlainText("Cleaned by PureLink", cleaned)
        clipboard.setPrimaryClip(newClip)
        Log.d(TAG, "Clipboard updated with cleaned URL")

        // Reset flag after a delay to allow the listener callback to complete
        handler.postDelayed(
                {
                    isProcessing = false
                    Log.d(TAG, "Reset isProcessing = false")
                },
                500
        )

        notifyUser()
    }

    private fun notifyUser() {
        val prefs = getSharedPreferences("PureLinkPrefs", MODE_PRIVATE)
        val shouldVibrate = prefs.getBoolean("vibrate", true)
        val shouldToast = prefs.getBoolean("toast", true)

        if (shouldVibrate) {
            vibrator.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE))
        }

        if (shouldToast) {
            Toast.makeText(this, "Cleaned! 🧹", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isDirty(url: String): Boolean {
        val trackingPatterns =
                listOf(
                        "utm_",
                        "fbclid",
                        "gclid",
                        "si=",
                        "ref=",
                        "mc_eid",
                        "mc_cid",
                        "_ga",
                        "yclid",
                        "affiliate"
                )
        val hasDirty = trackingPatterns.any { url.contains(it, ignoreCase = true) }
        Log.d(TAG, "isDirty check: $hasDirty")
        return hasDirty
    }

    private fun cleanUrl(url: String): String {
        var result = url

        // More comprehensive tracking parameter list
        val trackingParams =
                listOf(
                        "utm_source",
                        "utm_medium",
                        "utm_campaign",
                        "utm_term",
                        "utm_content",
                        "fbclid",
                        "gclid",
                        "ref",
                        "si",
                        "s",
                        "mc_eid",
                        "mc_cid",
                        "_ga",
                        "yclid",
                        "affiliate",
                        "source",
                        "campaign"
                )

        // Build regex pattern for all tracking params
        val paramPattern = trackingParams.joinToString("|") { Regex.escape(it) }
        val regex = Regex("[?&]($paramPattern)=[^&]*", RegexOption.IGNORE_CASE)

        result = regex.replace(result, "")

        // Clean up leftover ? or & at the end or double &&
        result = result.replace(Regex("\\?&"), "?")
        result = result.replace(Regex("&&+"), "&")
        if (result.endsWith("?") || result.endsWith("&")) {
            result = result.dropLast(1)
        }
        // If we removed all params but still have a trailing ?, remove it
        if (result.endsWith("?")) {
            result = result.dropLast(1)
        }

        Log.d(TAG, "cleanUrl result: $result")
        return result
    }
}
