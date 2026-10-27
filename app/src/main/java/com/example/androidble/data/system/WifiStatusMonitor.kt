package com.example.androidble.data.system

import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.androidble.ui.models.WifiState
import com.example.androidble.ui.models.WifiStatus
import com.example.androidble.util.PermissionUtil
import com.example.androidble.util.effect.SystemBroadcastReceiver

@Composable
fun rememberWifiState(): WifiState { // 현재 값 기억하기
    val context = LocalContext.current

    // Wi-Fi 활성화 여부랑 세기 읽는 서비스
    val wifiManager =
        context.getSystemService(Context.WIFI_SERVICE) as WifiManager

    // 현재 연결된 네트워크 종류
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE)
                as ConnectivityManager

    fun getCurrentWifiState(): WifiState {
        val hasPermission = PermissionUtil.hasPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        if (!wifiManager.isWifiEnabled) {
            return WifiState(
                status = WifiStatus.DISABLED,
                signalLevel = 0,
                hasPermission = hasPermission
            )
        }

        val networkInfo = connectivityManager.activeNetworkInfo

        if (networkInfo != null &&
            networkInfo.isConnected &&
            networkInfo.type == ConnectivityManager.TYPE_WIFI
        ) {
            val rssi = wifiManager.connectionInfo.rssi

            val signalLevel = WifiManager.calculateSignalLevel(
                rssi,
                5
            )

            return WifiState(
                status = WifiStatus.CONNECTED,
                signalLevel = signalLevel,
                hasPermission = hasPermission
            )
        }

        return WifiState(
            status = WifiStatus.DISCONNECTED,
            signalLevel = 0,
            hasPermission = hasPermission
        )
    }

    var wifiState by remember {
        mutableStateOf(
            getCurrentWifiState()
        )
    }

    // Wi-Fi 기능 켜짐/꺼짐 감지
    SystemBroadcastReceiver(
        systemAction = WifiManager.WIFI_STATE_CHANGED_ACTION
    ) {
        wifiState = getCurrentWifiState()
    }

    // 네트워크 연결 및 신호 세기 변경 감지
    SystemBroadcastReceiver(
        systemAction = ConnectivityManager.CONNECTIVITY_ACTION
    ) {
        wifiState = getCurrentWifiState()
    }

    return wifiState
}