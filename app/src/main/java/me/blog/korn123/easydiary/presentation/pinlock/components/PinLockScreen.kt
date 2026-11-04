package me.blog.korn123.easydiary.presentation.pinlock.components

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.helper.PinLockConstants
import me.blog.korn123.easydiary.presentation.components.SimpleText
import me.blog.korn123.easydiary.presentation.theme.AppTheme

private val PinLockTextColor = Color(0xFF272626)

@Composable
fun PinLockScreen(
    pin: String,
    activityMode: String,
    isFingerprintEnabled: Boolean,
    onKeyClick: (String) -> Unit,
    onFingerprintClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val context = LocalContext.current
    val backgroundColor = Color(context.config.primaryColor)

    val infoMessageText =
        if (activityMode == PinLockConstants.ACTIVITY_SETTING) {
            stringResource(R.string.pin_setting_guide_message)
        } else {
            stringResource(R.string.pin_unlock_guide_message)
        }

    if (isLandscape) {
        Row(
            modifier =
                modifier
                    .fillMaxSize()
                    .background(backgroundColor)
                    .padding(5.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxHeight()
                        .weight(4f),
                contentAlignment = Alignment.Center,
            ) {
                LauncherInfoMessage(infoMessageText = infoMessageText)
            }

            Column(
                modifier =
                    Modifier
                        .fillMaxHeight()
                        .weight(6f),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                PinBoxesRow(
                    pin = pin,
                    activityMode = activityMode,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                )

                KeypadGrid(
                    isFingerprintEnabled = isFingerprintEnabled,
                    onKeyClick = onKeyClick,
                    onFingerprintClick = onFingerprintClick,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    } else {
        Column(
            modifier =
                modifier
                    .fillMaxSize()
                    .background(backgroundColor)
                    .padding(bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            PinBoxesRow(
                pin = pin,
                activityMode = activityMode,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp, start = 10.dp, end = 10.dp, bottom = 10.dp),
            )

            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                LauncherInfoMessage(infoMessageText = infoMessageText)
            }

            KeypadGrid(
                isFingerprintEnabled = isFingerprintEnabled,
                onKeyClick = onKeyClick,
                onFingerprintClick = onFingerprintClick,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun LauncherInfoMessage(
    infoMessageText: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_intro),
            contentDescription = null,
            modifier = Modifier.size(60.dp),
        )
        Spacer(modifier = Modifier.height(10.dp))
        SimpleText(
            text = infoMessageText,
            fontWeight = FontWeight.Bold,
            fontColor = Color.White,
            fontSize = 16f,
            modifier = Modifier.padding(10.dp),
        )
    }
}

@Composable
private fun PinBoxesRow(
    pin: String,
    activityMode: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        for (index in 0 until 4) {
            val digit = pin.getOrNull(index)
            val text =
                if (digit != null) {
                    if (activityMode == PinLockConstants.ACTIVITY_SETTING) {
                        digit.toString()
                    } else {
                        "-"
                    }
                } else {
                    ""
                }

            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .height(50.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color.White),
                contentAlignment = Alignment.Center,
            ) {
                SimpleText(
                    text = text,
                    fontWeight = FontWeight.Bold,
                    fontColor = PinLockTextColor,
                    fontSize = 20f,
                )
            }

            if (index < 3) {
                Spacer(modifier = Modifier.width(5.dp))
            }
        }
    }
}

@Composable
private fun KeypadGrid(
    isFingerprintEnabled: Boolean,
    onKeyClick: (String) -> Unit,
    onFingerprintClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        KeypadRow(
            keys = listOf("1", "2", "3"),
            onKeyClick = onKeyClick,
        )
        KeypadRow(
            keys = listOf("4", "5", "6"),
            onKeyClick = onKeyClick,
        )
        KeypadRow(
            keys = listOf("7", "8", "9"),
            onKeyClick = onKeyClick,
        )
        KeypadBottomRow(
            isFingerprintEnabled = isFingerprintEnabled,
            onKeyClick = onKeyClick,
            onFingerprintClick = onFingerprintClick,
        )
    }
}

@Composable
private fun KeypadRow(
    keys: List<String>,
    onKeyClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        keys.forEachIndexed { index, key ->
            KeypadButton(
                label = key,
                onClick = { onKeyClick(key) },
                modifier = Modifier.weight(1f),
            )
            if (index < keys.size - 1) {
                Spacer(modifier = Modifier.width(5.dp))
            }
        }
    }
}

@Composable
private fun KeypadBottomRow(
    isFingerprintEnabled: Boolean,
    onKeyClick: (String) -> Unit,
    onFingerprintClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isFingerprintEnabled) {
            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .height(50.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color.White)
                        .clickable { onFingerprintClick() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_fingerprint),
                    contentDescription = "Fingerprint Lock",
                    tint = PinLockTextColor,
                )
            }
        } else {
            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .height(50.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color.White),
            )
        }

        Spacer(modifier = Modifier.width(5.dp))

        KeypadButton(
            label = "0",
            onClick = { onKeyClick("0") },
            modifier = Modifier.weight(1f),
        )

        Spacer(modifier = Modifier.width(5.dp))

        KeypadButton(
            label = "←",
            onClick = { onKeyClick("delete") },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun KeypadButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .height(50.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color.White)
                .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        SimpleText(
            text = label,
            fontWeight = FontWeight.Bold,
            fontColor = PinLockTextColor,
            fontSize = 18f,
        )
    }
}

@Preview(name = "Portrait Unlock Mode", showBackground = true)
@Composable
private fun PinLockScreenPortraitPreview() {
    AppTheme {
        PinLockScreen(
            pin = "12",
            activityMode = PinLockConstants.ACTIVITY_UNLOCK,
            isFingerprintEnabled = true,
            onKeyClick = {},
            onFingerprintClick = {},
        )
    }
}

@Preview(name = "Landscape Setting Mode", showBackground = true, widthDp = 640, heightDp = 360)
@Composable
private fun PinLockScreenLandscapePreview() {
    AppTheme {
        PinLockScreen(
            pin = "123",
            activityMode = PinLockConstants.ACTIVITY_SETTING,
            isFingerprintEnabled = false,
            onKeyClick = {},
            onFingerprintClick = {},
        )
    }
}
