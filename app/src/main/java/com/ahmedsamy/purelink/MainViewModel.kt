package com.ahmedsamy.purelink

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Resources
import android.net.Uri
import android.util.Base64
import androidx.core.content.edit
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

data class MainUiState(
        val isMonitoringActive: Boolean = true,
        val cleanCount: Int = 0,
        val inputText: String = "",
        val isServiceEnabled: Boolean = false,
        val unshortenEnabled: Boolean = false,
        val vibrateEnabled: Boolean = true,
        val toastEnabled: Boolean = true,
        val isResolving: Boolean = false,
        val toastMessage: String? = null,
        val updateStatus: UpdateStatus = UpdateStatus.IDLE,
        val showOnboardingAlert: Boolean = false
)

enum class UpdateStatus {
    IDLE, LOADING, SUCCESS, ERROR
}

class MainViewModel(
        private val prefs: SharedPreferences, // Kept for stats/monitoring_active legacy if needed, or move all to repo
        private val settingsRepository: SettingsRepository,
        private val clipboardManager: ClipboardManager,
        private val resources: Resources,
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
        loadHistory()
        checkOnboarding()
        viewModelScope.launch {
             UrlCleaner.reloadRules(context)
        }
        prefs.registerOnSharedPreferenceChangeListener(prefsListener)
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
                    toastEnabled = settingsRepository.isToastEnabled()
            )
        }
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
            it.copy(isResolving = true, toastMessage = resources.getString(R.string.btn_resolving))
        }

        viewModelScope.launch {
            try {
                val processed = UrlCleaner.processText(text, _uiState.value.unshortenEnabled)
                finalizeClean(processed)
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(isResolving = false) }
                showToast("Error: ${e.message}")
            }
        }
    }

    private fun finalizeClean(cleanedText: String) {
        _uiState.update { it.copy(inputText = cleanedText, isResolving = false) }
        copyToClipboard(cleanedText)
        incrementStats()
        
        if (cleanedText.contains("http", ignoreCase = true)) {
             viewModelScope.launch {
                 try {
                     historyRepository.addUrl(cleanedText)
                     loadHistory()
                 } catch (e: Exception) {
                     e.printStackTrace()
                 }
             }
        }
        
        showToast(resources.getString(R.string.toast_done))
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
                showToast(resources.getString(R.string.toast_base64_invalid))
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
            val cleaned = UrlCleaner.cleanMixedText(text)
            _uiState.update { it.copy(inputText = cleaned) }
            copyToClipboard(cleaned)
            incrementStats()
            
             if (cleaned.contains("http", ignoreCase = true)) {
                 viewModelScope.launch {
                     historyRepository.addUrl(cleaned)
                     loadHistory()
                 }
             }
             
            showToast(resources.getString(R.string.toast_done))
        }
    }

    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }

    private fun showToast(message: String) {
        // We still use this internal method for UI state toast messages (non-intrusive),
        // but let's check settings too if it's a "Done" message? 
        // Actually, UI logic uses 'toastMessage' which Compose displays. 
        // We should check setting here before setting state.
        if (settingsRepository.isToastEnabled()) {
             _uiState.update { it.copy(toastMessage = message) }
        }
    }

    fun copyToClipboard(text: String) {
        clipboardManager.setPrimaryClip(ClipData.newPlainText("PureLink", text))
        
        // Haptic feedback logic should be here or triggered via Utils in View
        // Since ViewModel doesn't have View reference easily for haptic, we can use Utils
        // But Utils needs context. We have context.
        if (settingsRepository.isVibrateEnabled()) {
            // Need to use Vibrator service.
             val v = context.getSystemService(Context.VIBRATOR_SERVICE) as android.os.Vibrator
             if (v.hasVibrator()) {
                v.vibrate(android.os.VibrationEffect.createOneShot(50, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
             }
        }
        
        showToast(resources.getString(R.string.toast_copied))
    }
    
    fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
             showToast(resources.getString(R.string.toast_open_failed))
        }
    }

    override fun onCleared() {
        super.onCleared()
        prefs.unregisterOnSharedPreferenceChangeListener(prefsListener)
    }

    companion object {
        fun provideFactory(
                context: Context,
                prefs: SharedPreferences,
                clipboardManager: ClipboardManager,
                resources: Resources
        ): ViewModelProvider.Factory =
                object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        val historyRepository = HistoryRepository(context)
                        val settingsRepository = SettingsRepository(context)
                        return MainViewModel(prefs, settingsRepository, clipboardManager, resources, historyRepository, context) as T
                    }
                }
    }
}
