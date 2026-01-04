package com.ahmedsamy.purelink

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import com.ahmedsamy.purelink.ui.MainScreen
import com.ahmedsamy.purelink.ui.theme.PureLinkTheme

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("PureLinkPrefs", MODE_PRIVATE)
        val clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager

        viewModel =
            ViewModelProvider(this, MainViewModel.provideFactory(prefs, clipboardManager))[
                MainViewModel::class.java]

        setContent {
            PureLinkTheme {
                MainScreen(
                    viewModel = viewModel,
                    onServiceClick = ::handleServiceClick,
                    onWhatsAppClick = ::openWhatsApp,
                    onTelegramClick = ::openTelegram,
                    onAboutClick = ::showAboutDialog
                )
            }
        }

        handleIncomingIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        viewModel.updateServiceEnabled(isAccessibilityServiceEnabled())
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        val text = intent?.getStringExtra(Intent.EXTRA_TEXT)
        viewModel.handleIncomingText(text)
    }

    private fun handleServiceClick() {
        if (!isBatteryOptimized()) {
            askForBatteryOptimization()
        } else if (!isAccessibilityServiceEnabled()) {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            Toast.makeText(this, "Enable 'PureLink' Service", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Service is already Running ✅", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isBatteryOptimized(): Boolean {
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        return pm.isIgnoringBatteryOptimizations(packageName)
    }

    @SuppressLint("BatteryLife")
    private fun askForBatteryOptimization() {
        try {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            intent.data = "package:$packageName".toUri()
            startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(this, "Battery optimization not available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val enabledServices =
            Settings.Secure.getString(
                contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
        return enabledServices != null &&
                enabledServices.contains("$packageName/$packageName.ClipboardService")
    }

    private fun openWhatsApp(text: String) {
        if (text.isEmpty()) {
            Toast.makeText(this, "Paste number!", Toast.LENGTH_SHORT).show()
            return
        }
        val number = text.replace("+", "").replace(" ", "").replace("-", "")
        try {
            startActivity(Intent(Intent.ACTION_VIEW, "https://wa.me/$number".toUri()))
        } catch (_: Exception) {
            Toast.makeText(this, "WhatsApp not available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openTelegram(text: String) {
        if (text.isEmpty()) {
            Toast.makeText(this, "Paste username!", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val username = text.replace("@", "")
            startActivity(Intent(Intent.ACTION_VIEW, "https://t.me/$username".toUri()))
        } catch (_: Exception) {
            Toast.makeText(this, "Telegram not available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAboutDialog() {
        AlertDialog.Builder(this)
            .setTitle("About")
            .setMessage("PureLink v1.0\nSafe & Native.\nAhmed Samy.")
            .setPositiveButton("OK", null)
            .show()
    }
}
