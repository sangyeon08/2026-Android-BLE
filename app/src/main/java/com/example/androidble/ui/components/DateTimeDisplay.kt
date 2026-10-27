package com.example.androidble.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.example.androidble.data.system.rememberCurrentDateTime

@Composable
fun DateTimeDisplay(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    // 시스템 시간 이벤트에 따라 갱신되는 현재 시간 가져오기
    val currentDateTime = rememberCurrentDateTime()

    Column(
        modifier = modifier.clickable { onClick() }
    ) {
        Text(
            text = currentDateTime,
            fontWeight = FontWeight.Normal,
            color = Color.White
        )
    }
}