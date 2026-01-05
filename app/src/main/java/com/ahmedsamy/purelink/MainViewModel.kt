package com.ahmedsamy.purelink

import android.content.ClipData
import android.content.ClipboardManager
import android.content.SharedPreferences
import android.content.res.Resources
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.core.content.edit
import com.ahmedsamy.purelink.utils.UrlCleaner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

data class MainUiState(
        val isMonitoringActive: Boolean = true,
        val cleanCount: Int = 0,
        val inputText: String = "",
        val isServiceEnabled: Boolean = false,
        val unshortenEnabled: Boolean = false,
        val vibrateEnabled: Boolean = true,
        val toastEnabled: Boolean = true,
        val isResolving: Boolean = false,
        val toastMessage: String? = null
)

class MainViewModel(
        private val prefs: SharedPreferences,
        private val clipboardManager: ClipboardManager,
        private val resources: Resources
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

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
        prefs.registerOnSharedPreferenceChangeListener(prefsListener)
    }

    private fun loadInitialState() {
        _uiState.update {
            it.copy(
                    isMonitoringActive = prefs.getBoolean("monitoring_active", true),
                    cleanCount = prefs.getInt("stats_count", 0),
                    unshortenEnabled = prefs.getBoolean("unshorten", false),
                    vibrateEnabled = prefs.getBoolean("vibrate", true),
                    toastEnabled = prefs.getBoolean("toast", true)
            )
        }
    }

    fun updateServiceEnabled(enabled: Boolean) {
        _uiState.update { it.copy(isServiceEnabled = enabled) }
    }

    fun toggleMonitoring() {
        val newActive = !_uiState.value.isMonitoringActive
        prefs.edit { putBoolean("monitoring_active", newActive) }
        // Listener will update state
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

        if (_uiState.value.unshortenEnabled && text.contains("http")) {
            _uiState.update { it.copy(isResolving = true, toastMessage = resources.getString(R.string.btn_resolving)) }
            viewModelScope.launch {
                // If unshorten is enabled, try to resolve first.
                // Note: Current resolve logic is optimized for single URL. 
                // Mixed text resolution is a future enhancement.
                val resolved = withContext(Dispatchers.IO) { 
                    UrlCleaner.resolveUrl(text) 
                }
                val cleaned = UrlCleaner.cleanMixedText(resolved)
                finalizeClean(cleaned)
            }
        } else {
            val cleaned = UrlCleaner.cleanMixedText(text)
            finalizeClean(cleaned)
        }
    }

    private fun finalizeClean(cleanedText: String) {
        _uiState.update { it.copy(inputText = cleanedText, isResolving = false) }
        copyToClipboard(cleanedText)
        incrementStats()
        showToast(resources.getString(R.string.toast_done))
    }

    private fun incrementStats() {
        val newCount = _uiState.value.cleanCount + 1
        prefs.edit { putInt("stats_count", newCount) }
        _uiState.update { it.copy(cleanCount = newCount) }
    }

    fun setUnshortenEnabled(enabled: Boolean) {
        prefs.edit { putBoolean("unshorten", enabled) }
        _uiState.update { it.copy(unshortenEnabled = enabled) }
    }

    fun setVibrateEnabled(enabled: Boolean) {
        prefs.edit { putBoolean("vibrate", enabled) }
        _uiState.update { it.copy(vibrateEnabled = enabled) }
    }

    fun setToastEnabled(enabled: Boolean) {
        prefs.edit { putBoolean("toast", enabled) }
        _uiState.update { it.copy(toastEnabled = enabled) }
    }

    // Menu actions
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
            showToast(resources.getString(R.string.toast_done))
        }
    }

    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }

    private fun showToast(message: String) {
        _uiState.update { it.copy(toastMessage = message) }
    }

    private fun copyToClipboard(text: String) {
        clipboardManager.setPrimaryClip(ClipData.newPlainText("PureLink", text))
    }

    override fun onCleared() {
        super.onCleared()
        prefs.unregisterOnSharedPreferenceChangeListener(prefsListener)
    }

    companion object {
        fun provideFactory(
                prefs: SharedPreferences,
                clipboardManager: ClipboardManager,
                resources: Resources
        ): ViewModelProvider.Factory =
                object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return MainViewModel(prefs, clipboardManager, resources) as T
                    }
                }
    }
}