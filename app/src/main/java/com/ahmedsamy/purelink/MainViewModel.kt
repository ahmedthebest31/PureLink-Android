package com.ahmedsamy.purelink

import android.content.ClipData
import android.content.ClipboardManager
import android.content.SharedPreferences
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.core.content.edit

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
        private val clipboardManager: ClipboardManager
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
            _uiState.update { it.copy(isResolving = true, toastMessage = "Resolving...") }
            viewModelScope.launch {
                val resolved = withContext(Dispatchers.IO) { resolveUrl(text) }
                val cleaned = cleanUrl(resolved)
                finalizeClean(cleaned)
            }
        } else {
            finalizeClean(cleanUrl(text))
        }
    }

    private fun finalizeClean(cleanedText: String) {
        _uiState.update { it.copy(inputText = cleanedText, isResolving = false) }
        copyToClipboard(cleanedText)
        incrementStats()
        showToast("DONE! 🚀")
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
                showToast("Invalid Base64")
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
            val cleaned = cleanUrl(text)
            _uiState.update { it.copy(inputText = cleaned) }
            copyToClipboard(cleaned)
            incrementStats()
            showToast("DONE! 🚀")
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

    private fun cleanUrl(url: String): String {
        var result = url
        val pattern = Regex("([?&](utm_[^=&]+|fbclid|gclid|ref|s|si)=[^&]*)")
        result = pattern.replace(result, "")
        if (result.endsWith("?") || result.endsWith("&")) {
            result = result.dropLast(1)
        }
        return result
    }

    private fun resolveUrl(shortUrl: String): String {
        return try {
            var url = shortUrl.trim()
            if (!url.startsWith("http")) url = "https://$url"
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.setRequestProperty("User-Agent", "Mozilla/5.0")
            connection.instanceFollowRedirects = false
            connection.connect()
            val location = connection.getHeaderField("Location") ?: connection.url.toString()
            location.ifEmpty { shortUrl }
        } catch (_: Exception) {
            shortUrl
        }
    }

    override fun onCleared() {
        super.onCleared()
        prefs.unregisterOnSharedPreferenceChangeListener(prefsListener)
    }

    companion object {
        fun provideFactory(
                prefs: SharedPreferences,
                clipboardManager: ClipboardManager
        ): ViewModelProvider.Factory =
                object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return MainViewModel(prefs, clipboardManager) as T
                    }
                }
    }
}
