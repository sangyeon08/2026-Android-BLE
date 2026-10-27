package com.example.androidble.util.effect

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.androidble.util.PermissionUtil

//앱 전체의 필수 권한 상태를 관찰하고 요청을 관리하는 State 래퍼 클래스

class PermissionState(
    val checkAndRequestPermission: (permission: String, onGranted: () -> Unit) -> Unit
)

@Composable
fun rememberPermissionState(): PermissionState {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // 현재 모든 필수 권한이 부여되었는지 저장하는 State
    var isAllGranted by remember {
        mutableStateOf(PermissionUtil.hasAllPermissions(context))
    }

    // 다중 권한 요청
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsResult ->
        // 모든 요청 권한이 허용인지 확인
        isAllGranted = permissionsResult.values.all { it }
    }

    // 권한 요청 함수
    val requestPermissions = {
        launcher.launch(PermissionUtil.getRequiredPermissions())
    }

    // 개별 아이콘 클릭 시 권한 체크랑 요청,설정 이동 처리 함수
    val checkAndRequestPermission = { permission: String, onGranted: () -> Unit ->
        if (PermissionUtil.hasPermission(context, permission)) {
            // 권한이 이미 허용된 상태 ➔ 전달받은 설정 화면 이동 등의 동작 수행
            onGranted()
        } else {
            val activity = context as? Activity
            if ((activity != null) && PermissionUtil.shouldShowRationale(activity, permission)) {
                // 1회 거절 ➔ 권한 요청 팝업 다시 표시
                requestPermissions()
            } else {
                // 권한이 없거나 2회 이상 거절 상태 ➔ 앱 상세 설정 화면으로 이동
                PermissionUtil.openAppSettings(context)
            }
        }
    }

    // 앱 시작할 때 아직 권한이 없으면 자동으로 권한 팝업 표시
    LaunchedEffect(Unit) {
        if (!isAllGranted) {
            requestPermissions()
        }
    }

    // 설정 화면 등 외부에서 앱으로 복귀 ON_RESUME 했을 때에도 즉시 권한 상태 잗동 갱신
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isAllGranted = PermissionUtil.hasAllPermissions(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    return PermissionState(
        checkAndRequestPermission = checkAndRequestPermission
    )
}
