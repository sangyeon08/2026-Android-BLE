package com.example.androidble.ui.screens

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.androidble.data.ble.rememberBleScanner
import com.example.androidble.ui.components.ScannedDeviceItem
import com.example.androidble.util.effect.PermissionState
import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel

@SuppressLint("MissingPermission") // 구조를 위해..
@Composable
fun ScanScreen( permissionState: PermissionState, viewModel: ScanViewModel = viewModel()
) {
    val scanner = rememberBleScanner()

    val context = LocalContext.current

    // 연결 상태
    val connectionState by viewModel.connectionState.collectAsState()

    // 스캔에 필요한 권한
    val scanPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Manifest.permission.BLUETOOTH_SCAN
    } else {
        Manifest.permission.ACCESS_FINE_LOCATION
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // 화면 상단 연결 상태
        Text(
            text = "연결 상태: $connectionState",
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // 스캔 시작, 중지 버튼
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    // 권한이 있으면 스캔 시작, 없으면 권한 요청하거나 설정 화면으로 이동
                    permissionState.checkAndRequestPermission(scanPermission) {
                        scanner.startScan()
                    }
                },
                enabled = !scanner.isScanning
            ) {
                Text("스캔 시작")
            }

            OutlinedButton(
                onClick = { scanner.stopScan() },
                enabled = scanner.isScanning
            ) {
                Text("스캔 중지")
            }
        }

        // 현재 상태
        Text(
            text = if (scanner.isScanning) {
                "스캔 중... (${scanner.devices.size}대 발견)"
            } else {
                "발견한 기기: ${scanner.devices.size}대"
            },
            modifier = Modifier.padding(horizontal = 16.dp),
            style = MaterialTheme.typography.bodyMedium
        )

        // 발견한 기기 목록. MAC 주소를 key로 써서 같은 기기는 같은 줄로 유지할 수 있다..!
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(
                items = scanner.devices,
                key = { device -> device.address }
            ) { device ->
                ScannedDeviceItem(
                    device = device,
                    // 탭하면 GATT 연결 시도
                    modifier = Modifier.clickable {
                        viewModel.connect(context, device.address)
                    }
                )
                HorizontalDivider()
            }
        }
    }
}