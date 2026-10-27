package com.example.androidble.data.system

import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.os.PowerManager
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.androidble.ui.models.BatteryState
import com.example.androidble.ui.models.ChargingType
import com.example.androidble.util.effect.SystemBroadcastReceiver

@Composable
fun rememberBatteryState(): BatteryState { // 이벤트를 앱 상태로
    val context = LocalContext.current

    var batteryState by remember {
        mutableStateOf(BatteryState())
    }

    // 이전 충전 상태를 기억하여 실제 변경이 일어났을 때만 토스트 출력
    var previousChargingType by remember {
        mutableStateOf<ChargingType?>(null)
    }

    SystemBroadcastReceiver(
        systemAction = Intent.ACTION_BATTERY_CHANGED
    ) { intent ->
        intent?.let {
            val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val status = it.getIntExtra(
                BatteryManager.EXTRA_STATUS,
                -1
            )
            val plugged = it.getIntExtra(
                BatteryManager.EXTRA_PLUGGED,
                0
            )

            // 충전 유형 구분
            val currentChargingType = when (plugged) {
                BatteryManager.BATTERY_PLUGGED_AC -> ChargingType.AC
                BatteryManager.BATTERY_PLUGGED_USB -> ChargingType.USB
                BatteryManager.BATTERY_PLUGGED_WIRELESS -> ChargingType.WIRELESS
                else -> ChargingType.NONE
            }

            // 충전 상태가 변경되었을 때만 토스트 메시지 표시
            if (previousChargingType != null && previousChargingType != currentChargingType) {
                val message = when (currentChargingType) {
                    ChargingType.AC -> "AC(콘센트) 충전 시작"
                    ChargingType.USB -> "USB 충전 시작"
                    ChargingType.WIRELESS -> "무선 충전 시작"
                    ChargingType.NONE -> "충전 해제됨"
                }
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }

            previousChargingType = currentChargingType

            // 배터리 상태 업데이트
            batteryState = batteryState.copy(
                percentage = if (level != -1 && scale != -1) {
                    (level * 100) / scale
                } else {
                    batteryState.percentage
                },
                isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING,
                chargingType = currentChargingType
            )
        }
    }

    // 절전? 절약 모드
    SystemBroadcastReceiver(
        systemAction = PowerManager.ACTION_POWER_SAVE_MODE_CHANGED
    ) {
        val powerManager =
            context.getSystemService(Context.POWER_SERVICE) as? PowerManager

        val isPowerSave = try {
            powerManager?.isPowerSaveMode == true
        } catch (ignored: Exception) {
            false
        }

        batteryState = batteryState.copy(
            isPowerSaveMode = isPowerSave
        )
    }

    return batteryState
}