package com.ahmedsamy

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // إعداد حفظ البيانات
        prefs = getSharedPreferences("PureLinkPrefs", Context.MODE_PRIVATE)

        val inputField = findViewById<EditText>(R.id.inputField)
        val btnPaste = findViewById<Button>(R.id.btnPaste)
        val btnClean = findViewById<Button>(R.id.btnClean)
        val switchService = findViewById<Switch>(R.id.switchService)
        val switchVibrate = findViewById<Switch>(R.id.switchVibrate)
        val switchToast = findViewById<Switch>(R.id.switchToast)
        val cardService = findViewById<LinearLayout>(R.id.cardService)

        // استرجاع الإعدادات المحفوظة
        switchVibrate.isChecked = prefs.getBoolean("vibrate", true)
        switchToast.isChecked = prefs.getBoolean("toast", true)

        // زرار اللصق
        btnPaste.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = clipboard.primaryClip
            if (clip != null && clip.itemCount > 0) {
                inputField.setText(clip.getItemAt(0).text)
            }
        }

        // زرار التنظيف (للتجربة)
        btnClean.setOnClickListener {
            val text = inputField.text.toString()
            if (text.isNotEmpty()) {
                val cleaned = cleanUrl(text)
                inputField.setText(cleaned)
                // نسخ الرابط النضيف
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("Cleaned", cleaned))
                Toast.makeText(this, "Cleaned & Copied!", Toast.LENGTH_SHORT).show()
            }
        }

        // التعامل مع زر الخدمة (مع شرح)
        cardService.setOnClickListener {
             showExplanationDialog()
        }
        switchService.setOnClickListener {
            showExplanationDialog()
            // بنرجع السويتش لوضعه لحد ما المستخدم يفعل الخدمة بنفسه
            switchService.isChecked = isAccessibilityServiceEnabled()
        }

        // حفظ إعدادات الهزاز
        switchVibrate.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("vibrate", isChecked).apply()
        }

        // حفظ إعدادات التنبيهات
        switchToast.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("toast", isChecked).apply()
        }
    }

    override fun onResume() {
        super.onResume()
        // تحديث حالة زر الخدمة لما نرجع للتطبيق
        val switchService = findViewById<Switch>(R.id.switchService)
        switchService.isChecked = isAccessibilityServiceEnabled()
    }

    // دالة لإظهار رسالة الشرح (Alert Dialog)
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
            Toast.makeText(this, "Service is already Active ✅", Toast.LENGTH_SHORT).show()
        }
    }

    // دالة للتحقق هل الخدمة شغالة ولا لأ
    private fun isAccessibilityServiceEnabled(): Boolean {
        val prefString = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        return prefString != null && prefString.contains("$packageName/$packageName.ClipboardService")
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