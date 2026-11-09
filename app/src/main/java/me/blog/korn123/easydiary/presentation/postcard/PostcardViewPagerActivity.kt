package me.blog.korn123.easydiary.presentation.postcard

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import me.blog.korn123.easydiary.extensions.shareFile
import me.blog.korn123.easydiary.helper.MIME_TYPE_JPEG
import me.blog.korn123.easydiary.helper.POSTCARD_SEQUENCE
import me.blog.korn123.easydiary.presentation.base.EasyDiaryComposeBaseActivity
import me.blog.korn123.easydiary.presentation.postcard.components.PostcardViewPagerScreen
import me.blog.korn123.easydiary.presentation.theme.AppTheme

@AndroidEntryPoint
class PostcardViewPagerActivity : EasyDiaryComposeBaseActivity() {
    private val postcardViewPagerViewModel: PostcardViewPagerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sequence = intent.getIntExtra(POSTCARD_SEQUENCE, 0)
        postcardViewPagerViewModel.initData(sequence)

        setContent {
            AppTheme {
                val uiState by postcardViewPagerViewModel.uiState.collectAsStateWithLifecycle()

                PostcardViewPagerScreen(
                    postcardFiles = uiState.postcardFiles,
                    initialPosition = uiState.initialPosition,
                    isLoading = uiState.isLoading,
                    onShareClick = { file ->
                        shareFile(file, MIME_TYPE_JPEG)
                    },
                    onCloseClick = {
                        finishActivityWithTransition()
                    },
                )
            }
        }
    }
}
