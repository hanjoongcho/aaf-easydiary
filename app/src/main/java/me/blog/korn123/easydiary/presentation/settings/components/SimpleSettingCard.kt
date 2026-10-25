package me.blog.korn123.easydiary.presentation.settings.components

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.glide.GlideImage
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.helper.ComposeConstants.HORIZONTAL_PADDING
import me.blog.korn123.easydiary.helper.ComposeConstants.ROUNDED_CORNER_SHAPE_SIZE
import me.blog.korn123.easydiary.helper.ComposeConstants.VERTICAL_PADDING
import me.blog.korn123.easydiary.presentation.components.SimpleText
import me.blog.korn123.easydiary.utils.FontUtils

/***************************************************************************************************
 *   Base Composable & Simple Setting Card
 *
 ***************************************************************************************************/

@Composable
fun CardContainer(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val scrollState = rememberScrollState()
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .verticalScroll(scrollState),
    ) {
        content()
    }
}

@Composable
fun CategoryTitleCard(
    title: String,
    marginTop: Int = 6,
    fontFamily: FontFamily? =
        if (LocalInspectionMode.current) {
            null
        } else {
            FontUtils.getComposeFontFamily(
                LocalContext.current,
            )
        },
) {
    val modifier = Modifier.fillMaxWidth()
    Card(
        shape = RoundedCornerShape(2.dp),
        colors = CardDefaults.cardColors(Color(LocalContext.current.config.primaryColor)),
        modifier = (
            if (LocalContext.current.config.enableCardViewPolicy) {
                modifier.padding(
                    start = 3.dp,
                    top = marginTop.plus(3).dp,
                    end = 3.dp,
                    bottom = 3.dp,
                )
            } else {
                modifier.padding(
                    start = 1.dp,
                    top = marginTop.plus(1).dp,
                    end = 1.dp,
                    bottom = 1.dp,
                )
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(15.dp, 5.dp),
        ) {
            SimpleText(
                text = title,
                fontWeight = FontWeight.Bold,
                fontColor = Color.White,
                fontFamily = fontFamily,
            )
        }
    }
}

@Composable
fun SimpleCard(
    title: String,
    description: String?,
    subDescription: String? = null,
    modifier: Modifier,
    enableCardViewPolicy: Boolean = LocalContext.current.config.enableCardViewPolicy,
    fontSize: Float = LocalContext.current.config.settingFontSize,
    fontFamily: FontFamily? =
        if (LocalInspectionMode.current) {
            null
        } else {
            FontUtils.getComposeFontFamily(
                LocalContext.current,
            )
        },
    lineSpacingScaleFactor: Float = LocalContext.current.config.lineSpacingScaleFactor,
    isBoldTitle: Boolean = true,
    onLongClick: () -> Unit = {},
    onClick: () -> Unit = {},
) {
    SimpleCardWithImage(
        title = title,
        description = description,
        subDescription = subDescription,
        imageResourceId = null,
        modifier = modifier,
        enableCardViewPolicy = enableCardViewPolicy,
        fontSize = fontSize,
        fontFamily = fontFamily,
        lineSpacingScaleFactor = lineSpacingScaleFactor,
        isBoldTitle = isBoldTitle,
        onLongClick = onLongClick,
        onClick = onClick,
    )
}

@Composable
fun SimpleCardWithImage(
    title: String,
    description: String?,
    subDescription: String? = null,
    imageResourceId: Int?,
    imageUrl: Uri? = null,
    modifier: Modifier,
    enableCardViewPolicy: Boolean = LocalContext.current.config.enableCardViewPolicy,
    fontSize: Float = LocalContext.current.config.settingFontSize,
    fontFamily: FontFamily? =
        if (LocalInspectionMode.current) {
            null
        } else {
            FontUtils.getComposeFontFamily(
                LocalContext.current,
            )
        },
    lineSpacingScaleFactor: Float = LocalContext.current.config.lineSpacingScaleFactor,
    isBoldTitle: Boolean = true,
    onLongClick: () -> Unit = {},
    onClick: () -> Unit = {},
) {
    Card(
        shape = RoundedCornerShape(ROUNDED_CORNER_SHAPE_SIZE.dp),
        colors = CardDefaults.cardColors(Color(LocalContext.current.config.backgroundColor)),
        modifier =
            (
                if (enableCardViewPolicy) {
                    modifier.padding(
                        HORIZONTAL_PADDING.dp,
                        VERTICAL_PADDING.dp,
                    )
                } else {
                    modifier
                        .padding(1.dp, 1.dp)
                }
            ).combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = ROUNDED_CORNER_SHAPE_SIZE.dp),
    ) {
        Column(
            modifier = Modifier.padding(15.dp),
        ) {
            Row(
                modifier = Modifier,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SimpleText(
                    text = title,
                    fontWeight = if (isBoldTitle) FontWeight.Bold else FontWeight.Normal,
                    fontSize = fontSize,
                    fontFamily = fontFamily,
                    lineSpacingScaleFactor = lineSpacingScaleFactor,
                )
            }

            if (imageResourceId != null || description != null) {
                Row(
                    modifier = Modifier.padding(0.dp, 5.dp, 0.dp, 0.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    imageResourceId?.let {
                        val model =
                            imageUrl
                                ?: "android.resource://${LocalContext.current.packageName}/$imageResourceId"
                        GlideImage(
                            imageModel = { model },
                            imageOptions =
                                ImageOptions(
                                    contentScale = ContentScale.Fit,
                                ),
                            modifier =
                                Modifier
                                    .size(32.dp),
                            failure = {
                                Image(
                                    painter = painterResource(imageResourceId),
                                    contentDescription = "Load failed",
                                )
                            },
                        )

                        Spacer(modifier = Modifier.width(10.dp))
                    }
                    description?.run {
                        SimpleText(
                            text = description,
                            alpha = 0.7f,
                            fontSize = fontSize,
                            fontFamily = fontFamily,
                            lineSpacingScaleFactor = lineSpacingScaleFactor,
                        )
                    }
                }
            }

            subDescription?.run {
                Row(
                    modifier = Modifier.padding(0.dp, 5.dp, 0.dp, 0.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SimpleText(
                        text = subDescription,
                        alpha = 0.7f,
                        fontSize = fontSize,
                        fontFamily = fontFamily,
                        lineSpacingScaleFactor = lineSpacingScaleFactor,
                    )
                }
            }
        }
    }
}
