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

class MainActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var txtStats: TextView
    private var cleanCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // إعداد التولبار
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        prefs = getSharedPreferences("PureLinkPrefs", Context.MODE_PRIVATE)
        cleanCount = prefs.getInt("stats_count", 0)

        val inputField = findViewById<EditText>(R.id.inputField)
        val btnPaste = findViewById<Button>(R.id.btnPaste)
        val btnClean = findViewById<Button>(R.id.btnClean)
        val switchService = findViewById<Switch>(R.id.switchService)
        val switchVibrate = findViewById<Switch>(R.id.switchVibrate)
        val switchToast = findViewById<Switch>(R.id.switchToast)
        val cardService = findViewById<LinearLayout>(R.id.cardService)
        txtStats = findViewById(R.id.txtStats)
        
        updateStatsUI()

        // استرجاع الإعدادات
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
                val cleaned = cleanUrl(text)
                inputField.setText(cleaned)
                copyToClipboard(cleaned)
                incrementStats()
                Toast.makeText(this, "CLEANED & COPIED", Toast.LENGTH_SHORT).show()
            }
        }

        cardService.setOnClickListener { showExplanationDialog() }
        switchService.setOnClickListener {
            showExplanationDialog()
            // التحقق المؤقت
            switchService.isChecked = isAccessibilityServiceEnabled()
        }

        switchVibrate.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("vibrate", isChecked).apply()
        }
        switchToast.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("toast", isChecked).apply()
        }

        // معالجة الـ Intent (Share & Process Text)
        handleIncomingIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent == null) return
        
        val action = intent.action
        val type = intent.type

        if (Intent.ACTION_SEND == action && type == "text/plain") {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (sharedText != null) {
                processAndFinish(sharedText)
            }
        }
        
        if (Intent.ACTION_PROCESS_TEXT == action && type == "text/plain") {
            val sharedText = intent.getStringExtra(Intent.EXTRA_PROCESS_TEXT) ?: 
                             intent.getStringExtra(Intent.EXTRA_TEXT)
            if (sharedText != null) {
                processAndFinish(sharedText)
            }
        }
    }

    private fun processAndFinish(text: String) {
        val cleaned = cleanUrl(text)
        copyToClipboard(cleaned)
        incrementStats()
        Toast.makeText(this, "PureLink: Cleaned & Copied! 🚀", Toast.LENGTH_LONG).show()
        // وضع النص في الواجهة للمشاهدة
        findViewById<EditText>(R.id.inputField).setText(cleaned)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_about -> {
                AlertDialog.Builder(this)
                    .setTitle("About")
                    .setMessage("Developed by Ahmed Samy.\n\nDesigned for Blind Developers.\nNative Android / No Libraries.")
                    .setPositiveButton("Cool", null)
                    .show()
            }
            R.id.action_stats -> {
                // استخدام الجمع النصي لتجنب مشاكل علامة الدولار في السكريبت
                val msg = "Total Links Cleaned: " + cleanCount
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            }
        }
        return true
    }

    override fun onResume() {
        super.onResume()
        val switchService = findViewById<Switch>(R.id.switchService)
        switchService.isChecked = isAccessibilityServiceEnabled()
    }

    private fun showExplanationDialog() {
        if (!isAccessibilityServiceEnabled()) {
            AlertDialog.Builder(this)
                .setTitle(R.string.accessibility_title)
                .setMessage(R.string.accessibility_desc)
                .setPositiveButton(R.string.ok) { _, _ ->
                    startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        } else {
            Toast.makeText(this, "Monitor is Active ✅", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val prefString = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        // بناء اسم الخدمة يدوياً لتجنب مشاكل الترمنال
        val serviceId = packageName + "/" + packageName + ".ClipboardService"
        return prefString != null && prefString.contains(serviceId)
    }

    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Cleaned", text))
    }

    private fun incrementStats() {
        cleanCount++
        prefs.edit().putInt("stats_count", cleanCount).apply()
        updateStatsUI()
    }

    private fun updateStatsUI() {
        txtStats.text = cleanCount.toString()
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
