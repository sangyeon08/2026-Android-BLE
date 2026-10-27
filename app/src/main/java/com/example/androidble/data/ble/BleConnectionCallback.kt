package com.example.androidble.data.ble

// 연결 상태를 알려주는 callback
interface BleConnectionCallback {
    fun onConnect(code: Int) // 데이터를 주고받을 상태 됨!
    fun onError(code: Int, message: String)
}
interface CmdCallback {
    fun onSend(bytes: ByteArray)
}

enum class ErrorCode(private val value: Int) {
    TRY_AGAIN(1);

    fun getValue(): Int = value
}
