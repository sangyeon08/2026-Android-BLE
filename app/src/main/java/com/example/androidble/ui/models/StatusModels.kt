package com.example.androidble.ui.models

//status monitor에서 가져온 상태를 데이터로 저장해주기

data class BatteryState(
    val percentage: Int = 100,
    val isCharging: Boolean = false,
    val isPowerSaveMode: Boolean = false,
    val chargingType: ChargingType = ChargingType.NONE
)

enum class ChargingType {
    NONE,
    AC,
    USB, // wired가 아니엇구만..
    WIRELESS
}

enum class NetworkType {
    NONE,
    WIFI,
    CELLULAR,
    ETHERNET
}

data class NetworkState(
    val type: NetworkType = NetworkType.NONE,
    val signalLevel: Int = 0, // 0~4 단계
    val hasPermission: Boolean = true
)

enum class WifiStatus {
    CONNECTED,
    DISCONNECTED,
    DISABLED
}

data class WifiState(
    val status: WifiStatus = WifiStatus.DISABLED,
    val signalLevel: Int = 0,
    val hasPermission: Boolean = true
)

enum class BluetoothStatus {
    ON,
    OFF,
    CONNECTING,
    NOT_SUPPORTED
}

data class BluetoothState(
    val status: BluetoothStatus = BluetoothStatus.OFF,
    val hasPermission: Boolean = true
)