package com.example.androidble.util.effect

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
fun SystemBroadcastReceiver(
    systemAction: String,
    onSystemEvent: (intent: Intent?) -> Unit
) {
    val context = LocalContext.current
    val currentOnSystemEvent by rememberUpdatedState(onSystemEvent)

    DisposableEffect(context, systemAction) {
        val intentFilter = IntentFilter(systemAction)
        val broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                currentOnSystemEvent(intent)
            }
        } 
        
        // Android 14 (API 34+) 이상 호환성을 위해 ContextCompat.RECEIVER_EXPORTED 지정
        try {
            ContextCompat.registerReceiver(
                context,
                broadcastReceiver,
                intentFilter,
                ContextCompat.RECEIVER_EXPORTED
            )
        } catch (e: Exception) {
            try {
                context.registerReceiver(broadcastReceiver, intentFilter)
            } catch (ignored: Exception) {
            }
        }

        onDispose {
            try {
                context.unregisterReceiver(broadcastReceiver)
            } catch (ignored: Exception) {
            }
        }
    }
}