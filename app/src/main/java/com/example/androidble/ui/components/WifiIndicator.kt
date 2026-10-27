package com.example.androidble.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NetworkWifi
import androidx.compose.material.icons.filled.NetworkWifi1Bar
import androidx.compose.material.icons.filled.NetworkWifi2Bar
import androidx.compose.material.icons.filled.NetworkWifi3Bar
import androidx.compose.material.icons.filled.SignalWifi0Bar
import androidx.compose.material.icons.filled.SignalWifi4Bar
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.androidble.ui.models.WifiState
import com.example.androidble.ui.models.WifiStatus

@Composable
fun WifiIndicator(
    state: WifiState,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val icon = when (state.status) {
        WifiStatus.DISABLED ->
            Icons.Default.WifiOff

        WifiStatus.DISCONNECTED ->
            Icons.Default.SignalWifi0Bar

        WifiStatus.CONNECTED -> when (state.signalLevel) {
            0 -> Icons.Default.NetworkWifi
            1 -> Icons.Default.NetworkWifi1Bar
            2 -> Icons.Default.NetworkWifi2Bar
            3 -> Icons.Default.NetworkWifi3Bar
            4 -> Icons.Default.SignalWifi4Bar
            else -> Icons.Default.SignalWifi4Bar
        }
    }

    // 권한이 없으면 빨간색, 연결되어 있으면 흰색, 그 외에는 회색
    val color = when {
        !state.hasPermission -> Color.Red
        state.status == WifiStatus.CONNECTED -> Color.White
        else -> Color.Gray
    }

    Icon(
        imageVector = icon,
        contentDescription = "Wi-Fi Status",
        modifier = modifier.clickable { onClick() },
        tint = color
    )
}