package com.ahmedsamy.purelink

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var inputField: EditText
    private lateinit var clipboard: ClipboardManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        inputField = findViewById(R.id.inputField)

        val btnPaste = findViewById<Button>(R.id.btnPaste)
        val btnCopy = findViewById<Button>(R.id.btnCopy)
        val btnClean = findViewById<Button>(R.id.btnClean)
        val switchService = findViewById<Switch>(R.id.switchService)

        btnPaste.setOnClickListener {
            val clip = clipboard.primaryClip
            if (clip != null && clip.itemCount > 0) {
                inputField.setText(clip.getItemAt(0).text)
            } else {
                Toast.makeText(this, "Clipboard is empty", Toast.LENGTH_SHORT).show()
            }
        }

        btnCopy.setOnClickListener {
            val text = inputField.text.toString()
            if (text.isNotEmpty()) {
                val clip = ClipData.newPlainText("PureLink", text)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this, "Copied to Clipboard", Toast.LENGTH_SHORT).show()
            }
        }

        btnClean.setOnClickListener {
            val original = inputField.text.toString()
            val cleaned = cleanUrl(original)
            inputField.setText(cleaned)
            Toast.makeText(this, "Link Cleaned", Toast.LENGTH_SHORT).show()
        }

        switchService.setOnClickListener {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
            Toast.makeText(this, "Find PureLink and Turn ON", Toast.LENGTH_LONG).show()
        }

        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (sharedText != null) {
                val cleaned = cleanUrl(sharedText)
                inputField.setText(cleaned)
                val clip = ClipData.newPlainText("PureLink", cleaned)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this, "Cleaned & Copied!", Toast.LENGTH_LONG).show()
            }
        }
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
