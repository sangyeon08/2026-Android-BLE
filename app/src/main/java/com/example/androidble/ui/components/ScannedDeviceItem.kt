package com.example.androidble.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.androidble.ui.models.ScannedDevice

// 스캔 목록에 기기 이름, MAC 주소, 신호 세기
@Composable
fun ScannedDeviceItem(
    device: ScannedDevice,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = device.name ?: "(이름 없음)",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = device.address,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = "${device.rssi} dBm",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

// 일단 빈칸 채우기용 가짜
@Preview(showBackground = true)
@Composable
private fun ScannedDeviceItemPreview() {
    ScannedDeviceItem(
        device = ScannedDevice(address = "AA:BB:CC:DD:EE:FF", name = "hgs", rssi = -52)
    )
}