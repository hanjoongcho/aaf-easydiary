package me.blog.korn123.easydiary.presentation.photoviewer

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import me.blog.korn123.easydiary.extensions.shareFile
import me.blog.korn123.easydiary.helper.DIARY_ATTACH_PHOTO_INDEX
import me.blog.korn123.easydiary.helper.DIARY_SEQUENCE
import me.blog.korn123.easydiary.helper.MIME_TYPE_JPEG
import me.blog.korn123.easydiary.helper.TransitionConstants
import me.blog.korn123.easydiary.presentation.base.EasyDiaryComposeBaseActivity
import me.blog.korn123.easydiary.presentation.photoviewer.components.PhotoViewPagerScreen
import me.blog.korn123.easydiary.presentation.theme.AppTheme
import me.blog.korn123.easydiary.utils.EasyDiaryUtils
import java.io.File

@AndroidEntryPoint
class PhotoViewPagerActivity : EasyDiaryComposeBaseActivity() {
    private val photoViewPagerViewModel: PhotoViewPagerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val diarySequence = intent.getLongExtra(DIARY_SEQUENCE, 0)
        val photoIndex = intent.getIntExtra(DIARY_ATTACH_PHOTO_INDEX, 0)
        photoViewPagerViewModel.initData(diarySequence, photoIndex)

        setContent {
            AppTheme {
                val uiState by photoViewPagerViewModel.uiState.collectAsStateWithLifecycle()

                PhotoViewPagerScreen(
                    diary = uiState.diary,
                    initialPosition = uiState.initialPosition,
                    isLoading = uiState.isLoading,
                    onShareClick = { photoUri ->
                        uiState.diary?.let { diary ->
                            val filePath = EasyDiaryUtils.getApplicationDataDirectory(this@PhotoViewPagerActivity) + photoUri.getFilePath()
                            shareFile(File(filePath), photoUri.mimeType ?: MIME_TYPE_JPEG)
                        }
                    },
                    onCloseClick = {
                        finishActivityWithTransition()
                    },
                )
            }
        }
    }
}
