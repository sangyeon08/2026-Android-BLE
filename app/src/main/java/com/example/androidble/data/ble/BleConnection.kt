package com.example.androidble.data.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.example.androidble.data.ble.protocol.BleCommand
import com.example.androidble.data.ble.protocol.GripModule
import com.example.androidble.data.ble.protocol.ICommandCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@SuppressLint("MissingPermission")
class BleConnection {

    // 연결된 악력계랑 통신하기
    private var mBluetoothGatt: BluetoothGatt? = null

    // 연결 상태를 담는 상자
    private val _connectionState = MutableStateFlow("DISCONNECTED")
    val connectionState: StateFlow<String> = _connectionState

    var callback: BleConnectionCallback? = null
    var cmdCallback: CmdCallback? = null

    // 검증된 BleCommand를 받아서 해석하는 곳
    val readCallback = GripModule()
    private val commandCallback: ICommandCallback = readCallback

    fun connect(context: Context, address: String) {
        val adapter = (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter

        val device: BluetoothDevice = adapter.getRemoteDevice(address)

        // 기존 장비 연결 해제 처리
        disconnect()

        _connectionState.value = "CONNECTING"
        val gatt = device.connectGatt(
            context,
            false,
            gattCallback
        )

        mBluetoothGatt = gatt
    }

    fun disconnect() {
        mBluetoothGatt?.let {
            it.disconnect()
            it.close()
        }
        mBluetoothGatt = null // null 처리 필수
        _connectionState.value = "DISCONNECTED"
    }

    private val gattCallback = object : BluetoothGattCallback() {

        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {

                Handler(Looper.getMainLooper()).postDelayed({
                    gatt.discoverServices()
                }, 200)

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                disconnect()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                enableTXNotification()
            }
        }

        override fun onCharacteristicChanged( // 장비 데이터가 앱에 도착
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            readData(value)
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            readData(characteristic.value)
        }
    }

    private fun enableTXNotification() {
        if (mBluetoothGatt == null) {
            callback?.onError(ErrorCode.TRY_AGAIN.getValue(), "gatt is null retry to connect")
            return
        }
        val rxService = mBluetoothGatt!!.getService(BleUuids.RX_SERVICE_UUID)
        if (rxService == null) {
            callback?.onError(ErrorCode.TRY_AGAIN.getValue(), "service is null retry to connect")
            return
        }
        val txChar = rxService.getCharacteristic(BleUuids.TX_CHAR_UUID)
        if (txChar == null) {
            callback?.onError(
                ErrorCode.TRY_AGAIN.getValue(),
                "characteristic is null retry to connect"
            )
            return
        }
        mBluetoothGatt!!.setCharacteristicNotification(txChar, true)
        val descriptor = txChar.getDescriptor(BleUuids.CCCD)
        descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
        mBluetoothGatt!!.writeDescriptor(descriptor)

        // 데이터를 주고받을 상태가 되었음
        Handler(Looper.getMainLooper()).postDelayed({
            _connectionState.value = "CONNECTED"
            callback?.onConnect(0)
        }, 1000)
    }

    fun write(byteArray: ByteArray): Boolean? {
        if (mBluetoothGatt == null) {
            return null
        }

        val rxService = mBluetoothGatt!!.getService(BleUuids.RX_SERVICE_UUID)
        if (rxService == null) {
            callback?.onError(ErrorCode.TRY_AGAIN.getValue(), "블루투스 서비스 에러")
            return null
        }

        val rxChar = rxService.getCharacteristic(BleUuids.RX_CHAR_UUID)
        if (rxChar == null) {
            callback?.onError(ErrorCode.TRY_AGAIN.getValue(), "블루트스 서비스 에러")
            return null
        }

        rxChar.value = byteArray
        rxChar.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE

        val ret = mBluetoothGatt!!.writeCharacteristic(rxChar)

        // 사용자가 커맨드 보고 싶을 때
        if (cmdCallback != null) {
            Thread {
                cmdCallback!!.onSend(byteArray)
            }.start()
        }

        return ret
    }

    private fun readData(value: ByteArray) {
        // STX랑 ETX 검증
        if (value.size < 8 || value[0] != 0x02.toByte() || value.last() != 0x03.toByte()) return

        val bod1 = (value[2].toInt() and 0xFF) - 0x0A
        val bod2 = (value[3].toInt() and 0xFF) - 0x0A
        val length = 4 + (bod1 + (bod2 shl 6)) + 2

        // 프레임 추출
        if (length != value.size) return

        val bleCommand = BleCommand.fromByteArray(value) ?: return
        commandCallback.onRead(bleCommand)
    }
}
