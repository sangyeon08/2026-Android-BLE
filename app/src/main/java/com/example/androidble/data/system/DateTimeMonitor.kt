package com.example.androidble.data.system

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.androidble.util.effect.SystemBroadcastReceiver
import com.example.androidble.util.DateTimeUtil

@Composable
fun rememberCurrentDateTime(): String {
    // 현재 시간을 저장해 시간 이벤트마다 화면을 갱신
    var currentDateTime by remember {
        mutableStateOf(
            DateTimeUtil.getCurrentDateTimeText()
        )
    }

    // Android가 보내는 분 단위 시간 이벤트 수신
    SystemBroadcastReceiver(
        systemAction = Intent.ACTION_TIME_TICK
    ) {
        currentDateTime = DateTimeUtil.getCurrentDateTimeText()
    }

    return currentDateTime
}