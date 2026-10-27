package com.example.androidble.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.NetworkWifi
import androidx.compose.material.icons.filled.NetworkWifi1Bar
import androidx.compose.material.icons.filled.NetworkWifi2Bar
import androidx.compose.material.icons.filled.NetworkWifi3Bar
import androidx.compose.material.icons.filled.SignalCellular0Bar
import androidx.compose.material.icons.filled.SignalCellular4Bar
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.SignalWifi4Bar
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.androidble.ui.models.NetworkState
import com.example.androidble.ui.models.NetworkType

@Composable
fun InternetIndicator(
    state: NetworkState,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val icon = when (state.type) {
        NetworkType.NONE -> Icons.Default.WifiOff

        NetworkType.WIFI -> when (state.signalLevel) {
            0 -> Icons.Default.NetworkWifi
            1 -> Icons.Default.NetworkWifi1Bar
            2 -> Icons.Default.NetworkWifi2Bar
            3 -> Icons.Default.NetworkWifi3Bar
            4 -> Icons.Default.SignalWifi4Bar
            else -> Icons.Default.SignalWifi4Bar
        }

        NetworkType.CELLULAR -> when (state.signalLevel) {
            0 -> Icons.Default.SignalCellular0Bar
            else -> Icons.Default.SignalCellularAlt
        }

        NetworkType.ETHERNET -> Icons.Default.Lan
    }

    // 권한이 없으면 빨간색, 연결되어 있으면 흰색, 그 외에는 회색
    val color = when {
        !state.hasPermission -> Color.Red
        state.type != NetworkType.NONE -> Color.White
        else -> Color.Gray
    }

    Icon(
        imageVector = icon,
        contentDescription = "Internet Status",
        modifier = modifier.clickable { onClick() },
        tint = color
    )
}
