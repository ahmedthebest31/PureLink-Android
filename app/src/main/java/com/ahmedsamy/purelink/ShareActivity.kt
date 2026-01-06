package com.ahmedsamy.purelink

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.ahmedsamy.purelink.data.HistoryRepository
import com.ahmedsamy.purelink.utils.FeedbackUtils
import com.ahmedsamy.purelink.utils.UrlCleaner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ShareActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val text = intent?.getStringExtra(Intent.EXTRA_TEXT) 
            ?: intent?.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString()

        if (text.isNullOrBlank()) {
            finish()
            return
        }

        // Clean
        val cleaned = UrlCleaner.cleanMixedText(text)

        // Save to History & Copy
        // Using Main scope to update UI (Toast) and access system services, 
        // repository handles its own IO switching.
        val scope = CoroutineScope(Dispatchers.Main)
        scope.launch {
            try {
                // Save to history if it looks like a URL
                if (cleaned.contains("http", ignoreCase = true)) {
                     val repo = HistoryRepository(this@ShareActivity)
                     repo.addUrl(cleaned)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Copy to clipboard
            val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("Cleaned by PureLink", cleaned))

            FeedbackUtils.showToast(this@ShareActivity, getString(R.string.toast_cleaned))
            
            finish()
        }
    }
}
