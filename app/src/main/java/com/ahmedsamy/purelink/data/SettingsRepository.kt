package com.ahmedsamy.purelink.data

import android.content.Context
import androidx.core.content.edit

class SettingsRepository(context: Context) {
    private val prefs = context.getSharedPreferences("PureLinkPrefs", Context.MODE_PRIVATE)

    fun hasSeenOnboarding(): Boolean {
        return prefs.getBoolean("has_seen_onboarding", false)
    }

    fun setOnboardingSeen() {
        prefs.edit { putBoolean("has_seen_onboarding", true) }
    }

    fun isVibrateEnabled(): Boolean {
        return prefs.getBoolean("vibrate", true)
    }

    fun isToastEnabled(): Boolean {
        return prefs.getBoolean("toast", true)
    }
    
    fun setVibrateEnabled(enabled: Boolean) {
        prefs.edit { putBoolean("vibrate", enabled) }
    }
    
    fun setToastEnabled(enabled: Boolean) {
        prefs.edit { putBoolean("toast", enabled) }
    }

    fun isUnshortenEnabled(): Boolean {
        return prefs.getBoolean("unshorten", false)
    }

    fun setUnshortenEnabled(enabled: Boolean) {
        prefs.edit { putBoolean("unshorten", enabled) }
    }
}
