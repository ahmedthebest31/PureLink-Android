package com.ahmedsamy.purelink.utils

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.ahmedsamy.purelink.data.SettingsRepository

object FeedbackUtils {

    fun performHapticFeedback(context: Context) {
        val repo = SettingsRepository(context)
        if (repo.isVibrateEnabled()) {
            val vibrator = ContextCompat.getSystemService(context, Vibrator::class.java)
            if (vibrator?.hasVibrator() == true) {
                vibrator.vibrate(VibrationEffect.createOneShot(20, 40))
            }
        }
    }

    fun showToast(context: Context, message: String) {
        val repo = SettingsRepository(context)
        if (repo.isToastEnabled()) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
}
