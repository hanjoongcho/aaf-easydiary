package me.blog.korn123.easydiary.ui.components

import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.config

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EasyDiaryActionBar(textUnit: TextUnit, close: () -> Unit) {
    val isDarkMode = isSystemInDarkTheme()
    val context = LocalContext.current as ComponentActivity

    DisposableEffect(isDarkMode) {
        context.enableEdgeToEdge(
            statusBarStyle = if (!isDarkMode) {
//                SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
                SystemBarStyle.auto(context.config.primaryColor, context.config.primaryColor)
            } else {
                SystemBarStyle.auto(context.config.primaryColor, context.config.primaryColor)
            },
            navigationBarStyle = if(!isDarkMode){
                SystemBarStyle.auto(context.config.primaryColor, context.config.primaryColor)
            } else {
                SystemBarStyle.auto(context.config.primaryColor, context.config.primaryColor)
            }
        )

        onDispose { }
    }

    TopAppBar(
        title = {
            Text(
                text = "내 액션바",
                style = TextStyle(
                    fontFamily = FontUtils.getComposeFontFamily(LocalContext.current),
                    color = Color.White,
                    fontSize = TextUnit(textUnit.value, TextUnitType.Sp),
                ),
            )
        },
        actions = {
            IconButton(onClick = { close.invoke() }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_cross),
                    contentDescription = "액션 아이콘"
                )
            }
        },
        colors = TopAppBarColors(
            Color(LocalContext.current.config.primaryColor),
            Color(LocalContext.current.config.primaryColor),
            Color.White,
            Color.White,
            Color.White,
        )
    )
}