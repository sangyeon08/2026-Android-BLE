package com.example.androidble.util

import android.content.Context
import android.content.Intent
import android.provider.Settings

object SettingUtil {

    // 안드로이드 세팅 인텐트 액션으로 해당 설정 화면으로 이동
    fun openSetting(context: Context, action: String) {
        val intent = Intent(action).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // 해당 설정 화면이 없거나 예외 발생 시 기본 설정 화면으로 안전하게 이동
            try {
                val fallbackIntent = Intent(Settings.ACTION_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(fallbackIntent)
            } catch (ignored: Exception) {
            }
        }
    }
}
