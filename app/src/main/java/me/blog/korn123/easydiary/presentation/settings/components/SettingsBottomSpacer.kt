package me.blog.korn123.easydiary.presentation.settings.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.blog.korn123.easydiary.extensions.isVanillaIceCreamPlus

@Composable
fun SettingsBottomSpacer() {
    val bottomPadding =
        if (isVanillaIceCreamPlus()) {
            WindowInsets.navigationBars
                .asPaddingValues()
                .calculateBottomPadding()
        } else {
            0.dp
        }
    val spacerHeight = bottomPadding + 32.dp
    Spacer(modifier = Modifier.fillMaxWidth().height(spacerHeight))
}
