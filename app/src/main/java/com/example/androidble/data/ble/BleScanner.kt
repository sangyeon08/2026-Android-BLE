package com.example.androidble.data.ble

import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.androidble.ui.models.ScannedDevice
import kotlinx.coroutines.delay

private const val TAG = "BleScanner"
private const val SCAN_PERIOD_MS = 10_000L // 스캔 제한 시간

class BleScannerState(
    val devices: List<ScannedDevice>,  // 기기 목록
    val isScanning: Boolean,           // 스캔 중인지
    val startScan: () -> Unit,
    val stopScan: () -> Unit
)

@Composable
fun rememberBleScanner(): BleScannerState {
    val context = LocalContext.current

    // 너 블루투스가 있니! 어뎁터 가져오기
    val bluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    val bluetoothAdapter = bluetoothManager?.adapter

    var devices by remember { mutableStateOf(listOf<ScannedDevice>()) }
    var isScanning by remember { mutableStateOf(false) }

    // 확인 시 할 일
    val scanCallback = remember {
        object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val name = result.scanRecord?.deviceName
                if (name.isNullOrEmpty()) return

                val device = ScannedDevice(
                    address = result.device.address,
                    name = name,
                    rssi = result.rssi
                )
                Log.d(TAG, "발견: ${device.name} / ${device.address} / ${device.rssi}")

                // 같은 MAC 주소가 이미 목록에 있으면 RSSI만 갱신, 없으면 끝에 추가
                val alreadyFound = devices.any { it.address == device.address }
                devices = if (alreadyFound) {
                    devices.map {
                        if (it.address == device.address) it.copy(rssi = device.rssi) else it
                    }
                } else {
                    devices + device
                }
            }

            override fun onScanFailed(errorCode: Int) {
                Log.w(TAG, "스캔 실패: errorCode=$errorCode")
                isScanning = false
            }
        }
    }

    // 키기
    val startScan: () -> Unit = {
        // 블루투스가 꺼져 있으면 스캐너가 null
        val scanner = bluetoothAdapter?.bluetoothLeScanner
        if (scanner == null) {
            Log.w(TAG, "블루투스가 꺼져 있거나 지원하지 않는 기기")
        } else {
            try {
                devices = emptyList()
                // 지금은 비어있음..!
                val scanFilters = listOf(
                    ScanFilter.Builder()
                        .build()
                )

                // 스캔 설정 가능. 빠르게, 계속, 약해도 적극적으로
                val scanSettings = ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                    .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
                    .build()

                scanner.startScan(scanFilters, scanSettings, scanCallback)
                isScanning = true
            } catch (e: SecurityException) {
                Log.w(TAG, "스캔 권한 없음", e)
            }
        }
    }

    val stopScan: () -> Unit = {
        try {
            bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
        } catch (e: SecurityException) {
            Log.w(TAG, "스캔 권한 없음", e)
        }
        isScanning = false
    }

    // 스캔을 시작하면 10초 뒤 자동으로 중지
    LaunchedEffect(isScanning) {
        if (isScanning) {
            delay(SCAN_PERIOD_MS)
            stopScan()
        }
    }

    // 화면이 사라지면 끄기 (배터리랑 CPU 절약)
    DisposableEffect(Unit) {
        onDispose {
            try {
                bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
            } catch (e: SecurityException) {
                Log.w(TAG, "스캔 권한 없음", e)
            }
        }
    }

    return BleScannerState(
        devices = devices,
        isScanning = isScanning,
        startScan = startScan,
        stopScan = stopScan
    )
}