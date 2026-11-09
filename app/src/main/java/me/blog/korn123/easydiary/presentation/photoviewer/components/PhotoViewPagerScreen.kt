package me.blog.korn123.easydiary.presentation.photoviewer.components

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
import androidx.compose.runtime.mutableStateMapOf
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
import me.blog.korn123.easydiary.domain.model.Diary
import me.blog.korn123.easydiary.domain.model.PhotoUri
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.dpToPixel
import me.blog.korn123.easydiary.presentation.components.EasyDiaryActionBar
import me.blog.korn123.easydiary.presentation.theme.AppTheme
import me.blog.korn123.easydiary.utils.EasyDiaryUtils
import me.blog.korn123.easydiary.utils.FontUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoViewPagerScreen(
    diary: Diary?,
    initialPosition: Int,
    isLoading: Boolean,
    onShareClick: (PhotoUri) -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val photoUris = diary?.photoUrisWithEncryptionPolicy() ?: emptyList()
    val totalCount = photoUris.size
    val safeInitialPage = if (initialPosition in photoUris.indices) initialPosition else 0
    val pagerState =
        rememberPagerState(
            initialPage = safeInitialPage,
            pageCount = { photoUris.size },
        )

    val rotations = remember { mutableStateMapOf<Int, Float>() }
    val currentRotation = rotations[pagerState.currentPage] ?: 0f

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
                useCloseIcon = false,
                actions = {
                    IconButton(
                        onClick = {
                            if (pagerState.currentPage in photoUris.indices) {
                                onShareClick(photoUris[pagerState.currentPage])
                            }
                        },
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_sharethis_w),
                            contentDescription = "Share",
                            tint = Color.White,
                        )
                    }
                    IconButton(
                        onClick = {
                            val page = pagerState.currentPage
                            val nextRotation = (rotations[page] ?: 0f) + 90f
                            rotations[page] = nextRotation
                        },
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_rotate),
                            contentDescription = "Rotate",
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
            } else if (photoUris.isNotEmpty()) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                ) { page ->
                    val photoUri = photoUris[page]
                    val imageFilePath = EasyDiaryUtils.getApplicationDataDirectory(context) + photoUri.getFilePath()
                    val bitmap =
                        remember(imageFilePath, diary?.isEncrypt) {
                            if (diary?.isEncrypt == true) {
                                null
                            } else {
                                BitmapFactory.decodeFile(imageFilePath)
                            }
                        }

                    if (bitmap != null) {
                        AndroidView(
                            modifier = Modifier.fillMaxSize(),
                            factory = { viewContext ->
                                PhotoView(viewContext).apply {
                                    setImageBitmap(bitmap)
                                    rotation = currentRotation
                                }
                            },
                            update = { photoView ->
                                photoView.setImageBitmap(bitmap)
                                photoView.rotation = rotations[page] ?: 0f
                            },
                            onRelease = { photoView ->
                                photoView.setImageDrawable(null)
                                photoView.clearAnimation()
                                photoView.animate().cancel()
                            },
                        )
                    } else {
                        val errorMessage =
                            if (diary?.isEncrypt == true) {
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
private fun PhotoViewPagerScreenPreview() {
    AppTheme {
        PhotoViewPagerScreen(
            diary = null,
            initialPosition = 0,
            isLoading = false,
            onShareClick = {},
            onCloseClick = {},
        )
    }
}
