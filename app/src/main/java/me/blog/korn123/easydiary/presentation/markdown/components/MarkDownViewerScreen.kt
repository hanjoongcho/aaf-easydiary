package me.blog.korn123.easydiary.presentation.markdown.components

import android.util.TypedValue
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import io.noties.markwon.Markwon
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.ext.tables.TableTheme
import io.noties.markwon.utils.ColorUtils
import io.noties.markwon.utils.Dip
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.presentation.components.EasyDiaryActionBar
import me.blog.korn123.easydiary.presentation.theme.AppTheme
import android.graphics.Color as AndroidColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarkDownViewerScreen(
    pageTitle: String,
    isLoading: Boolean,
    markdownText: String,
    onRefreshClick: () -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            EasyDiaryActionBar(
                title = pageTitle,
                useCloseIcon = true,
                actions = {
                    IconButton(onClick = onRefreshClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_update),
                            contentDescription = "Update",
                            tint = Color.White,
                        )
                    }
                },
                close = onCloseClick,
            )
        },
        containerColor = Color(context.config.backgroundColor),
        modifier = modifier,
    ) { innerPadding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Color(context.config.backgroundColor)),
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier =
                        Modifier
                            .size(30.dp)
                            .align(Alignment.Center),
                    color = Color(context.config.primaryColor),
                )
            } else {
                AndroidView(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                    factory = { viewContext ->
                        TextView(viewContext).apply {
                            val paddingPx =
                                TypedValue
                                    .applyDimension(
                                        TypedValue.COMPLEX_UNIT_DIP,
                                        10f,
                                        viewContext.resources.displayMetrics,
                                    ).toInt()
                            setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
                            setTextColor(context.config.textColor)
                        }
                    },
                    update = { textView ->
                        textView.setTextColor(context.config.textColor)
                        Markwon
                            .builder(textView.context)
                            .usePlugin(
                                TablePlugin.create { builder: TableTheme.Builder ->
                                    val dip = Dip.create(textView.context)
                                    builder
                                        .tableBorderWidth(dip.toPx(2))
                                        .tableBorderColor(AndroidColor.BLACK)
                                        .tableCellPadding(dip.toPx(4))
                                        .tableHeaderRowBackgroundColor(ColorUtils.applyAlpha(AndroidColor.BLUE, 80))
                                },
                            ).build()
                            .setMarkdown(textView, markdownText)
                    },
                )
            }
        }
    }
}

@Preview
@Composable
private fun MarkDownViewerScreenPreview() {
    AppTheme {
        MarkDownViewerScreen(
            pageTitle = "Markdown Preview",
            isLoading = false,
            markdownText = "# Hello Markdown\nThis is a sample markdown text.",
            onRefreshClick = {},
            onCloseClick = {},
        )
    }
}
