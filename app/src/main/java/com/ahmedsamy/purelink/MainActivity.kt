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

        // Passed resources to ViewModel for string resolution
        viewModel =
            ViewModelProvider(this, MainViewModel.provideFactory(prefs, clipboardManager, resources))[
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
            Toast.makeText(this, getString(R.string.service_enable_prompt), Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, getString(R.string.service_already_running), Toast.LENGTH_SHORT).show()
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
            Toast.makeText(this, getString(R.string.battery_opt_unavailable), Toast.LENGTH_SHORT).show()
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
            Toast.makeText(this, getString(R.string.toast_paste_number), Toast.LENGTH_SHORT).show()
            return
        }
        val number = text.replace("+", "").replace(" ", "").replace("-", "")
        try {
            startActivity(Intent(Intent.ACTION_VIEW, "https://wa.me/$number".toUri()))
        } catch (_: Exception) {
            Toast.makeText(this, getString(R.string.toast_whatsapp_unavailable), Toast.LENGTH_SHORT).show()
        }
    }

    private fun openTelegram(text: String) {
        if (text.isEmpty()) {
            Toast.makeText(this, getString(R.string.toast_paste_username), Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val username = text.replace("@", "")
            startActivity(Intent(Intent.ACTION_VIEW, "https://t.me/$username".toUri()))
        } catch (_: Exception) {
            Toast.makeText(this, getString(R.string.toast_telegram_unavailable), Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAboutDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.about_title))
            .setMessage(getString(R.string.about_message))
            .setPositiveButton(getString(R.string.btn_ok), null)
            .show()
    }
}