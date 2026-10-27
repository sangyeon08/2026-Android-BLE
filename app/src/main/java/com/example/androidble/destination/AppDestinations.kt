package com.example.androidble.destination

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BackHand
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.History
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.androidble.R

enum class AppDestinations(
    @StringRes val label: Int,
    val icon: ImageVector,
    @StringRes val contentDescription: Int
) {
    SCAN_CONNECT(R.string.nav_scan_connect, Icons.Default.Bluetooth, R.string.nav_scan_connect),
    HSG_TEST(R.string.nav_hgs_test, Icons.Default.BackHand, R.string.nav_hgs_test)
}
