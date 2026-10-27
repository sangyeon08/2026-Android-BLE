package com.example.androidble.ui.models

// 스캔으로 찾은 기기 정보
data class ScannedDevice(
    val address: String,   // MAC 주소
    val name: String?,     // 이름
    val rssi: Int          // 신호 강도
)