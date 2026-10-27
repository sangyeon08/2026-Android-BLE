package com.example.androidble.ui.components

import android.Manifest
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.androidble.data.system.rememberBatteryState
import com.example.androidble.data.system.rememberBluetoothState
import com.example.androidble.data.system.rememberNetworkState
import com.example.androidble.ui.models.NetworkType
import com.example.androidble.util.SettingUtil
import com.example.androidble.util.effect.PermissionState
// 권한을 여기서 요구 안 하고 한층 더 위? 에서 공통으로 요구하는걸로 변경. RememberPermissionState -> PermissionState

@Composable
fun StatusBar(
    permissionState: PermissionState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // 배터리, 인터넷, Bluetooth의 현재 시스템 상태 읽기
    val batteryState = rememberBatteryState()
    val networkState = rememberNetworkState()
    val bluetoothState = rememberBluetoothState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(32.dp)
            .background(Color.Black)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // 날짜/시간 클릭 ➔ 날짜 및 시간 설정 화면 이동
        DateTimeDisplay(
            onClick = {
                SettingUtil.openSetting(context, Settings.ACTION_DATE_SETTINGS)
            }
        )

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 블루투스 클릭 ➔ 권한 확인 후 블루투스 설정 화면 이동
            BluetoothIndicator(
                state = bluetoothState,
                modifier = Modifier.size(18.dp),
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        permissionState.checkAndRequestPermission(Manifest.permission.BLUETOOTH_CONNECT) {
                            SettingUtil.openSetting(context, Settings.ACTION_BLUETOOTH_SETTINGS)
                        }
                    } else {
                        SettingUtil.openSetting(context, Settings.ACTION_BLUETOOTH_SETTINGS)
                    }
                }
            )

            Spacer(modifier = Modifier.width(8.dp))

            // 인터넷 클릭 ➔ 권한 확인 후 해당 네트워크(WiFi / Cellular / Ethernet) 설정 화면 이동
            InternetIndicator(
                state = networkState,
                modifier = Modifier.size(18.dp),
                onClick = {
                    when (networkState.type) {
                        NetworkType.CELLULAR -> {
                            permissionState.checkAndRequestPermission(Manifest.permission.READ_PHONE_STATE) {
                                SettingUtil.openSetting(context, Settings.ACTION_DATA_ROAMING_SETTINGS)
                            }
                        }
                        NetworkType.WIFI -> {
                            permissionState.checkAndRequestPermission(Manifest.permission.ACCESS_FINE_LOCATION) {
                                SettingUtil.openSetting(context, Settings.ACTION_WIFI_SETTINGS)
                            }
                        }
                        NetworkType.ETHERNET -> {
                            SettingUtil.openSetting(context, Settings.ACTION_WIRELESS_SETTINGS)
                        }
                        NetworkType.NONE -> {
                            SettingUtil.openSetting(context, Settings.ACTION_WIRELESS_SETTINGS)
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.width(8.dp))

            // 배터리 클릭 ➔ 배터리 설정 화면 이동
            BatteryIndicator(
                state = batteryState,
                onClick = {
                    SettingUtil.openSetting(context, Settings.ACTION_BATTERY_SAVER_SETTINGS)
                }
            )
        }
    }
}