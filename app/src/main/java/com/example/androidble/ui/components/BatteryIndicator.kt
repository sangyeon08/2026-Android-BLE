package com.example.androidble.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Battery1Bar
import androidx.compose.material.icons.filled.Battery3Bar
import androidx.compose.material.icons.filled.Battery5Bar
import androidx.compose.material.icons.filled.Battery6Bar
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidble.ui.models.BatteryState

@Composable
fun BatteryIndicator(
    state: BatteryState,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val color = when {
        state.isCharging -> Color.Green
        state.isPowerSaveMode -> Color(0xFFFFA500)
        state.percentage <= 15 -> Color.Red
        else -> Color.White
    }

    // 현재 상태와 잔량에 맞는 배터리 아이콘을 선택
    val icon = when {
        state.isCharging -> Icons.Default.BatteryChargingFull
        state.isPowerSaveMode -> Icons.Default.BatterySaver
        state.percentage <= 15 -> Icons.Default.BatteryAlert
        state.percentage >= 90 -> Icons.Default.BatteryFull
        state.percentage >= 70 -> Icons.Default.Battery6Bar
        state.percentage >= 50 -> Icons.Default.Battery5Bar
        state.percentage >= 30 -> Icons.Default.Battery3Bar
        else -> Icons.Default.Battery1Bar
    }

    Row(
        modifier = modifier.clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "BatteryStatus",
            modifier = Modifier.size(16.dp),
            tint = color
        )

        Spacer(
            modifier = Modifier.width(4.dp)
        )

        Text(
            text = "${state.percentage}%",
            fontSize = 12.sp,
            color = color
        )
    }
}