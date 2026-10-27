package com.example.androidble.data.ble.protocol

// 검증을 통과한 BleCommand를 전달받는 인터페이스
interface ICommandCallback {
    fun onRead(bleCommand: BleCommand)
}

// GripModule이 CMD를 해석한 결과를 앱에 알려주는 callback
interface GripResponseCallback {
    fun onSetUnitAndBuzzer() // DS
    fun  onCheckConnection() // AR
    fun onMeasurement(type: Char, result: String) // RS
    fun onReset() // HC
    fun onPowerOff() // PS
    fun onSetMeasureMode() // MM
}

// CMD를 보고 어떤 응답인지 해석
class GripModule : ICommandCallback {

    var responseCallback: GripResponseCallback? = null

    override fun onRead(bleCommand: BleCommand) {
        val cmd = bleCommand.cmd
        val data = bleCommand.data

        when (cmd) {
            "DS" -> responseCallback?.onSetUnitAndBuzzer()
            "AR" -> responseCallback?.onCheckConnection()
            "RS" -> { // 측정상태 및 측정값
                if (data == null || data.size < 4) return
                val type = data[0].toInt().toChar()
                val result = String(data, 2, data.size - 3, Charsets.US_ASCII)
                responseCallback?.onMeasurement(type, result)
            }
            "HC" -> responseCallback?.onReset()
            "PS" -> responseCallback?.onPowerOff()
            "MM" -> responseCallback?.onSetMeasureMode()
        }
    }
}
