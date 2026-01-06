package com.ahmedsamy.purelink.utils

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import com.ahmedsamy.purelink.data.SettingsRepository

object FeedbackUtils {

    fun performHapticFeedback(context: Context) {
        val repo = SettingsRepository(context)
        if (repo.isVibrateEnabled()) {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (vibrator.hasVibrator()) {
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
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
