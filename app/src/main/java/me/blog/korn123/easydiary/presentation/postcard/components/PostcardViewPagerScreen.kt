package me.blog.korn123.easydiary.presentation.postcard.components

import android.graphics.BitmapFactory
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.chrisbanes.photoview.PhotoView
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.dpToPixel
import me.blog.korn123.easydiary.presentation.components.EasyDiaryActionBar
import me.blog.korn123.easydiary.presentation.theme.AppTheme
import me.blog.korn123.easydiary.utils.FontUtils
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostcardViewPagerScreen(
    postcardFiles: List<File>,
    initialPosition: Int,
    isLoading: Boolean,
    onShareClick: (File) -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val totalCount = postcardFiles.size
    val safeInitialPage = if (initialPosition in postcardFiles.indices) initialPosition else 0
    val pagerState =
        rememberPagerState(
            initialPage = safeInitialPage,
            pageCount = { postcardFiles.size },
        )

    val currentTitle =
        if (totalCount > 0) {
            "${pagerState.currentPage + 1} / $totalCount"
        } else {
            "0 / 0"
        }

    Scaffold(
        topBar = {
            EasyDiaryActionBar(
                title = currentTitle,
                useCloseIcon = true,
                actions = {
                    IconButton(
                        onClick = {
                            if (pagerState.currentPage in postcardFiles.indices) {
                                onShareClick(postcardFiles[pagerState.currentPage])
                            }
                        },
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_sharethis_w),
                            contentDescription = "Share",
                            tint = Color.White,
                        )
                    }
                },
                close = onCloseClick,
            )
        },
        containerColor = Color(context.config.screenBackgroundColor),
        modifier = modifier,
    ) { innerPadding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Color(context.config.screenBackgroundColor)),
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier =
                        Modifier
                            .size(30.dp)
                            .align(Alignment.Center),
                    color = Color(context.config.primaryColor),
                )
            } else if (postcardFiles.isNotEmpty()) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                ) { page ->
                    val postcardFile = postcardFiles[page]
                    val bitmap =
                        remember(postcardFile) {
                            BitmapFactory.decodeFile(postcardFile.path)
                        }

                    if (bitmap != null) {
                        AndroidView(
                            modifier = Modifier.fillMaxSize(),
                            factory = { viewContext ->
                                PhotoView(viewContext).apply {
                                    setImageBitmap(bitmap)
                                }
                            },
                            update = { photoView ->
                                photoView.setImageBitmap(bitmap)
                            },
                            onRelease = { photoView ->
                                photoView.setImageDrawable(null)
                                photoView.clearAnimation()
                                photoView.animate().cancel()
                            },
                        )
                    } else {
                        val errorMessage = stringResource(id = R.string.photo_view_error_info)
                        AndroidView(
                            modifier = Modifier.fillMaxSize(),
                            factory = { viewContext ->
                                TextView(viewContext).apply {
                                    gravity = android.view.Gravity.CENTER
                                    val padding = viewContext.dpToPixel(10F)
                                    setPadding(padding, padding, padding, padding)
                                    FontUtils.setTypefaceDefault(this)
                                    text = errorMessage
                                }
                            },
                            update = { textView ->
                                textView.text = errorMessage
                            },
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun PostcardViewPagerScreenPreview() {
    AppTheme {
        PostcardViewPagerScreen(
            postcardFiles = emptyList(),
            initialPosition = 0,
            isLoading = false,
            onShareClick = {},
            onCloseClick = {},
        )
    }
}
