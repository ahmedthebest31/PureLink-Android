package com.ahmedsamy

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var txtStats: TextView
    private lateinit var inputField: EditText
    private var cleanCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        prefs = getSharedPreferences("PureLinkPrefs", Context.MODE_PRIVATE)
        cleanCount = prefs.getInt("stats_count", 0)

        inputField = findViewById(R.id.inputField)
        val btnPaste = findViewById<Button>(R.id.btnPaste)
        val btnClean = findViewById<Button>(R.id.btnClean)
        val switchService = findViewById<Switch>(R.id.switchService)
        val switchUnshorten = findViewById<Switch>(R.id.switchUnshorten)
        val switchVibrate = findViewById<Switch>(R.id.switchVibrate)
        val switchToast = findViewById<Switch>(R.id.switchToast)
        val cardService = findViewById<LinearLayout>(R.id.cardService)
        txtStats = findViewById(R.id.txtStats)
        
        updateStatsUI()

        switchUnshorten.isChecked = prefs.getBoolean("unshorten", false)
        switchVibrate.isChecked = prefs.getBoolean("vibrate", true)
        switchToast.isChecked = prefs.getBoolean("toast", true)

        btnPaste.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = clipboard.primaryClip
            if (clip != null && clip.itemCount > 0) {
                inputField.setText(clip.getItemAt(0).text)
            }
        }

        btnClean.setOnClickListener {
            val text = inputField.text.toString()
            if (text.isNotEmpty()) {
                val shouldUnshorten = switchUnshorten.isChecked
                // التأكد إنه رابط عشان مانضيعش وقت
                if (shouldUnshorten && text.contains("http")) {
                    Toast.makeText(this, "Resolving URL... ⏳", Toast.LENGTH_SHORT).show()
                    thread {
                        val resolvedUrl = resolveUrl(text)
                        val cleaned = cleanUrl(resolvedUrl)
                        runOnUiThread {
                            finalizeProcess(cleaned)
                            if (resolvedUrl == text) {
                                // لو الرابط متغيرش، عرف المستخدم
                                Toast.makeText(this, "Could not expand URL (or it is original)", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    val cleaned = cleanUrl(text)
                    finalizeProcess(cleaned)
                }
            }
        }

        cardService.setOnClickListener { showExplanationDialog() }
        switchService.setOnClickListener {
            showExplanationDialog()
            switchService.isChecked = isAccessibilityServiceEnabled()
        }

        switchUnshorten.setOnCheckedChangeListener { _, isChecked -> prefs.edit().putBoolean("unshorten", isChecked).apply() }
        switchVibrate.setOnCheckedChangeListener { _, isChecked -> prefs.edit().putBoolean("vibrate", isChecked).apply() }
        switchToast.setOnCheckedChangeListener { _, isChecked -> prefs.edit().putBoolean("toast", isChecked).apply() }

        handleIncomingIntent(intent)
    }

    private fun finalizeProcess(cleanedText: String) {
        inputField.setText(cleanedText)
        copyToClipboard(cleanedText)
        incrementStats()
        Toast.makeText(this, "DONE! 🚀", Toast.LENGTH_SHORT).show()
    }

    // الدالة المحسنة لفك الروابط (مع التنكر)
    private fun resolveUrl(shortUrl: String): String {
        try {
            var urlStr = shortUrl.trim()
            if (!urlStr.startsWith("http")) urlStr = "https://" + urlStr
            
            val url = URL(urlStr)
            val connection = url.openConnection() as HttpURLConnection
            
            // 1. التنكر كمتصفح ديسكتوب عشان السيرفر يحترمنا
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
            
            // 2. تتبع التحويلات
            connection.instanceFollowRedirects = false // هنتبعها يدوياً لو احتجنا، أو نخليها true بس الـ UserAgent هو الأهم
            connection.connectTimeout = 8000 // زودنا الوقت لـ 8 ثواني
            connection.readTimeout = 8000
            
            // خدعة: لو الموقع مرجعش رابط جديد، يبقى هو الرابط الأصلي
            // بنستخدم GET بدل HEAD عشان بعض المواقع بترفض HEAD
            connection.requestMethod = "GET" 
            connection.connect()
            
            var expandedUrl = connection.getHeaderField("Location")
            if (expandedUrl == null) {
                expandedUrl = connection.url.toString()
            }
            
            connection.disconnect()
            
            // لو الرابط الجديد نسبي (بيبدأ بـ /) نركبه على الدومين الأصلي
            if (expandedUrl.startsWith("/")) {
                 expandedUrl = "https://" + url.host + expandedUrl
            }
            
            return if (expandedUrl.isNotEmpty()) expandedUrl else shortUrl

        } catch (e: Exception) {
            e.printStackTrace()
            return shortUrl // لو فشل، رجع القديم
        }
    }

    // باقي الدوال المساعدة...
    private fun isUrl(text: String): Boolean { return text.contains(".") && (text.startsWith("http") || text.contains("www")) }

    override fun onNewIntent(intent: Intent?) { super.onNewIntent(intent); handleIncomingIntent(intent) }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent == null) return
        val action = intent.action
        val type = intent.type
        if (Intent.ACTION_SEND == action && type == "text/plain") {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (sharedText != null) processAndFinish(sharedText)
        }
        if (Intent.ACTION_PROCESS_TEXT == action && type == "text/plain") {
            val sharedText = intent.getStringExtra(Intent.EXTRA_PROCESS_TEXT) ?: intent.getStringExtra(Intent.EXTRA_TEXT)
            if (sharedText != null) processAndFinish(sharedText)
        }
    }

    private fun processAndFinish(text: String) {
        val cleaned = cleanUrl(text)
        copyToClipboard(cleaned)
        incrementStats()
        Toast.makeText(this, "Cleaned & Copied!", Toast.LENGTH_LONG).show()
        inputField.setText(cleaned)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean { menuInflater.inflate(R.menu.main_menu, menu); return true }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_about -> { AlertDialog.Builder(this).setTitle("About").setMessage("PureLink v2.2\nBot-Proof Resolver.\n\nAhmed Samy.").setPositiveButton("OK", null).show() }
            R.id.action_stats -> { Toast.makeText(this, "Total: " + cleanCount, Toast.LENGTH_SHORT).show() }
        }
        return true
    }

    override fun onResume() { super.onResume(); findViewById<Switch>(R.id.switchService).isChecked = isAccessibilityServiceEnabled() }

    private fun showExplanationDialog() {
        if (!isAccessibilityServiceEnabled()) {
            AlertDialog.Builder(this).setTitle(R.string.accessibility_title).setMessage(R.string.accessibility_desc).setPositiveButton(R.string.ok) { _, _ -> startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }.setNegativeButton(R.string.cancel, null).show()
        } else { Toast.makeText(this, "Monitor Active", Toast.LENGTH_SHORT).show() }
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val prefString = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        val serviceId = packageName + "/" + packageName + ".ClipboardService"
        return prefString != null && prefString.contains(serviceId)
    }

    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Cleaned", text))
    }

    private fun incrementStats() { cleanCount++; prefs.edit().putInt("stats_count", cleanCount).apply(); updateStatsUI() }
    private fun updateStatsUI() { txtStats.text = cleanCount.toString() }

    private fun cleanUrl(url: String): String {
        var result = url
        val trackingPattern = Regex("([?&](utm_[^=&]+|fbclid|gclid|ref|s|si)=[^&]*)")
        result = trackingPattern.replace(result, "")
        if (result.endsWith("?") || result.endsWith("&")) result = result.dropLast(1)
        return result
    }
}
