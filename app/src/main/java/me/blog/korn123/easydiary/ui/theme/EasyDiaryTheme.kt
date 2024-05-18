package me.blog.korn123.easydiary.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import me.blog.korn123.commons.utils.ColorUtils
import me.blog.korn123.easydiary.extensions.config


val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)


@Composable
fun AppTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    // Material 3 color schemes
    val DarkColorScheme = darkColorScheme(
        primary = Purple80,
        secondary = PurpleGrey80,
        tertiary = Pink80
    )

    val LightColorScheme = lightColorScheme(
        primary = Color(ColorUtils.adjustAlpha(LocalContext.current.config.primaryColor, 1f)),
        secondary = Color(ColorUtils.adjustAlpha(LocalContext.current.config.primaryColor, 1f)),
        tertiary = Color(ColorUtils.adjustAlpha(LocalContext.current.config.primaryColor, 1f)),
    )

    val replyColorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    MaterialTheme(
        colorScheme = replyColorScheme,
        content = content
    )
}