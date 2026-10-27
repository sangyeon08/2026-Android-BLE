package com.example.androidble

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.androidble.destination.AppDestinations
import com.example.androidble.ui.components.StatusBar
import com.example.androidble.ui.screens.HsgTestScreen
import com.example.androidble.ui.screens.ScanScreen
import com.example.androidble.ui.theme.AndroidBleTheme
import com.example.androidble.util.effect.rememberPermissionState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        hideSystemStatusBar()

        setContent {
            BleApp()
        }
    }

    private fun hideSystemStatusBar() {
        try {
            val windowInsetsController =
                WindowCompat.getInsetsController(window, window.decorView)

            // 1. 상단 스와이프 시 일시적으로 시스템 상태바 표시 후 다시 자동 숨김
            windowInsetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

            // 2. 시스템 상태바 숨기기
            windowInsetsController.hide(WindowInsetsCompat.Type.statusBars())
        } catch (ignored: Exception) {
        }
    }
}

@Composable
fun BleApp() {
    var currentDestination by rememberSaveable {
        mutableStateOf(AppDestinations.SCAN_CONNECT)
    }

    // StatusBar, ScanScreen이 함께 사용하도록 최상위에서 한 번만 생성
    val permissionState = rememberPermissionState()

    AndroidBleTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            StatusBar(permissionState = permissionState)

            NavigationSuiteScaffold(
                modifier = Modifier.weight(1f),
                navigationSuiteItems = {
                    AppDestinations.entries.forEach { destination ->
                        item(
                            icon = {
                                androidx.compose.material3.Icon(
                                    imageVector = destination.icon,
                                    contentDescription = null
                                )
                            },
                            label = {
                                androidx.compose.material3.Text(
                                    text = androidx.compose.ui.res.stringResource(
                                        destination.label
                                    )
                                )
                            },
                            selected = currentDestination == destination,
                            onClick = {
                                currentDestination = destination
                            }
                        )
                    }
                }
            ) {
                when (currentDestination) {
                    AppDestinations.SCAN_CONNECT -> ScanScreen(permissionState = permissionState)
                    AppDestinations.HSG_TEST -> HsgTestScreen()
                }
            }
        }
    }
}