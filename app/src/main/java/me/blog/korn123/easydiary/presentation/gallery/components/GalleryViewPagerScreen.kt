package me.blog.korn123.easydiary.presentation.gallery.components

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
import me.blog.korn123.easydiary.adapters.GalleryAdapter
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.dpToPixel
import me.blog.korn123.easydiary.presentation.components.EasyDiaryActionBar
import me.blog.korn123.easydiary.presentation.theme.AppTheme
import me.blog.korn123.easydiary.utils.FontUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryViewPagerScreen(
    attachedPhotos: List<GalleryAdapter.AttachedPhoto>,
    initialPosition: Int,
    isLoading: Boolean,
    onShareClick: (GalleryAdapter.AttachedPhoto) -> Unit,
    onDiaryClick: (GalleryAdapter.AttachedPhoto) -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val totalCount = attachedPhotos.size
    val safeInitialPage = if (initialPosition in attachedPhotos.indices) initialPosition else 0
    val pagerState = rememberPagerState(
        initialPage = safeInitialPage,
        pageCount = { attachedPhotos.size },
    )

    val currentTitle = if (totalCount > 0) {
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
                            if (pagerState.currentPage in attachedPhotos.indices) {
                                onDiaryClick(attachedPhotos[pagerState.currentPage])
                            }
                        },
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_note),
                            contentDescription = "Linked Diary",
                            tint = Color.White,
                        )
                    }
                    IconButton(
                        onClick = {
                            if (pagerState.currentPage in attachedPhotos.indices) {
                                onShareClick(attachedPhotos[pagerState.currentPage])
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
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(context.config.screenBackgroundColor)),
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(30.dp)
                        .align(Alignment.Center),
                    color = Color(context.config.primaryColor),
                )
            } else if (attachedPhotos.isNotEmpty()) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                ) { page ->
                    val attachedPhoto = attachedPhotos[page]
                    val bitmap = remember(attachedPhoto) {
                        if (attachedPhoto.diary?.isEncrypt == true) {
                            null
                        } else {
                            BitmapFactory.decodeFile(attachedPhoto.file.path)
                        }
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
                        val errorMessage = if (attachedPhoto.diary?.isEncrypt == true) {
                            "The diary is encrypted. You will need to decrypt the diary to see the attached photos."
                        } else {
                            stringResource(id = R.string.photo_view_error_info)
                        }
                        AndroidView(
                            modifier = Modifier.fillMaxSize(),
                            factory = { viewContext ->
                                TextView(viewContext).apply {
                                    gravity = android.view.Gravity.CENTER
                                    val padding = viewContext.dpToPixel(10F)
                                    setPadding(padding, padding, padding, padding)
                                    typeface = FontUtils.getCommonTypeface(viewContext)
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
private fun GalleryViewPagerScreenPreview() {
    AppTheme {
        GalleryViewPagerScreen(
            attachedPhotos = emptyList(),
            initialPosition = 0,
            isLoading = false,
            onShareClick = {},
            onDiaryClick = {},
            onCloseClick = {},
        )
    }
}
