package me.blog.korn123.easydiary.presentation.gallery

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import me.blog.korn123.easydiary.activities.DiaryReadingActivity
import me.blog.korn123.easydiary.extensions.makeToast
import me.blog.korn123.easydiary.extensions.shareFile
import me.blog.korn123.easydiary.helper.DIARY_SEQUENCE
import me.blog.korn123.easydiary.helper.MIME_TYPE_JPEG
import me.blog.korn123.easydiary.helper.POSTCARD_SEQUENCE
import me.blog.korn123.easydiary.helper.TransitionHelper
import me.blog.korn123.easydiary.presentation.base.EasyDiaryComposeBaseActivity
import me.blog.korn123.easydiary.presentation.gallery.components.GalleryViewPagerScreen
import me.blog.korn123.easydiary.presentation.theme.AppTheme

@AndroidEntryPoint
class GalleryViewPagerActivity : EasyDiaryComposeBaseActivity() {
    private val galleryViewPagerViewModel: GalleryViewPagerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sequence = intent.getIntExtra(POSTCARD_SEQUENCE, 0)
        galleryViewPagerViewModel.initData(sequence)

        setContent {
            AppTheme {
                val uiState by galleryViewPagerViewModel.uiState.collectAsStateWithLifecycle()

                GalleryViewPagerScreen(
                    attachedPhotos = uiState.attachedPhotos,
                    initialPosition = uiState.initialPosition,
                    isLoading = uiState.isLoading,
                    onShareClick = { attachedPhoto ->
                        if (attachedPhoto.diary == null) {
                            shareFile(attachedPhoto.file, MIME_TYPE_JPEG)
                        } else {
                            if (attachedPhoto.diary.isEncrypt) {
                                makeToast("Encrypted files cannot be shared.")
                            } else {
                                shareFile(attachedPhoto.file, MIME_TYPE_JPEG)
                            }
                        }
                    },
                    onDiaryClick = { attachedPhoto ->
                        attachedPhoto.diary?.let { diary ->
                            TransitionHelper.startActivityWithTransition(
                                this@GalleryViewPagerActivity,
                                Intent(
                                    this@GalleryViewPagerActivity,
                                    DiaryReadingActivity::class.java,
                                ).apply {
                                    putExtra(DIARY_SEQUENCE, diary.diaryId)
                                },
                            )
                        } ?: run {
                            makeToast("There is no linked diary information.")
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
