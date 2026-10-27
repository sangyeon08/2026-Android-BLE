package com.example.androidble.ui.screens

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.androidble.data.ble.BleConnection
import com.example.androidble.data.ble.BleConnectionCallback
import com.example.androidble.data.ble.protocol.BleCommand
import com.example.androidble.data.ble.protocol.GripResponseCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

const val MAX_MEASURE_ATTEMPTS = 3

enum class MeasureState { IDLE, MEASURING, COMPLETED, FINISHED }

class ScanViewModel : ViewModel() {
    private val bleConnection = BleConnection()

    // BleConnection의 상태를 그대로 화면에 노출
    val connectionState = bleConnection.connectionState

    // 마지막으로 받은 악력계 응답
    private val _lastResponse = MutableStateFlow("응답 없음")
    val lastResponse: StateFlow<String> = _lastResponse

    private val _measureState = MutableStateFlow(MeasureState.IDLE)
    val measureState: StateFlow<MeasureState> = _measureState

    private val _attempt = MutableStateFlow(0)
    val attempt: StateFlow<Int> = _attempt

    private val _currentValue = MutableStateFlow("0.0")
    val currentValue: StateFlow<String> = _currentValue

    private val _results = MutableStateFlow<List<String>>(emptyList())
    val results: StateFlow<List<String>> = _results

    fun connect(context: Context, address: String) {
        _measureState.value = MeasureState.IDLE
        _attempt.value = 0
        _currentValue.value = "0.0"
        _results.value = emptyList()
        bleConnection.connect(context, address)
    }

    fun checkConnection() {
        bleConnection.write(BleCommand("AR").toByteArray())
        // 그리고 줄줄이 넣는 식으로
    }

    fun startMeasurement() {
        if (_measureState.value == MeasureState.MEASURING) return
        _attempt.value = 0
        _results.value = emptyList()
        beginAttempt()
    }

    private fun beginAttempt() {
        _attempt.value = _attempt.value + 1
        _currentValue.value = "0.0"
        _measureState.value = MeasureState.MEASURING
        checkConnection()
    }

    private fun endAttempt() {
        if (_attempt.value < MAX_MEASURE_ATTEMPTS) {
            beginAttempt()
        } else {
            _measureState.value = MeasureState.COMPLETED
        }
    }

    fun resetDeviceValue() {
        bleConnection.write(BleCommand("HC").toByteArray())
    }

    fun finishMeasurement() {
        _measureState.value = MeasureState.FINISHED
        bleConnection.write(BleCommand("PS").toByteArray())
    }

    private fun formatValue(result: String): String {
        val number = result.toIntOrNull() ?: return result
        return "${number / 10}.${number % 10}"
    }

    fun setMeasureMode() {
        bleConnection.write(BleCommand("MM", byteArrayOf('0'.code.toByte())).toByteArray())
    }

    fun requestMeasurement() {
        bleConnection.write(BleCommand("RS").toByteArray())
    }

    private fun show(text: String) {
        Log.d("ScanViewModel", text)
        _lastResponse.value = text
    }

    init {
        bleConnection.callback = object : BleConnectionCallback {
            override fun onConnect(code: Int) { show("연결 완료: 데이터를 주고받을 수 있음") }
            override fun onError(code: Int, message: String) { show("에러($code): $message") }
        }

        bleConnection.readCallback.responseCallback = object : GripResponseCallback {
            override fun onSetUnitAndBuzzer() { show("DS 응답: 단위·부저 설정 완료") }
            override fun onCheckConnection() {
                show("AR 응답: 연결 확인")
                resetDeviceValue()
            }
            override fun onMeasurement(type: Char, result: String) {
                if (_measureState.value != MeasureState.MEASURING) return
                when (type) {
                    '0' -> {
                        show("RS 응답: 측정 중 $result")
                        _currentValue.value = formatValue(result)
                        requestMeasurement()
                    }
                    '1' -> {
                        show("RS 응답: 측정 완료 $result")
                        val value = formatValue(result)
                        _currentValue.value = value
                        _results.value = _results.value + value
                        endAttempt()
                    }
                    '2' -> {
                        show("RS 응답: 측정 에러 $result")
                        endAttempt()
                    }
                }
            }
            override fun onReset() {
                show("HC 응답: 측정값 초기화 완료")
                setMeasureMode()
            }
            override fun onPowerOff() { show("PS 응답: 전원 끄기") }
            override fun onSetMeasureMode() {
                show("MM 응답: 측정 모드 설정 완료")
                requestMeasurement()
            }
        }
    }
}

