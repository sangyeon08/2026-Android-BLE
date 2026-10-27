package com.example.androidble.util

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

// 날짜 포맷팅을 담당
object DateTimeUtil {
    private val formatter = DateTimeFormatter.ofPattern("yy.MM.dd HH:mm:ss", Locale.getDefault())

    fun getCurrentDateTimeText(): String {
        return LocalDateTime.now().format(formatter)
    }
}
