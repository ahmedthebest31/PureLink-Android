package com.ahmedsamy.purelink

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Resources
import android.net.Uri
import android.util.Base64
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ahmedsamy.purelink.data.HistoryItem
import com.ahmedsamy.purelink.data.HistoryRepository
import com.ahmedsamy.purelink.data.SettingsRepository
import com.ahmedsamy.purelink.utils.UrlCleaner
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.ahmedsamy.purelink.utils.FeedbackUtils

data class MainUiState(
        val isMonitoringActive: Boolean = true,
        val cleanCount: Int = 0,
        val inputText: String = "",
        val isServiceEnabled: Boolean = false,
        val unshortenEnabled: Boolean = false,
        val vibrateEnabled: Boolean = true,
        val toastEnabled: Boolean = true,
        val isResolving: Boolean = false,
        val toastMessage: ToastMessage? = null,
        val updateStatus: UpdateStatus = UpdateStatus.IDLE,
        val showOnboardingAlert: Boolean = false,
        val selectedLanguage: String = ""
)

sealed class ToastMessage {
    data class Resource(val resId: Int, val args: List<Any> = emptyList()) : ToastMessage()
    data class Literal(val message: String) : ToastMessage()
}

enum class UpdateStatus {
    IDLE, LOADING, SUCCESS, ERROR
}
class MainViewModel(
        private val prefs: SharedPreferences,
        private val settingsRepository: SettingsRepository,
        private val clipboardManager: ClipboardManager,
        private val historyRepository: HistoryRepository,
        private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val _historyList = MutableStateFlow<List<HistoryItem>>(emptyList())
    val historyList: StateFlow<List<HistoryItem>> = _historyList.asStateFlow()

    private val prefsListener =
            SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                if (key == "monitoring_active") {
                    _uiState.update {
                        it.copy(isMonitoringActive = prefs.getBoolean("monitoring_active", true))
                    }
                }
            }

    init {
        loadInitialState()
        applyInitialLocale()
        loadHistory()
        checkOnboarding()
        viewModelScope.launch {
             UrlCleaner.reloadRules(context)
        }
        prefs.registerOnSharedPreferenceChangeListener(prefsListener)
    }

    private fun applyInitialLocale() {
        val lang = settingsRepository.getLanguage()
        if (lang.isNotEmpty()) {
            val appLocale = LocaleListCompat.forLanguageTags(lang)
            AppCompatDelegate.setApplicationLocales(appLocale)
        }
    }
    
    private fun checkOnboarding() {
        val hasSeen = settingsRepository.hasSeenOnboarding()
        _uiState.update { it.copy(showOnboardingAlert = !hasSeen) }
    }

    fun markOnboardingSeen() {
        settingsRepository.setOnboardingSeen()
        _uiState.update { it.copy(showOnboardingAlert = false) }
    }

    fun updateRules() {
        _uiState.update { it.copy(updateStatus = UpdateStatus.LOADING) }
        viewModelScope.launch {
            val repo = com.ahmedsamy.purelink.data.RulesRepository(context)
            val success = repo.fetchAndSaveRules()
            if (success) {
                UrlCleaner.reloadRules(context)
                _uiState.update { it.copy(updateStatus = UpdateStatus.SUCCESS) }
            } else {
                _uiState.update { it.copy(updateStatus = UpdateStatus.ERROR) }
            }
        }
    }

    fun resetUpdateStatus() {
        _uiState.update { it.copy(updateStatus = UpdateStatus.IDLE) }
    }

    private fun loadInitialState() {
        _uiState.update {
            it.copy(
                    isMonitoringActive = prefs.getBoolean("monitoring_active", true),
                    cleanCount = prefs.getInt("stats_count", 0),
                    unshortenEnabled = settingsRepository.isUnshortenEnabled(),
                    vibrateEnabled = settingsRepository.isVibrateEnabled(),
                    toastEnabled = settingsRepository.isToastEnabled(),
                    selectedLanguage = settingsRepository.getLanguage()
            )
        }
    }

    fun setLanguage(lang: String) {
        settingsRepository.setLanguage(lang)
        _uiState.update { it.copy(selectedLanguage = lang) }
        
        val appLocale: LocaleListCompat = if (lang.isEmpty()) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(lang)
        }
        AppCompatDelegate.setApplicationLocales(appLocale)
    }

    private fun loadHistory() {
        viewModelScope.launch {
            _historyList.value = historyRepository.getRecentHistory()
        }
    }

    fun updateServiceEnabled(enabled: Boolean) {
        _uiState.update { it.copy(isServiceEnabled = enabled) }
    }

    fun toggleMonitoring() {
        val newActive = !_uiState.value.isMonitoringActive
        prefs.edit { putBoolean("monitoring_active", newActive) }
    }

    fun updateInputText(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun pasteFromClipboard() {
        val clip = clipboardManager.primaryClip
        if (clip != null && clip.itemCount > 0) {
            val text = clip.getItemAt(0).text?.toString() ?: ""
            _uiState.update { it.copy(inputText = text) }
        }
    }

    fun executeClean() {
        val text = _uiState.value.inputText
        if (text.isEmpty()) return

        _uiState.update {
            it.copy(isResolving = true, toastMessage = ToastMessage.Resource(R.string.btn_resolving))
        }

        viewModelScope.launch {
            try {
                val result = UrlCleaner.processText(text, _uiState.value.unshortenEnabled)
                finalizeClean(result, text)
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(isResolving = false) }
                showToast(ToastMessage.Literal("Error: ${e.message}"))
            }
        }
    }

    private fun finalizeClean(result: UrlCleaner.ProcessingResult, originalText: String) {
        val cleanedText = result.resultText
        _uiState.update { it.copy(inputText = cleanedText, isResolving = false) }
        
        if (cleanedText != originalText) {
            copyToClipboard(cleanedText)
            
            if (result.cleanCount > 0 || result.unshortenCount > 0) {
                incrementStats()
                viewModelScope.launch {
                    try {
                        historyRepository.addUrl(cleanedText)
                        loadHistory()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                
                if (result.unshortenCount > 0) {
                    showToast(ToastMessage.Resource(R.string.toast_unshortened_plural, listOf(result.cleanCount, result.unshortenCount)))
                } else {
                    showToast(ToastMessage.Resource(R.string.toast_cleaned_plural, listOf(result.cleanCount)))
                }
            } else {
                 // Technically shouldn't happen if cleanedText != originalText but for safety
                showToast(ToastMessage.Resource(R.string.toast_cleaned))
            }
        } else {
            // Text is the same. Was there a URL at all?
            if (originalText.contains("http", ignoreCase = true)) {
                showToast(ToastMessage.Resource(R.string.toast_nothing_to_clean))
            } else {
                showToast(ToastMessage.Resource(R.string.toast_no_links))
            }
        }
    }

    private fun incrementStats() {
        val newCount = _uiState.value.cleanCount + 1
        prefs.edit { putInt("stats_count", newCount) }
        _uiState.update { it.copy(cleanCount = newCount) }
    }

    fun setUnshortenEnabled(enabled: Boolean) {
        settingsRepository.setUnshortenEnabled(enabled)
        _uiState.update { it.copy(unshortenEnabled = enabled) }
    }

    fun setVibrateEnabled(enabled: Boolean) {
        settingsRepository.setVibrateEnabled(enabled)
        _uiState.update { it.copy(vibrateEnabled = enabled) }
    }

    fun setToastEnabled(enabled: Boolean) {
        settingsRepository.setToastEnabled(enabled)
        _uiState.update { it.copy(toastEnabled = enabled) }
    }

    fun encodeBase64() {
        val text = _uiState.value.inputText.trim()
        if (text.isNotEmpty()) {
            val encoded = Base64.encodeToString(text.toByteArray(), Base64.NO_WRAP)
            _uiState.update { it.copy(inputText = encoded) }
            copyToClipboard(encoded)
        }
    }

    fun decodeBase64() {
        val text = _uiState.value.inputText.trim()
        if (text.isNotEmpty()) {
            try {
                val decoded = String(Base64.decode(text, Base64.NO_WRAP))
                _uiState.update { it.copy(inputText = decoded) }
                copyToClipboard(decoded)
            } catch (_: Exception) {
                showToast(ToastMessage.Resource(R.string.toast_base64_invalid))
            }
        }
    }

    fun generateUuid() {
        val uuid = UUID.randomUUID().toString()
        _uiState.update { it.copy(inputText = uuid) }
        copyToClipboard(uuid)
    }

    fun handleIncomingText(text: String?) {
        if (!text.isNullOrEmpty()) {
            viewModelScope.launch {
                val result = UrlCleaner.processText(text, settingsRepository.isUnshortenEnabled())
                finalizeClean(result, text)
            }
        }
    }

    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }

    private fun showToast(message: ToastMessage) {
        _uiState.update { it.copy(toastMessage = message) }
    }

    fun copyToClipboard(text: String) {
        clipboardManager.setPrimaryClip(ClipData.newPlainText(context.getString(R.string.clipboard_label), text))
        FeedbackUtils.performHapticFeedback(context)
        showToast(ToastMessage.Resource(R.string.toast_copied))
    }
    
    fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: android.content.ActivityNotFoundException) {
            showToast(ToastMessage.Resource(R.string.toast_open_browser_failed))
        } catch (e: Exception) {
             showToast(ToastMessage.Resource(R.string.toast_open_failed))
        }
    }

    fun shareApp() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, context.getString(R.string.share_text))
        }
        val chooser = Intent.createChooser(intent, null)
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    fun rateApp() {
        val packageName = context.packageName
        val uri = Uri.parse("market://details?id=$packageName")
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(goToMarket)
        } catch (e: Exception) {
            val webUri = Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
            val goToWeb = Intent(Intent.ACTION_VIEW, webUri)
            goToWeb.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(goToWeb)
        }
    }

    fun openRepo() {
        openUrl("https://github.com/ahmedsamy/PureLink-Android")
    }

    fun openPayPal() {
        openUrl("https://www.paypal.com/paypalme/ahmedthebest31")
    }

    fun openInstaPay() {
        openUrl("https://ipn.eg/S/ahmedthebest/instapay/63TO4s")
    }

    override fun onCleared() {
        super.onCleared()
        prefs.unregisterOnSharedPreferenceChangeListener(prefsListener)
    }

    companion object {
        fun provideFactory(
                context: Context,
                prefs: SharedPreferences,
                clipboardManager: ClipboardManager
        ): ViewModelProvider.Factory =
                object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        val historyRepository = HistoryRepository(context)
                        val settingsRepository = SettingsRepository(context)
                        return MainViewModel(prefs, settingsRepository, clipboardManager, historyRepository, context) as T
                    }
                }
    }
}
