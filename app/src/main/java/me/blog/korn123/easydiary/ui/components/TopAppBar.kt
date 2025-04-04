package me.blog.korn123.easydiary.ui.components

import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.config

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EasyDiaryActionBar(title: String? = null, subTitle: String, scrollBehavior: TopAppBarScrollBehavior? = null, close: () -> Unit) {
    val isDarkMode = isSystemInDarkTheme()
    val context = LocalContext.current

    val pixelValue = context.config.settingFontSize
    val density = LocalDensity.current
    val currentTextUnit = with (density) {
        val temp = pixelValue.toDp()
        temp.toSp()
    }

    TopAppBar(
        scrollBehavior = scrollBehavior,
        title = {
           Column {
               title?.let {
                   Text(
                       text = title,
                       style = TextStyle(
                           fontFamily = if (LocalInspectionMode.current) null else FontUtils.getComposeFontFamily(LocalContext.current),
                           color = Color.White,
                           fontSize = TextUnit(currentTextUnit.value.times(1.0F), TextUnitType.Sp),
                       ),
                       modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 5.dp)
                   )
               }
               Text(
                   text = subTitle,
                   style = TextStyle(
                       fontFamily = if (LocalInspectionMode.current) null else FontUtils.getComposeFontFamily(LocalContext.current),
                       color = Color.White,
                       fontSize = TextUnit(currentTextUnit.value.times(0.9F), TextUnitType.Sp),
                   ),
//                   modifier = Modifier
//                       .fillMaxWidth()
//                       .wrapContentWidth(Alignment.End)
               )
           }
        },
        navigationIcon = {
            IconButton(onClick = { close.invoke() }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_cross),
                    contentDescription = "액션 아이콘"
                )
            }
        },
//        actions = {
//            IconButton(onClick = { close.invoke() }) {
//                Icon(
//                    painter = painterResource(id = R.drawable.ic_cross),
//                    contentDescription = "액션 아이콘"
//                )
//            }
//        },
        colors = TopAppBarColors(
            Color(LocalContext.current.config.primaryColor),
            Color(LocalContext.current.config.primaryColor),
            Color.White,
            Color.White,
            Color.White,
        )
    )
}