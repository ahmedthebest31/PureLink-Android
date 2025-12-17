package com.ahmedsamy

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
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
    private lateinit var txtStatusTitle: TextView
    private lateinit var btnPauseResume: Button
    private lateinit var inputField: EditText
    private var cleanCount = 0
    
    // مراقب التغييرات (عشان لو غيرت من الستارة التطبيق يحس)
    private val prefsListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == "monitoring_active") {
            updateStatusUI()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        prefs = getSharedPreferences("PureLinkPrefs", Context.MODE_PRIVATE)
        prefs.registerOnSharedPreferenceChangeListener(prefsListener) // تفعيل المراقب
        
        cleanCount = prefs.getInt("stats_count", 0)

        // تعريف العناصر
        inputField = findViewById(R.id.inputField)
        txtToolbarStats = findViewById(R.id.txtToolbarStats)
        txtStatusTitle = findViewById(R.id.txtStatusTitle)
        btnPauseResume = findViewById(R.id.btnPauseResume)
        
        val btnPaste = findViewById<Button>(R.id.btnPaste)
        val btnClean = findViewById<Button>(R.id.btnClean)
        val switchService = findViewById<Switch>(R.id.switchService)
        val switchUnshorten = findViewById<Switch>(R.id.switchUnshorten)
        val switchVibrate = findViewById<Switch>(R.id.switchVibrate)
        val switchToast = findViewById<Switch>(R.id.switchToast)
        val cardService = findViewById<LinearLayout>(R.id.cardService)
        
        updateStatsUI()
        updateStatusUI()

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
        
        // زرار التوقيف المؤقت (Pause Protection)
        btnPauseResume.setOnClickListener {
            val isActive = prefs.getBoolean("monitoring_active", true)
            prefs.edit().putBoolean("monitoring_active", !isActive).apply()
            // الـ Listener هيحدث الواجهة لوحده
        }

        // زرار الخدمة الرئيسي (Flow: Battery -> Accessibility)
        val serviceAction = {
            if (!isBatteryOptimized()) {
                // الخطوة 1: طلب البطارية
                askForBatteryOptimization()
            } else if (!isAccessibilityServiceEnabled()) {
                // الخطوة 2: طلب إمكانية الوصول
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                Toast.makeText(this, "Enable 'PureLink' Service", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Service is already Running ✅", Toast.LENGTH_SHORT).show()
            }
        }

        cardService.setOnClickListener { serviceAction() }
        switchService.setOnClickListener { serviceAction() }

        switchUnshorten.setOnCheckedChangeListener { _, v -> prefs.edit().putBoolean("unshorten", v).apply() }
        switchVibrate.setOnCheckedChangeListener { _, v -> prefs.edit().putBoolean("vibrate", v).apply() }
        switchToast.setOnCheckedChangeListener { _, v -> prefs.edit().putBoolean("toast", v).apply() }

        handleIncomingIntent(intent)
    }
    
    // تحديث شكل الحالة (Active/Paused)
    private fun updateStatusUI() {
        val isActive = prefs.getBoolean("monitoring_active", true)
        if (isActive) {
            txtStatusTitle.text = "Status: Active 🛡️"
            txtStatusTitle.setTextColor(0xFF00FF00.toInt()) // Green
            txtStatusTitle.contentDescription = "Status: Active"
            btnPauseResume.text = "PAUSE"
            btnPauseResume.background.setTint(0xFF333333.toInt())
        } else {
            txtStatusTitle.text = "Status: Paused ⏸️"
            txtStatusTitle.setTextColor(0xFFAAAAAA.toInt()) // Gray
            txtStatusTitle.contentDescription = "Status: Paused"
            btnPauseResume.text = "RESUME"
            btnPauseResume.background.setTint(0xFF006600.toInt())
        }
    }

    private fun isBatteryOptimized(): Boolean {
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        return pm.isIgnoringBatteryOptimizations(packageName)
    }

    private fun askForBatteryOptimization() {
        try {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            intent.data = Uri.parse("package:" + packageName)
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Battery optimization not available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun finalizeProcess(text: String) {
        inputField.setText(text)
        copyToClipboard(text)
        incrementStats()
        Toast.makeText(this, "DONE! 🚀", Toast.LENGTH_SHORT).show()
    }
    
    // ... باقي الدوال (القائمة، التنظيف، الخ) زي ما هي ...
    override fun onCreateOptionsMenu(menu: Menu?): Boolean { menuInflater.inflate(R.menu.main_menu, menu); return true }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val text = inputField.text.toString().trim()
        when (item.itemId) {
            R.id.action_whatsapp -> {
                 if (text.isEmpty()) Toast.makeText(this, "Paste number!", Toast.LENGTH_SHORT).show()
                 else {
                     val number = text.replace("+", "").replace(" ", "").replace("-", "")
                     try { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/" + number))) } catch(e:Exception){}
                 }
            }
            R.id.action_telegram -> {
                if (text.isEmpty()) Toast.makeText(this, "Paste username!", Toast.LENGTH_SHORT).show()
                else try { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/" + text.replace("@", "")))) } catch(e:Exception){}
            }
            R.id.action_b64_encode -> {
                if (text.isNotEmpty()) { val e = Base64.encodeToString(text.toByteArray(), Base64.NO_WRAP); inputField.setText(e); copyToClipboard(e) }
            }
            R.id.action_b64_decode -> {
                try { if (text.isNotEmpty()) { val d = String(Base64.decode(text, Base64.NO_WRAP)); inputField.setText(d); copyToClipboard(d) } } catch(e:Exception){}
            }
            R.id.action_uuid -> { val u = UUID.randomUUID().toString(); inputField.setText(u); copyToClipboard(u) }
            R.id.action_about -> { AlertDialog.Builder(this).setTitle("About").setMessage("PureLink v3.0\nSafe & Native.\nAhmed Samy.").setPositiveButton("OK", null).show() }
        }
        return true
    }

    private fun updateStatsUI() {
        txtToolbarStats.text = cleanCount.toString()
        txtToolbarStats.contentDescription = "Cleaned: " + cleanCount + " Items"
    }
    private fun incrementStats() { cleanCount++; prefs.edit().putInt("stats_count", cleanCount).apply(); updateStatsUI() }
    private fun resolveUrl(shortUrl: String): String {
        try {
            var u = shortUrl.trim(); if (!u.startsWith("http")) u="https://"+u
            val c = URL(u).openConnection() as HttpURLConnection
            c.setRequestProperty("User-Agent", "Mozilla/5.0"); c.instanceFollowRedirects=false
            c.connect(); var loc = c.getHeaderField("Location"); if(loc==null) loc=c.url.toString()
            return if(loc.isNotEmpty()) loc else shortUrl
        } catch(e:Exception){return shortUrl}
    }
    private fun copyToClipboard(text: String) { (getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText("PureLink", text)) }
    private fun cleanUrl(url: String): String {
        var r = url; val p = Regex("([?&](utm_[^=&]+|fbclid|gclid|ref|s|si)=[^&]*)"); r = p.replace(r, "")
        if (r.endsWith("?") || r.endsWith("&")) r = r.dropLast(1)
        return r
    }
    override fun onNewIntent(intent: Intent?) { super.onNewIntent(intent); handleIncomingIntent(intent) }
    private fun handleIncomingIntent(intent: Intent?) {
        if (intent==null) return; val s = intent.getStringExtra(Intent.EXTRA_TEXT); if(s!=null) finalizeProcess(cleanUrl(s))
    }
    private fun isAccessibilityServiceEnabled(): Boolean {
        val p = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        return p != null && p.contains(packageName + "/" + packageName + ".ClipboardService")
    }
    override fun onResume() { 
        super.onResume()
        findViewById<Switch>(R.id.switchService).isChecked = isAccessibilityServiceEnabled()
        // تحديث حالة البطارية في الواجهة لو حبيت، بس السويتش كفاية
    }
    override fun onDestroy() {
        super.onDestroy()
        prefs.unregisterOnSharedPreferenceChangeListener(prefsListener)
    }
}
