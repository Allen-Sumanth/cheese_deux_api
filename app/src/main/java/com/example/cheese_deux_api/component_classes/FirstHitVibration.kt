package com.example.cheese_deux_api.component_classes

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat

class FirstHitVibration {
    @RequiresApi(Build.VERSION_CODES.O)
    fun vibrate(context: Context, duration: Long) {
        val vibrator = ContextCompat.getSystemService(context, Vibrator::class.java)
        vibrator?.let {
            if (it.hasVibrator()) {
                val vibrationEffect = VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE)
                it.vibrate(vibrationEffect)
            }
        }
    }

}