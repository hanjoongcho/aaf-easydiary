package me.blog.korn123.easydiary.utils

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import android.util.Log
import me.blog.korn123.easydiary.helper.AlarmConstants
import me.blog.korn123.easydiary.helper.EXTERNAL_FULL_BACKUP_TEMP_FILE_NAME
import me.blog.korn123.easydiary.helper.MIME_TYPE_ZIP
import java.io.File
import java.io.IOException

/**
 * The folder a scheduled full backup writes into, as the user picked it with `ACTION_OPEN_DOCUMENT_TREE`.
 *
 * Everything here works on the tree uri alone, because an unattended run has no activity to ask the user anything:
 * the read/write grant is persisted when the folder is picked ([takePersistablePermission]) and read back from
 * [ContentResolver.getPersistedUriPermissions] on every run. The check and the write are separate on purpose, so the
 * caller can tell "the destination cannot be used" apart from "the archive could not be written" and report it.
 */
object ExternalBackupFolder {
    private const val TAG = "ExternalBackupFolder"

    /** Why a stored folder cannot be written right now, or that it can. */
    enum class Status {
        /** The schedule points at a folder the app may read and write, and the folder still exists. */
        READY,

        /** No folder has been picked for this schedule yet. */
        NOT_CONFIGURED,

        /** The folder was picked once, but the grant is gone (cleared by the system, or the app was reinstalled). */
        PERMISSION_REVOKED,

        /** The grant is still there, but the folder itself no longer exists (deleted, or the volume was removed). */
        FOLDER_MISSING,
    }

    /**
     * Keeps the read and write access of a folder picked with `ACTION_OPEN_DOCUMENT_TREE` across reboots and app
     * starts, which is what lets a scheduled run write without any UI. Returns false when the provider does not offer
     * a persistable grant, in which case the folder cannot be used by a schedule.
     */
    fun takePersistablePermission(
        context: Context,
        folderUri: Uri,
    ): Boolean =
        runCatching {
            context.contentResolver.takePersistableUriPermission(
                folderUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
            )
            true
        }.onFailure {
            Log.w(TAG, "takePersistableUriPermission failed: $folderUri", it)
        }.getOrDefault(false)

    /**
     * Drops the grant of a folder the app no longer backs up to, so switching folders or deleting a schedule does not
     * pile up persisted grants in the app's grant table.
     */
    fun releasePersistablePermission(
        context: Context,
        folderUriString: String?,
    ) {
        val folderUri = folderUriString?.takeIf { it.isNotBlank() }?.let { runCatching { Uri.parse(it) }.getOrNull() } ?: return
        runCatching {
            context.contentResolver.releasePersistableUriPermission(
                folderUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
            )
        }.onFailure {
            // Not granted (already released, or granted for another mode): nothing to clean up.
            Log.i(TAG, "releasePersistableUriPermission skipped: $folderUri")
        }
    }

    /** Whether the stored folder of a schedule can be used by a run that starts now. */
    fun status(
        context: Context,
        folderUriString: String?,
    ): Status {
        val treeUri = parseTreeUri(folderUriString) ?: return Status.NOT_CONFIGURED
        if (!hasPersistedWritePermission(context, treeUri)) return Status.PERMISSION_REVOKED
        val folderDocumentUri = treeDocumentUri(treeUri) ?: return Status.FOLDER_MISSING
        return if (documentExists(context, folderDocumentUri)) Status.READY else Status.FOLDER_MISSING
    }

    /** Name of the folder as the file manager shows it, for the schedule edit screen. */
    fun displayName(
        context: Context,
        folderUriString: String?,
    ): String? {
        val treeUri = parseTreeUri(folderUriString) ?: return null
        val folderDocumentUri = treeDocumentUri(treeUri) ?: return null
        val queriedName =
            runCatching {
                context.contentResolver
                    .query(folderDocumentUri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                    ?.use { cursor ->
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameIndex != -1 && cursor.moveToFirst()) cursor.getString(nameIndex) else null
                    }
            }.onFailure {
                Log.w(TAG, "reading the folder name failed: $folderUriString", it)
            }.getOrNull()
        // A stored folder whose name cannot be read right now (the grant is gone, or the volume is removed) still gets
        // named after the last segment of its tree uri, so the edit screen does not claim it has no folder at all.
        return queriedName ?: treeUri.lastPathSegment?.substringAfterLast(':')?.takeIf { it.isNotBlank() }
    }

    /**
     * Writes [sourceFile] into the folder as [fileName], or replaces the file of that name.
     *
     * The archive is copied to a temporary document of the same folder first and only then renamed to its final name,
     * so a folder sync tool never sees a half-written archive. A document that already carries the final name is
     * deleted before the rename instead of being written through, because the documents provider would otherwise
     * leave both of them behind as "name (1).zip".
     *
     * @return the name the file finally has in the folder, or null when nothing could be written.
     */
    fun writeArchive(
        context: Context,
        folderUriString: String,
        fileName: String,
        sourceFile: File,
    ): String? {
        val treeUri = parseTreeUri(folderUriString) ?: return null
        if (status(context, folderUriString) != Status.READY) return null

        val resolver = context.contentResolver
        val folderDocumentUri = treeDocumentUri(treeUri) ?: return null
        val temporaryDocumentUri =
            createDocument(resolver, treeUri, folderDocumentUri, EXTERNAL_FULL_BACKUP_TEMP_FILE_NAME) ?: return null
        val isCopied =
            runCatching {
                val outputStream = resolver.openOutputStream(temporaryDocumentUri) ?: throw IOException("no output stream for $temporaryDocumentUri")
                outputStream.use { output ->
                    sourceFile.inputStream().use { input -> input.copyTo(output) }
                }
                true
            }.onFailure {
                Log.w(TAG, "writing the temporary archive failed: $temporaryDocumentUri", it)
            }.getOrDefault(false)

        if (!isCopied) {
            deleteDocument(resolver, temporaryDocumentUri)
            return null
        }

        val existingDocumentUri = findDocument(resolver, treeUri, fileName)
        if (existingDocumentUri != null && !deleteDocument(resolver, existingDocumentUri)) {
            // The old archive is still there, so the rename below would only add a "(1)" copy next to it.
            Log.w(TAG, "replacing the previous archive failed: $fileName")
            deleteDocument(resolver, temporaryDocumentUri)
            return null
        }

        val renamedUri = renameDocument(resolver, temporaryDocumentUri, fileName)
        if (renamedUri != null) return fileName

        // A provider without rename support cannot replace a document in place. Falling back to one direct write keeps
        // the backup working there at the cost of the atomic replace, which is the only thing this path can no longer
        // promise.
        Log.w(TAG, "rename is not supported by this folder, writing $fileName directly")
        val fallbackDocumentUri = createDocument(resolver, treeUri, folderDocumentUri, fileName)
        val isFallbackWritten =
            fallbackDocumentUri != null &&
                runCatching {
                    val outputStream = resolver.openOutputStream(fallbackDocumentUri) ?: throw IOException("no output stream for $fallbackDocumentUri")
                    outputStream.use { output ->
                        sourceFile.inputStream().use { input -> input.copyTo(output) }
                    }
                    true
                }.onFailure {
                    Log.w(TAG, "writing the archive directly failed: $fallbackDocumentUri", it)
                }.getOrDefault(false)
        deleteDocument(resolver, temporaryDocumentUri)
        return if (isFallbackWritten) fileName else null
    }

    private fun parseTreeUri(folderUriString: String?): Uri? =
        folderUriString
            ?.takeIf { it.isNotBlank() }
            ?.let { runCatching { Uri.parse(it) }.getOrNull() }
            ?.takeIf { it.scheme == ContentResolver.SCHEME_CONTENT }

    private fun treeDocumentUri(treeUri: Uri): Uri? =
        runCatching {
            DocumentsContract.buildDocumentUriUsingTree(treeUri, DocumentsContract.getTreeDocumentId(treeUri))
        }.onFailure {
            Log.w(TAG, "the stored folder is not a tree uri: $treeUri")
        }.getOrNull()

    private fun hasPersistedWritePermission(
        context: Context,
        treeUri: Uri,
    ): Boolean =
        runCatching {
            context.contentResolver.persistedUriPermissions.any { it.uri == treeUri && it.isWritePermission && it.isReadPermission }
        }.getOrDefault(false)

    private fun documentExists(
        context: Context,
        documentUri: Uri,
    ): Boolean =
        runCatching {
            context.contentResolver
                .query(documentUri, arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID), null, null, null)
                ?.use { cursor -> cursor.moveToFirst() } ?: false
        }.onFailure {
            Log.i(TAG, "the folder cannot be read: $documentUri")
        }.getOrDefault(false)

    /** The document of [displayName] inside the folder, or null when the folder does not hold one. */
    private fun findDocument(
        resolver: ContentResolver,
        treeUri: Uri,
        displayName: String,
    ): Uri? =
        runCatching {
            val childrenUri =
                DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, DocumentsContract.getTreeDocumentId(treeUri))
            resolver.query(
                childrenUri,
                arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID, DocumentsContract.Document.COLUMN_DISPLAY_NAME),
                null,
                null,
                null,
            )?.use { cursor ->
                while (cursor.moveToNext()) {
                    if (displayName == cursor.getString(1)) {
                        return@use DocumentsContract.buildDocumentUriUsingTree(treeUri, cursor.getString(0))
                    }
                }
                null
            }
        }.onFailure {
            Log.w(TAG, "listing the folder failed", it)
        }.getOrNull()

    /**
     * Creates the document of [displayName], removing a leftover of the same name first: the provider would otherwise
     * create the new document as "name (1)".
     */
    private fun createDocument(
        resolver: ContentResolver,
        treeUri: Uri,
        folderDocumentUri: Uri,
        displayName: String,
    ): Uri? {
        findDocument(resolver, treeUri, displayName)?.let { deleteDocument(resolver, it) }
        return runCatching {
            DocumentsContract.createDocument(resolver, folderDocumentUri, MIME_TYPE_ZIP, displayName)
        }.onFailure {
            Log.w(TAG, "creating $displayName failed", it)
        }.getOrNull()
    }

    private fun renameDocument(
        resolver: ContentResolver,
        documentUri: Uri,
        displayName: String,
    ): Uri? =
        runCatching {
            DocumentsContract.renameDocument(resolver, documentUri, displayName)
        }.onFailure {
            Log.i(TAG, "renaming to $displayName failed", it)
        }.getOrNull()

    private fun deleteDocument(
        resolver: ContentResolver,
        documentUri: Uri,
    ): Boolean =
        runCatching {
            DocumentsContract.deleteDocument(resolver, documentUri)
        }.onFailure {
            Log.w(TAG, "deleting $documentUri failed", it)
        }.getOrDefault(false)

    /** File name of the archive written in [mode], e.g. `easy_diary_backup.zip` or `easy_diary_backup_20261128_090000.zip`. */
    fun archiveFileName(
        baseName: String,
        mode: Int,
        timestamp: String,
    ): String =
        if (mode == AlarmConstants.BACKUP_FILE_MODE_TIMESTAMPED) "${baseName}_$timestamp.zip" else "$baseName.zip"
}
