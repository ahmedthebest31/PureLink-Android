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
import com.ahmedsamy.purelink.utils.UrlCleaner

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
        try {
            checkAndClean()
        } catch (e: Exception) {
            Log.e(TAG, "Error in checkAndClean", e)
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "Service connected!")
        clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        clipboard.addPrimaryClipChangedListener(clipListener)

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
        if (isProcessing) {
            Log.d(TAG, "Skipping - already processing")
            return
        }

        val prefs = getSharedPreferences("PureLinkPrefs", MODE_PRIVATE)
        val isMonitoringActive = prefs.getBoolean("monitoring_active", true)

        if (!isMonitoringActive) {
            Log.d(TAG, "Skipping - monitoring inactive")
            return
        }

        if (!clipboard.hasPrimaryClip()) {
            Log.d(TAG, "Skipping - no primary clip")
            return
        }

        val clipLabel = clipboard.primaryClip?.description?.label?.toString() ?: ""
        Log.d(TAG, "Clip label: '$clipLabel'")
        if (clipLabel.contains("PureLink", ignoreCase = true)) {
            Log.d(TAG, "Skipping - already cleaned by PureLink")
            return
        }

        val item = try {
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

        // Use UrlCleaner to handle mixed text (multiple URLs in one block)
        val cleaned = UrlCleaner.cleanMixedText(text)

        if (cleaned == text) {
            Log.d(TAG, "Skipping - no changes needed")
            return
        }

        Log.d(TAG, "Found dirty URLs. Cleaning...")
        Log.d(TAG, "Original: $text")
        Log.d(TAG, "Cleaned:  $cleaned")

        isProcessing = true
        Log.d(TAG, "Setting isProcessing = true")

        try {
            val newClip = ClipData.newPlainText("Cleaned by PureLink", cleaned)
            clipboard.setPrimaryClip(newClip)
            Log.d(TAG, "Clipboard updated with cleaned content")
            
            notifyUser()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update clipboard", e)
        }

        // Reset flag after delay
        handler.postDelayed({
            isProcessing = false
            Log.d(TAG, "Reset isProcessing = false")
        }, 500)
    }

    private fun notifyUser() {
        val prefs = getSharedPreferences("PureLinkPrefs", MODE_PRIVATE)
        val shouldVibrate = prefs.getBoolean("vibrate", true)
        val shouldToast = prefs.getBoolean("toast", true)

        if (shouldVibrate) {
            vibrator.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE))
        }

        if (shouldToast) {
            Toast.makeText(this, getString(R.string.toast_cleaned), Toast.LENGTH_SHORT).show()
        }
    }
}