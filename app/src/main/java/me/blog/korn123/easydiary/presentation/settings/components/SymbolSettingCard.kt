package me.blog.korn123.easydiary.presentation.settings.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import me.blog.korn123.commons.utils.FlavorUtils
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.helper.ComposeConstants.HORIZONTAL_PADDING
import me.blog.korn123.easydiary.helper.ComposeConstants.ROUNDED_CORNER_SHAPE_SIZE
import me.blog.korn123.easydiary.presentation.components.SimpleText
import me.blog.korn123.easydiary.presentation.dev.BaseDevViewModel
import me.blog.korn123.easydiary.utils.FontUtils

/***************************************************************************************************
 *   Symbol Setting Card
 *
 ***************************************************************************************************/

@Composable
fun SymbolCard(
    modifier: Modifier,
    viewModel: BaseDevViewModel,
    fontFamily: FontFamily? =
        if (LocalInspectionMode.current) {
            null
        } else {
            FontUtils.getComposeFontFamily(
                LocalContext.current,
            )
        },
    callback: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(ROUNDED_CORNER_SHAPE_SIZE.dp),
        colors = CardDefaults.cardColors(Color(LocalContext.current.config.backgroundColor)),
        modifier =
            (
                if (LocalContext.current.config.enableCardViewPolicy) {
                    modifier.padding(
                        HORIZONTAL_PADDING.dp,
                        HORIZONTAL_PADDING.dp,
                    )
                } else {
                    modifier.padding(1.dp, 1.dp)
                }
            ).clickable {
                callback.invoke()
            },
        elevation = CardDefaults.cardElevation(defaultElevation = ROUNDED_CORNER_SHAPE_SIZE.dp),
    ) {
        Row(
            modifier = Modifier.padding(15.dp),
        ) {
            SimpleText(
                text = viewModel.symbol.toString(),
                fontFamily = fontFamily,
            )
            Image(
                painter = painterResource(id = FlavorUtils.sequenceToSymbolResourceId(viewModel.symbol)),
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                contentScale = ContentScale.Crop,
            )
        }
    }
}
