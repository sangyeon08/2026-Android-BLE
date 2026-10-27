package com.example.androidble.data.system

import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import android.telephony.PhoneStateListener
import android.telephony.SignalStrength
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.androidble.ui.models.NetworkState
import com.example.androidble.ui.models.NetworkType
import com.example.androidble.util.PermissionUtil

@Composable
fun rememberNetworkState(): NetworkState {
    val context = LocalContext.current

    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    val wifiManager =
        context.getSystemService(Context.WIFI_SERVICE) as? WifiManager
    val telephonyManager =
        context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager

    // 셀룰러 신호 강도 레벨 (0~4)
    var cellularSignalLevel by remember { mutableIntStateOf(0) }

    // 권한 확인
    val hasLocationPermission = PermissionUtil.hasPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    val hasPhoneStatePermission = PermissionUtil.hasPermission(
        context,
        Manifest.permission.READ_PHONE_STATE
    )

    // 현재 네트워크 상태 계산 함수
    fun getCurrentNetworkState(): NetworkState {
        val activeNetwork = connectivityManager?.activeNetwork
        val caps = connectivityManager?.getNetworkCapabilities(activeNetwork)

        if (activeNetwork == null || caps == null || !caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
            return NetworkState(
                type = NetworkType.NONE,
                signalLevel = 0,
                hasPermission = true
            )
        }

        return when {
            // Wi-Fi 연결 상태
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                val rssi = wifiManager?.connectionInfo?.rssi ?: -100
                val level = WifiManager.calculateSignalLevel(rssi, 5)
                NetworkState(
                    type = NetworkType.WIFI,
                    signalLevel = level,
                    hasPermission = hasLocationPermission
                )
            }

            // 셀룰러 연결 상태
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                NetworkState(
                    type = NetworkType.CELLULAR,
                    signalLevel = cellularSignalLevel,
                    hasPermission = hasPhoneStatePermission
                )
            }

            // 3. 이더넷 연결 상태
            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                NetworkState(
                    type = NetworkType.ETHERNET,
                    signalLevel = 4,
                    hasPermission = true
                )
            }

            else -> {
                NetworkState(
                    type = NetworkType.NONE,
                    signalLevel = 0,
                    hasPermission = true
                )
            }
        }
    }

    var networkState by remember { mutableStateOf(getCurrentNetworkState()) }

    // 셀룰러 신호 강도 감지 리스너
    DisposableEffect(hasPhoneStatePermission) {
        if (hasPhoneStatePermission && telephonyManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val callback = @RequiresApi(Build.VERSION_CODES.S) object :
                    TelephonyCallback(),
                    TelephonyCallback.SignalStrengthsListener {
                    override fun onSignalStrengthsChanged(signalStrength: SignalStrength) {
                        cellularSignalLevel = signalStrength.level
                        networkState = getCurrentNetworkState()
                    }
                }
                try {
                    telephonyManager.registerTelephonyCallback(context.mainExecutor, callback)
                } catch (ignored: Exception) {
                }

                onDispose {
                    try {
                        telephonyManager.unregisterTelephonyCallback(callback)
                    } catch (ignored: Exception) {
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                val listener = object : PhoneStateListener() {
                    @Deprecated("Deprecated in Java")
                    override fun onSignalStrengthsChanged(signalStrength: SignalStrength?) {
                        super.onSignalStrengthsChanged(signalStrength)
                        if (signalStrength != null) {
                            cellularSignalLevel = signalStrength.level
                            networkState = getCurrentNetworkState()
                        }
                    }
                }
                try {
                    @Suppress("DEPRECATION")
                    telephonyManager.listen(listener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS)
                } catch (ignored: Exception) {
                }

                onDispose {
                    try {
                        @Suppress("DEPRECATION")
                        telephonyManager.listen(listener, PhoneStateListener.LISTEN_NONE)
                    } catch (ignored: Exception) {
                    }
                }
            }
        } else {
            onDispose { }
        }
    }

    // 네트워크 변화 감지
    DisposableEffect(Unit) {
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                networkState = getCurrentNetworkState()
            }

            override fun onLost(network: Network) {
                networkState = getCurrentNetworkState()
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                networkState = getCurrentNetworkState()
            }
        }

        try {
            connectivityManager?.registerDefaultNetworkCallback(networkCallback)
        } catch (ignored: Exception) {
        }

        onDispose {
            try {
                connectivityManager?.unregisterNetworkCallback(networkCallback)
            } catch (ignored: Exception) {
            }
        }
    }

    return networkState
}
