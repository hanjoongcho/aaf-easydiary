package me.blog.korn123.easydiary.presentation.fingerprintlock.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tbuonomo.viewpagerdotsindicator.dpToPx
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.presentation.components.SimpleText
import me.blog.korn123.easydiary.presentation.theme.AppTheme

@Composable
fun FingerprintLockScreen(
    guideMessage: String,
    isChangePinLockVisible: Boolean,
    onChangePinLock: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val backgroundColor = Color(context.config.primaryColor)

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(backgroundColor)
                .statusBarsPadding()
                .navigationBarsPadding(),
    ) {
        Column(
            modifier =
                Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_fingerprint),
                contentDescription = "Fingerprint",
                modifier = Modifier.size(72.dp),
            )
            Spacer(modifier = Modifier.height(16.dp))
            SimpleText(
                text = guideMessage,
                fontColor = Color.White,
                fontSize = 16.dp.dpToPx(),
                modifier = Modifier.padding(10.dp),
            )
        }

        if (isChangePinLockVisible) {
            Box(
                modifier =
                    Modifier
                        .align(Alignment.BottomEnd)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onChangePinLock() }
                        .padding(10.dp),
            ) {
                SimpleText(
                    text = stringResource(R.string.change_pin_lock_mode),
                    fontColor = Color.White,
                    fontSize = 14.dp.dpToPx(),
                )
            }
        }
    }
}

@Preview(name = "Fingerprint Unlock Mode", showBackground = true)
@Composable
private fun FingerprintLockScreenUnlockPreview() {
    AppTheme {
        FingerprintLockScreen(
            guideMessage = "Place your finger on the sensor",
            isChangePinLockVisible = true,
            onChangePinLock = {},
        )
    }
}

@Preview(name = "Fingerprint Setting Mode", showBackground = true)
@Composable
private fun FingerprintLockScreenSettingPreview() {
    AppTheme {
        FingerprintLockScreen(
            guideMessage = "Place your finger on the sensor",
            isChangePinLockVisible = false,
            onChangePinLock = {},
        )
    }
}
