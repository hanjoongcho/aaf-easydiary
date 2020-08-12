/**
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.blog.korn123.easydiary.helper

import android.accounts.Account
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.core.util.Pair
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.models.ActionLog
import org.apache.commons.io.IOUtils
import java.io.*
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors

/**
 * A utility for performing read/write operations on Drive files via the REST API and opening a
 * file picker UI via Storage Access Framework.
 */
class DriveServiceHelper(val context: Context) {
    lateinit var mDriveService: Drive
    
    constructor(context: Context, account: Account) : this(context) {
        val credential: GoogleAccountCredential = GoogleAccountCredential.usingOAuth2(context, Collections.singleton(DriveScopes.DRIVE_FILE))
        credential.selectedAccount = account
        val googleDriveService: Drive = Drive.Builder(AndroidHttp.newCompatibleTransport(), GsonFactory(), credential)
                .setApplicationName(context.getString(R.string.app_name))
                .build()
        mDriveService = googleDriveService
    }

    constructor(context: Context, driveService: Drive) : this(context) {
        this.mDriveService = driveService
    }
    
    private val mExecutor = Executors.newSingleThreadExecutor()

    companion object {
        const val MIME_TYPE_GOOGLE_APPS_FOLDER = "application/vnd.google-apps.folder"
        const val MIME_TYPE_AAF_EASY_DIARY_PHOTO = "aaf/easy.diary.photo"
        
        const val AAF_ROOT_FOLDER_NAME = "AAFactoty"
        const val AAF_EASY_DIARY_PHOTO_FOLDER_NAME = "aaf-easydiary_photos"
        const val AAF_EASY_DIARY_REALM_FOLDER_NAME = "aaf-easydiary_realm"

        const val WORKING_FOLDER_ID = "working-folder-id"
    }

    fun initDriveWorkingDirectory(workingFolderName: String, callback: (workingFolderId: String?) -> Unit) {
        // 01. AAF 폴더 검색
        queryFiles("'root' in parents and name = '${DriveServiceHelper.AAF_ROOT_FOLDER_NAME}' and trashed = false").run {
            addOnSuccessListener { result ->
                when (result.files.size) {
                    // 02. AAF 폴더 없으면 생성
                    0 -> createFolder(DriveServiceHelper.AAF_ROOT_FOLDER_NAME).addOnSuccessListener { aafFolderId ->
                        // 02-01. workingFolder 생성
                        createFolder(workingFolderName, aafFolderId).addOnSuccessListener { workingFolderId ->
                            callback(workingFolderId)
                        }
                    }
                    // 03. workingFolder 검색
                    1 -> {
                        val parentId = result.files[0].id
                        queryFiles("'$parentId' in parents and name = '$workingFolderName' and trashed = false").addOnSuccessListener {
                            when (it.files.size) {
                                // 03-01. workingFolder 생성
                                0 -> createFolder(workingFolderName, parentId).addOnSuccessListener { workingFolderId ->
                                    callback(workingFolderId)
                                }
                                1 -> {
                                    callback(it.files[0].id)
                                }
                            }
                        }
                    }
                }
            }
            addOnFailureListener { e ->
                callback(null)
            }
        }
    }

    fun createFolder(folderName: String, parentId: String = "root"): Task<String> {
        return Tasks.call(mExecutor, Callable<String> {
            val metadata = File()
                    .setParents(listOf(parentId))
                    .setMimeType(MIME_TYPE_GOOGLE_APPS_FOLDER)
                    .setName(folderName)

            val googleFile = mDriveService.files().create(metadata).execute()
                    ?: throw IOException("Null result when requesting file creation.")
            googleFile.id
        })
    }

    /**
     * Creates a text file in the user's My Drive folder and returns its file ID.
     */
    fun createFile(parentId: String, filePath: String, name: String, mimeType: String): Task<String> {
        return Tasks.call(mExecutor, Callable<String> {
            val metadata = File()
                    .setParents(listOf(parentId))
                    .setMimeType(mimeType)
                    .setName(name)

            // Convert content to an AbstractInputStreamContent instance.
            val contentStream = ByteArrayContent(mimeType, IOUtils.toByteArray(FileInputStream(File(filePath))))
            
            val googleFile = mDriveService.files().create(metadata, contentStream).execute()
                    ?: throw IOException("Null result when requesting file creation.")
            googleFile.id
        })
    }

    // FIXME: Drive file creation and data creation to be done at once
    fun uploadFile(fileId: String, filePath: String, mimeType: String): Task<Void> {
        return Tasks.call(mExecutor, Callable<Void> {

            // Convert content to an AbstractInputStreamContent instance.
            val contentStream = ByteArrayContent(mimeType, IOUtils.toByteArray(FileInputStream(File(filePath))))

            // Update the metadata and contents.
            mDriveService.files().update(fileId, null, contentStream).execute()
            null
        })
    }

    fun downloadFile(fileId: String, destFilePath: String): Task<Int> {
        return Tasks.call(mExecutor, Callable<Int> {
            IOUtils.copy(mDriveService.files().get(fileId).executeMediaAsInputStream(), FileOutputStream(File(destFilePath)))
        })
    }

    fun readFile(fileId: String): Task<List<String>> {
        return Tasks.call(mExecutor, Callable<List<String>> {
            IOUtils.readLines(mDriveService.files().get(fileId).executeMediaAsInputStream(), "UTF-8")
        })
    }

    /**
     * Opens the file identified by `fileId` and returns a [Pair] of its name and
     * contents.
     */
//    fun readFile(fileId: String): Task<Pair<String, String>> {
//        return Tasks.call(mExecutor, Callable {
//            // Retrieve the metadata as a File object.
//            val metadata = mDriveService.files().get(fileId).execute()
//            val name = metadata.name
//
//            // Stream the file contents to a String.
//            mDriveService.files().get(fileId).executeMediaAsInputStream().use { `is` ->
//                BufferedReader(InputStreamReader(`is`)).use { reader ->
//                    val stringBuilder = StringBuilder()
//                    var line: String
//
//                    while ((line = reader.readLine()) != null) {
//                        stringBuilder.append(line)
//                    }
//                    val contents = stringBuilder.toString()
//
//                    return@Tasks.call Pair . create < String, String>(name, contents)
//                }
//            }
//        })
//    }

    /**
     * Updates the file identified by `fileId` with the given `name` and `content`.
     */
    fun saveFile(fileId: String, name: String, content: String): Task<Void> {
        return Tasks.call(mExecutor, Callable<Void> {
            // Create a File containing any metadata changes.
            val metadata = File().setName(name)

            // Convert content to an AbstractInputStreamContent instance.
            val contentStream = ByteArrayContent.fromString("text/plain", content)

            // Update the metadata and contents.
            mDriveService.files().update(fileId, metadata, contentStream).execute()
            null
        })
    }

    /**
     * Returns a [FileList] containing all the visible files in the user's My Drive.
     *
     *
     * The returned list will only contain files visible to this app, i.e. those which were
     * created by this app. To perform operations on files not created by the app, the project must
     * request Drive Full Scope in the [Google
 * Developer's Console](https://play.google.com/apps/publish) and be submitted to Google for verification.
     */
    fun queryFiles(q: String, pageSize: Int = 10, nextPageToken: String? = null): Task<FileList> {
        Log.i("GSuite H", nextPageToken ?: "없어~")
        Log.i("GSuite H", q)
        val fields = "nextPageToken, files(id, name, mimeType, createdTime)"
        return when (nextPageToken == null) {
            true -> Tasks.call(mExecutor, Callable<FileList> { mDriveService.files().list().setQ(q).setFields(fields).setSpaces("drive").setOrderBy("createdTime desc").setPageSize(pageSize).execute() })
            false -> Tasks.call(mExecutor, Callable<FileList> { mDriveService.files().list().setQ(q).setFields(fields).setSpaces("drive").setOrderBy("createdTime desc").setPageSize(pageSize).setPageToken(nextPageToken).execute() })
        }
    }

    /**
     * Returns an [Intent] for opening the Storage Access Framework file picker.
     */
    fun createFilePickerIntent(): Intent {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "text/plain"

        return intent
    }

    /**
     * Opens the file at the `uri` returned by a Storage Access Framework [Intent]
     * created by [.createFilePickerIntent] using the given `contentResolver`.
     */
    fun openFileUsingStorageAccessFramework(
            contentResolver: ContentResolver, uri: Uri): Task<Pair<String, String>> {
        return Tasks.call(mExecutor, Callable {
            // Retrieve the document's display name from its metadata.
            var name: String? = null
            contentResolver.query(uri, null, null, null, null)!!.use { cursor ->
                if (cursor != null && cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    name = cursor.getString(nameIndex)
                } else {
                    throw IOException("Empty cursor returned for file.")
                }
            }

            // Read the document's contents as a String.
            var content: String? = null
            contentResolver.openInputStream(uri)!!.use { stream ->
                val sb = StringBuilder()
                val lines = IOUtils.readLines(stream, "UTF-8")
                lines.forEach { sb.append(it) }
                content = sb.toString()
            }

            Pair.create(name, content)
        })
    }
}
