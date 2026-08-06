package me.blog.korn123.easydiary.ui.components

import android.content.pm.ActivityInfo
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import me.blog.korn123.easydiary.extensions.config

@Composable
fun LoadingScreen(message: String? = null) {
    val activity = LocalActivity.current

    DisposableEffect(Unit) {
        val originalOrientation = activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
        onDispose {
            activity?.requestedOrientation = originalOrientation
        }
    }

    // 배경을 약간 어둡게 하고 클릭을 방지하는 오버레이
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color(LocalContext.current.config.backgroundColor).copy(alpha = 0.7f)) // 반투명 배경
                .pointerInput(Unit) {},
        // 로딩 중 터치 이벤트 무시 (클릭 방지)
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(30.dp),
                color = Color(LocalContext.current.config.textColor),
                strokeWidth = 2.dp,
            )
            message?.let {
                Spacer(modifier = Modifier.height(16.dp))
                SimpleText(
                    text = it,
                )
            }
        }
    }
}
