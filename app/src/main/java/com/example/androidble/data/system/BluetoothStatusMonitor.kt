package com.example.androidble.data.system

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.androidble.ui.models.BluetoothState
import com.example.androidble.ui.models.BluetoothStatus
import com.example.androidble.util.PermissionUtil
import com.example.androidble.util.effect.SystemBroadcastReceiver

@Composable
fun rememberBluetoothState(): BluetoothState {
    val context = LocalContext.current

    val bluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager

    val bluetoothAdapter = try {
        bluetoothManager?.adapter
    } catch (e: Exception) {
        null
    }

    fun checkBluetoothPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PermissionUtil.hasPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            true
        }
    }

    fun isBluetoothEnabled(): Boolean {
        return try {
            bluetoothAdapter?.isEnabled == true
        } catch (e: SecurityException) {
            false
        } catch (e: Exception) {
            false
        }
    }

    var bluetoothState by remember {
        mutableStateOf(
            BluetoothState(
                status = when {
                    bluetoothAdapter == null ->
                        BluetoothStatus.NOT_SUPPORTED

                    isBluetoothEnabled() ->
                        BluetoothStatus.ON

                    else ->
                        BluetoothStatus.OFF
                },
                hasPermission = checkBluetoothPermission()
            )
        )
    }

    SystemBroadcastReceiver(
        systemAction = BluetoothAdapter.ACTION_STATE_CHANGED
    ) { intent ->
        val state = intent?.getIntExtra(
            BluetoothAdapter.EXTRA_STATE,
            BluetoothAdapter.STATE_OFF
        )

        bluetoothState = bluetoothState.copy(
            status = when (state) {
                BluetoothAdapter.STATE_ON ->
                    BluetoothStatus.ON

                BluetoothAdapter.STATE_TURNING_ON ->
                    BluetoothStatus.CONNECTING

                BluetoothAdapter.STATE_OFF,
                BluetoothAdapter.STATE_TURNING_OFF ->
                    BluetoothStatus.OFF

                else ->
                    BluetoothStatus.OFF
            },
            hasPermission = checkBluetoothPermission()
        )
    }

    return bluetoothState
}