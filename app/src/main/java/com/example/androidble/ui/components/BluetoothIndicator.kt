package com.example.androidble.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.BluetoothSearching
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.androidble.ui.models.BluetoothState
import com.example.androidble.ui.models.BluetoothStatus

@Composable
fun BluetoothIndicator(
    state: BluetoothState,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val icon = when (state.status) {
        BluetoothStatus.ON -> Icons.Default.Bluetooth
        BluetoothStatus.OFF, BluetoothStatus.NOT_SUPPORTED -> Icons.Default.BluetoothDisabled
        BluetoothStatus.CONNECTING -> Icons.AutoMirrored.Filled.BluetoothSearching
    }

    // 권한이 없으면 빨간색, 켜짐이나 연결 중이면 흰색, 그 외에는 회색
    val color = when {
        !state.hasPermission -> Color.Red
        state.status == BluetoothStatus.ON || state.status == BluetoothStatus.CONNECTING -> Color.White
        else -> Color.Gray
    }

    Icon(
        imageVector = icon,
        contentDescription = "Bluetooth Status",
        modifier = modifier.clickable { onClick() },
        tint = color
    )
}
