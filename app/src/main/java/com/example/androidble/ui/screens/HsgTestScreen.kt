package com.example.androidble.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun HsgTestScreen(viewModel: ScanViewModel = viewModel()) {
    val connectionState by viewModel.connectionState.collectAsState()
    val lastResponse by viewModel.lastResponse.collectAsState()
    val measureState by viewModel.measureState.collectAsState()
    val attempt by viewModel.attempt.collectAsState()
    val currentValue by viewModel.currentValue.collectAsState()
    val results by viewModel.results.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "연결 상태: $connectionState",
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Text(
            text = "마지막 응답: $lastResponse",
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$currentValue kg",
                style = MaterialTheme.typography.displayLarge
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { viewModel.startMeasurement() },
                enabled = connectionState == "CONNECTED" &&
                    (measureState == MeasureState.IDLE || measureState == MeasureState.FINISHED)
            ) {
                Text("측정 시작")
            }

            OutlinedButton(
                onClick = { viewModel.startMeasurement() },
                enabled = connectionState == "CONNECTED" &&
                    measureState == MeasureState.COMPLETED
            ) {
                Text("재측정")
            }

            Button(
                onClick = { viewModel.finishMeasurement() },
                enabled = measureState == MeasureState.COMPLETED
            ) {
                Text("측정 완료")
            }
        }

        Text(
            text = "측정 횟수: $attempt / $MAX_MEASURE_ATTEMPTS",
            modifier = Modifier.padding(horizontal = 16.dp),
            style = MaterialTheme.typography.bodyMedium
        )

        results.forEachIndexed { index, value ->
            Text(
                text = "${index + 1}회: $value kg",
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
