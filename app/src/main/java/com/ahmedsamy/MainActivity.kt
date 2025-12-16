package com.ahmedsamy

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Base64
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
import java.util.UUID
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var txtToolbarStats: TextView
    private lateinit var inputField: EditText
    private var cleanCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // إعداد التولبار المخصص
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

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
        
        // هنا التغيير المهم: ربطنا بالعداد الجديد في التولبار
        txtToolbarStats = findViewById(R.id.txtToolbarStats)
        
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
                if (switchUnshorten.isChecked && text.contains("http")) {
                    Toast.makeText(this, "Resolving...", Toast.LENGTH_SHORT).show()
                    thread {
                        val resolved = resolveUrl(text)
                        val cleaned = cleanUrl(resolved)
                        runOnUiThread { finalizeProcess(cleaned) }
                    }
                } else {
                    finalizeProcess(cleanUrl(text))
                }
            }
        }

        cardService.setOnClickListener { showExplanationDialog() }
        switchService.setOnClickListener {
            showExplanationDialog()
            switchService.isChecked = isAccessibilityServiceEnabled()
        }

        switchUnshorten.setOnCheckedChangeListener { _, v -> prefs.edit().putBoolean("unshorten", v).apply() }
        switchVibrate.setOnCheckedChangeListener { _, v -> prefs.edit().putBoolean("vibrate", v).apply() }
        switchToast.setOnCheckedChangeListener { _, v -> prefs.edit().putBoolean("toast", v).apply() }

        handleIncomingIntent(intent)
    }

    private fun finalizeProcess(text: String) {
        inputField.setText(text)
        copyToClipboard(text)
        incrementStats()
        Toast.makeText(this, "DONE! 🚀", Toast.LENGTH_SHORT).show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val text = inputField.text.toString().trim()
        
        when (item.itemId) {
            R.id.action_whatsapp -> {
                if (text.isEmpty()) {
                    Toast.makeText(this, "Paste a number first!", Toast.LENGTH_SHORT).show()
                } else {
                    val number = text.replace("+", "").replace(" ", "").replace("-", "")
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/" + number))
                    try { startActivity(intent) } catch (e: Exception) { Toast.makeText(this, "WhatsApp not installed", Toast.LENGTH_SHORT).show() }
                }
            }
            R.id.action_telegram -> {
                if (text.isEmpty()) {
                    Toast.makeText(this, "Paste a username first!", Toast.LENGTH_SHORT).show()
                } else {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/" + text.replace("@", "")))
                    try { startActivity(intent) } catch (e: Exception) { Toast.makeText(this, "Telegram not installed", Toast.LENGTH_SHORT).show() }
                }
            }
            R.id.action_b64_encode -> {
                if (text.isNotEmpty()) {
                    val encoded = Base64.encodeToString(text.toByteArray(), Base64.NO_WRAP)
                    inputField.setText(encoded)
                    copyToClipboard(encoded)
                    Toast.makeText(this, "Encoded Base64", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.action_b64_decode -> {
                if (text.isNotEmpty()) {
                    try {
                        val decoded = String(Base64.decode(text, Base64.NO_WRAP))
                        inputField.setText(decoded)
                        copyToClipboard(decoded)
                        Toast.makeText(this, "Decoded Base64", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) { Toast.makeText(this, "Invalid Base64", Toast.LENGTH_SHORT).show() }
                }
            }
            R.id.action_uuid -> {
                val uuid = UUID.randomUUID().toString()
                inputField.setText(uuid)
                copyToClipboard(uuid)
                Toast.makeText(this, "UUID Generated", Toast.LENGTH_SHORT).show()
            }
            R.id.action_about -> {
                AlertDialog.Builder(this).setTitle("About").setMessage("PureLink v3.0\nSwiss Army Knife for Devs.\n\nAhmed Samy.").setPositiveButton("OK", null).show()
            }
        }
        return true
    }

    private fun updateStatsUI() {
        txtToolbarStats.text = cleanCount.toString()
        txtToolbarStats.contentDescription = "Cleaned: " + cleanCount + " Items"
    }

    private fun incrementStats() {
        cleanCount++
        prefs.edit().putInt("stats_count", cleanCount).apply()
        updateStatsUI()
    }

    private fun resolveUrl(shortUrl: String): String {
        try {
            var urlStr = shortUrl.trim()
            if (!urlStr.startsWith("http")) urlStr = "https://" + urlStr
            val url = URL(urlStr)
            val connection = url.openConnection() as HttpURLConnection
            connection.setRequestProperty("User-Agent", "Mozilla/5.0")
            connection.instanceFollowRedirects = false
            connection.connectTimeout = 5000
            connection.requestMethod = "GET"
            connection.connect()
            var expanded = connection.getHeaderField("Location")
            if (expanded == null) expanded = connection.url.toString()
            return if (expanded.isNotEmpty()) expanded else shortUrl
        } catch (e: Exception) { return shortUrl }
    }

    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("PureLink", text))
    }

    private fun cleanUrl(url: String): String {
        var result = url
        val trackingPattern = Regex("([?&](utm_[^=&]+|fbclid|gclid|ref|s|si)=[^&]*)")
        result = trackingPattern.replace(result, "")
        if (result.endsWith("?") || result.endsWith("&")) result = result.dropLast(1)
        return result
    }

    override fun onNewIntent(intent: Intent?) { super.onNewIntent(intent); handleIncomingIntent(intent) }
    
    private fun handleIncomingIntent(intent: Intent?) {
        if (intent == null) return
        val action = intent.action
        val type = intent.type
        if (Intent.ACTION_SEND == action && type == "text/plain") {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (sharedText != null) finalizeProcess(cleanUrl(sharedText))
        }
        if (Intent.ACTION_PROCESS_TEXT == action && type == "text/plain") {
            val sharedText = intent.getStringExtra(Intent.EXTRA_PROCESS_TEXT) ?: intent.getStringExtra(Intent.EXTRA_TEXT)
            if (sharedText != null) finalizeProcess(cleanUrl(sharedText))
        }
    }
    
    private fun showExplanationDialog() {
        if (!isAccessibilityServiceEnabled()) {
            AlertDialog.Builder(this).setTitle("Activate").setMessage("Enable Accessibility.").setPositiveButton("Settings") { _, _ -> startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }.setNegativeButton("Cancel", null).show()
        } else { Toast.makeText(this, "Active ✅", Toast.LENGTH_SHORT).show() }
    }
    
    private fun isAccessibilityServiceEnabled(): Boolean {
        val prefString = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        val serviceId = packageName + "/" + packageName + ".ClipboardService"
        return prefString != null && prefString.contains(serviceId)
    }
    
    override fun onResume() { super.onResume(); findViewById<Switch>(R.id.switchService).isChecked = isAccessibilityServiceEnabled() }
}
