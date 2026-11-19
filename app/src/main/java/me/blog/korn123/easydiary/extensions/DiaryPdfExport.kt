package me.blog.korn123.easydiary.extensions

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import me.blog.korn123.commons.utils.FlavorUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.domain.model.Diary
import me.blog.korn123.easydiary.domain.model.DiaryPdfSnapshot
import me.blog.korn123.easydiary.domain.model.PhotoUri
import me.blog.korn123.easydiary.enums.DialogMode
import me.blog.korn123.easydiary.helper.FILE_URI_PREFIX
import me.blog.korn123.easydiary.helper.MIME_TYPE_PDF
import me.blog.korn123.easydiary.utils.DateUtils
import me.blog.korn123.easydiary.utils.EasyDiaryUtils

/**
 * Copies the fields the PDF renderer needs out of [Diary] on the calling (main) thread.
 *
 * Photo files are resolved to absolute paths here as well, so the background renderer only works
 * with plain strings and never touches the repository model.
 */
fun Diary.toPdfSnapshot(context: Context): DiaryPdfSnapshot {
    val appDataDirectory = EasyDiaryUtils.getApplicationDataDirectory(context)
    val dateString =
        when (isAllDay) {
            true -> DateUtils.getDateStringFromTimeMillis(currentTimeMillis)
            false -> DateUtils.getDateTimeStringForceFormatting(currentTimeMillis, context)
        }

    return DiaryPdfSnapshot(
        diaryId = diaryId,
        currentTimeMillis = currentTimeMillis,
        dateString = dateString,
        title = title,
        contents = contents,
        symbolSequence = symbolSequence,
        symbolLabel =
            when (symbolSequence) {
                0 -> ""
                else -> FlavorUtils.getDiarySymbolMap(context)[symbolSequence] ?: ""
            },
        photoPaths =
            when (isEncrypt) {
                true -> emptyList()
                false -> photoUris.mapNotNull { it.resolvePhotoPath(appDataDirectory) }
            },
        isEncrypt = isEncrypt,
    )
}

private fun PhotoUri.resolvePhotoPath(appDataDirectory: String): String? {
    val uri = photoUri?.takeIf { it.isNotEmpty() } ?: return null
    return when {
        uri.startsWith(FILE_URI_PREFIX) -> "/" + uri.removePrefix(FILE_URI_PREFIX).trimStart('/')
        uri.startsWith("/") -> uri
        isContentUri() -> null
        else -> appDataDirectory + getFilePath()
    }
}

/**
 * Shows the export summary with the photo inclusion choice.
 *
 * [encryptedEntriesWithoutContent] is the number of selected entries whose protected contents
 * cannot be decrypted in a batch export; single entry exports ask for the password instead and
 * pass 0.
 */
fun Activity.showPdfExportOptionsDialog(
    snapshots: List<DiaryPdfSnapshot>,
    encryptedEntriesWithoutContent: Int,
    onConfirm: (includePhotos: Boolean) -> Unit,
) {
    val hasPhotos = snapshots.any { it.photoPaths.isNotEmpty() }
    val hasEncryptedEntries = snapshots.any { it.isEncrypt }
    val message =
        buildString {
            append(
                when (snapshots.size) {
                    1 -> getString(R.string.pdf_export_confirm_single_message)
                    else -> getString(R.string.pdf_export_confirm_message, snapshots.size)
                },
            )
            if (hasPhotos) {
                append("\n")
                append(getString(R.string.pdf_export_choose_photos))
            }
            if (hasEncryptedEntries) {
                append("\n\n")
                append(getString(R.string.pdf_export_unencrypted_warning))
            }
            if (encryptedEntriesWithoutContent > 0) {
                append("\n\n")
                append(
                    getString(
                        R.string.pdf_export_encrypted_entries_warning,
                        encryptedEntriesWithoutContent,
                    ),
                )
            }
        }

    val positiveListener = DialogInterface.OnClickListener { _, _ -> onConfirm(hasPhotos) }
    val negativeListener = DialogInterface.OnClickListener { _, _ -> }
    val neutralListener =
        when (hasPhotos) {
            true -> DialogInterface.OnClickListener { _, _ -> onConfirm(false) }
            false -> null
        }

    showAlertDialog(
        message,
        positiveListener,
        negativeListener,
        neutralListener,
        DialogMode.INFO,
        true,
        getString(R.string.pdf_export_title),
        getString(if (hasPhotos) R.string.pdf_export_include_photos else R.string.ok),
        getString(R.string.cancel),
        getString(R.string.pdf_export_text_only),
    )
}

fun Activity.startPdfSaveLocation(
    fileName: String,
    launcher: ActivityResultLauncher<Intent>,
) {
    EasyDiaryUtils.writeFileWithSAF(fileName, MIME_TYPE_PDF, launcher)
}

fun Activity.showPdfExportResultDialog(
    uri: Uri,
    pageCount: Int,
) {
    showAlertDialog(
        getString(R.string.pdf_export_success, pageCount),
        { _, _ -> openPdfFile(uri) },
        { _, _ -> },
        { _, _ -> sharePdfFile(uri) },
        DialogMode.INFO,
        true,
        getString(R.string.pdf_export_title),
        getString(R.string.pdf_export_open),
        getString(R.string.pdf_export_close),
        getString(R.string.pdf_export_share),
    )
}

private fun Activity.openPdfFile(uri: Uri) {
    try {
        startActivity(
            Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, MIME_TYPE_PDF)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            },
        )
    } catch (_: ActivityNotFoundException) {
        makeSnackBar(getString(R.string.pdf_export_no_app_to_open))
    }
}

private fun Activity.sharePdfFile(uri: Uri) {
    try {
        val sendIntent =
            Intent(Intent.ACTION_SEND).apply {
                type = MIME_TYPE_PDF
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        startActivity(Intent.createChooser(sendIntent, getString(R.string.pdf_export_share)))
    } catch (_: ActivityNotFoundException) {
        makeSnackBar(getString(R.string.pdf_export_no_app_to_open))
    }
}
