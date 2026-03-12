package com.ahmedsamy.purelink

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.ahmedsamy.purelink.data.HistoryRepository
import com.ahmedsamy.purelink.utils.FeedbackUtils
import com.ahmedsamy.purelink.utils.UrlCleaner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ShareActivity : AppCompatActivity() {

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

        val scope = CoroutineScope(Dispatchers.Main)
        scope.launch {
            try {
                // Get unshorten setting
                val prefs = getSharedPreferences("PureLinkPrefs", Context.MODE_PRIVATE)
                val unshortenEnabled = prefs.getBoolean("unshorten", false)
                
                // Clean & Unshorten
                val result = UrlCleaner.processText(text, unshortenEnabled)
                val cleaned = result.resultText
                
                // Save to history if changed
                if (cleaned != text) {
                     val repo = HistoryRepository(this@ShareActivity)
                     repo.addUrl(cleaned)
                     
                     // Handle PROCESS_TEXT in-place replacement
                     if (intent?.action == Intent.ACTION_PROCESS_TEXT) {
                         val resultIntent = Intent()
                         resultIntent.putExtra(Intent.EXTRA_PROCESS_TEXT, cleaned)
                         setResult(RESULT_OK, resultIntent)
                     }

                     // Copy to clipboard
                     val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                     clipboard.setPrimaryClip(ClipData.newPlainText(getString(R.string.clipboard_label), cleaned))
                     
                     // Show detailed toast
                     val message = if (result.unshortenCount > 0) {
                         getString(R.string.toast_unshortened_plural, result.cleanCount, result.unshortenCount)
                     } else if (result.cleanCount > 0) {
                         getString(R.string.toast_cleaned_plural, result.cleanCount)
                     } else {
                         getString(R.string.toast_cleaned)
                     }
                     FeedbackUtils.showToast(this@ShareActivity, message)
                     FeedbackUtils.performHapticFeedback(this@ShareActivity)
                } else {
                    // Nothing changed
                    if (text.contains("http", ignoreCase = true)) {
                        FeedbackUtils.showToast(this@ShareActivity, getString(R.string.toast_nothing_to_clean))
                    } else {
                        FeedbackUtils.showToast(this@ShareActivity, getString(R.string.toast_no_links))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                finish()
            }
        }
    }
}
