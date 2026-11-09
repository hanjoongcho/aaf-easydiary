package me.blog.korn123.easydiary.presentation.markdown

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import me.blog.korn123.easydiary.extensions.EMPTY
import me.blog.korn123.easydiary.extensions.checkPermission
import me.blog.korn123.easydiary.extensions.confirmPermission
import me.blog.korn123.easydiary.extensions.makeSnackBar
import me.blog.korn123.easydiary.extensions.makeToast
import me.blog.korn123.easydiary.extensions.pauseLock
import me.blog.korn123.easydiary.helper.EXTERNAL_STORAGE_PERMISSIONS
import me.blog.korn123.easydiary.helper.MarkdownConstants
import me.blog.korn123.easydiary.helper.REQUEST_CODE_EXTERNAL_STORAGE_WITH_MARKDOWN
import me.blog.korn123.easydiary.presentation.base.EasyDiaryComposeBaseActivity
import me.blog.korn123.easydiary.presentation.markdown.components.MarkDownViewerScreen
import me.blog.korn123.easydiary.presentation.theme.AppTheme

@AndroidEntryPoint
class MarkDownViewerActivity : EasyDiaryComposeBaseActivity() {
    private val markDownViewModel: MarkDownViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pageTitle = intent.getStringExtra(MarkdownConstants.OPEN_URL_DESCRIPTION)
        val forceAppendCodeBlock = intent.getBooleanExtra(MarkdownConstants.FORCE_APPEND_CODE_BLOCK, true)
        val markdownUrl = intent.getStringExtra(MarkdownConstants.OPEN_URL_INFO) ?: String.EMPTY

        markDownViewModel.initData(
            pageTitle = pageTitle,
            markdownUrl = markdownUrl,
            forceAppendCodeBlock = forceAppendCodeBlock,
        )

        if (checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
            markDownViewModel.loadMarkdownFile()
        } else {
            confirmPermission(EXTERNAL_STORAGE_PERMISSIONS, REQUEST_CODE_EXTERNAL_STORAGE_WITH_MARKDOWN)
        }

        setContent {
            AppTheme {
                val uiState by markDownViewModel.uiState.collectAsStateWithLifecycle()

                LaunchedEffect(uiState.toastMessage) {
                    uiState.toastMessage?.let { message ->
                        makeToast(message)
                        markDownViewModel.clearToastMessage()
                    }
                }

                MarkDownViewerScreen(
                    pageTitle = uiState.pageTitle,
                    isLoading = uiState.isLoading,
                    markdownText = uiState.markdownText,
                    onRefreshClick = {
                        if (checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
                            markDownViewModel.refreshMarkdownFile()
                        } else {
                            confirmPermission(EXTERNAL_STORAGE_PERMISSIONS, REQUEST_CODE_EXTERNAL_STORAGE_WITH_MARKDOWN)
                        }
                    },
                    onCloseClick = {
                        finishActivityWithTransition()
                    },
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        pauseLock()
        when (requestCode) {
            REQUEST_CODE_EXTERNAL_STORAGE_WITH_MARKDOWN -> {
                if (checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
                    markDownViewModel.loadMarkdownFile()
                } else {
                    makeSnackBar("Permission denied")
                }
            }
        }
    }
}
