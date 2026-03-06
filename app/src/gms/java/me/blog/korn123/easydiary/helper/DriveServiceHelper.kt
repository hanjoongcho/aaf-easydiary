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
import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.blog.korn123.easydiary.R
import org.apache.commons.io.IOUtils
import java.io.*
import java.util.Collections
import java.util.concurrent.Callable
import java.util.concurrent.Executors

/**
 * A utility for performing read/write operations on Drive files via the REST API and opening a
 * file picker UI via Storage Access Framework.
 */
class DriveServiceHelper(
    val context: Context,
) {
    lateinit var mDriveService: Drive

    constructor(context: Context, account: Account) : this(context) {
        val credential: GoogleAccountCredential =
            GoogleAccountCredential.usingOAuth2(
                context,
                Collections.singleton(DriveScopes.DRIVE_FILE),
            )
        credential.selectedAccount = account
        val googleDriveService: Drive =
            Drive
                .Builder(NetHttpTransport(), GsonFactory(), credential)
                .setApplicationName(context.getString(R.string.app_name))
                .build()
        mDriveService = googleDriveService
    }

    constructor(context: Context, driveService: Drive) : this(context) {
        this.mDriveService = driveService
    }

    private val mExecutor = Executors.newSingleThreadExecutor()

    suspend fun initDriveWorkingDirectory(
        workingFolderName: String,
    ): String =
        withContext(Dispatchers.IO) {
            // --- STEP 1: 최상위 폴더(AAF) 찾기 ---
            val appRootFileList =
                queryFiles("'root' in parents and name = '${GDriveConstants.AAF_ROOT_FOLDER_NAME}' and trashed = false")

            // 폴더 ID 결정 (없으면 만들고, 있으면 가져옴)
            val aafFolderId =
                if (appRootFileList.files.isEmpty()) {
                    // 없으면 생성 후 대기
                    createFolder(GDriveConstants.AAF_ROOT_FOLDER_NAME)
                } else {
                    // 있으면 ID 추출
                    appRootFileList.files[0].id
                }

            // --- STEP 2: 작업 폴더(EasyDiary) 찾기 ---
            val workingFolderFileList =
                queryFiles("'$aafFolderId' in parents and name = '$workingFolderName' and trashed = false")

            val finalWorkingFolderId =
                if (workingFolderFileList.files.isEmpty()) {
                    // 없으면 생성 후 대기 (부모는 aafFolderId)
                    createFolder(workingFolderName, aafFolderId)
                } else {
                    // 있으면 ID 추출
                    workingFolderFileList.files[0].id
                }

            finalWorkingFolderId
        }

    suspend fun createFolder(
        folderName: String,
        parentId: String = "root",
    ): String =
        withContext(Dispatchers.IO) {
            val metadata =
                File()
                    .setParents(listOf(parentId))
                    .setMimeType(GDriveConstants.MIME_TYPE_GOOGLE_APPS_FOLDER)
                    .setName(folderName)

            val googleFile =
                mDriveService.files().create(metadata).execute()
                    ?: throw IOException("Null result when requesting file creation.")

            googleFile.id
        }

    /**
     * Creates a text file in the user's My Drive folder and returns its file ID.
     */
    fun createFileLegacy(
        parentId: String,
        filePath: String,
        name: String,
        mimeType: String,
    ): Task<String> =
        Tasks.call(
            mExecutor,
            Callable<String> {
                val metadata =
                    File()
                        .setParents(listOf(parentId))
                        .setMimeType(mimeType)
                        .setName(name)

                // Convert content to an AbstractInputStreamContent instance.
                val contentStream =
                    ByteArrayContent(mimeType, IOUtils.toByteArray(FileInputStream(File(filePath))))

                val googleFile =
                    mDriveService.files().create(metadata, contentStream).execute()
                        ?: throw IOException("Null result when requesting file creation.")
                googleFile.id
            },
        )

    suspend fun createFile(
        parentId: String,
        filePath: String,
        name: String,
        mimeType: String,
    ) = withContext(Dispatchers.IO) {
        val metadata =
            File()
                .setParents(listOf(parentId))
                .setMimeType(mimeType)
                .setName(name)

        // Convert content to an AbstractInputStreamContent instance.
        val contentStream =
            ByteArrayContent(mimeType, IOUtils.toByteArray(FileInputStream(File(filePath))))

        val googleFile =
            mDriveService.files().create(metadata, contentStream).execute()
                ?: throw IOException("Null result when requesting file creation.")
    }

    suspend fun downloadFile(
        fileId: String,
        destFilePath: String,
    ): Int =
        withContext(Dispatchers.IO) {
            IOUtils.copy(
                mDriveService.files().get(fileId).executeMediaAsInputStream(),
                FileOutputStream(File(destFilePath)),
            )
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
    suspend fun queryFiles(
        q: String,
        pageSize: Int = 10,
        nextPageToken: String? = null,
    ): FileList {
        Log.i("GSuite H", nextPageToken ?: "없어~")
        Log.i("GSuite H", q)
        val fields = "nextPageToken, files(id, name, mimeType, createdTime)"
        return withContext(Dispatchers.IO) {
            when (nextPageToken == null) {
                true -> {
                    mDriveService
                        .files()
                        .list()
                        .setQ(q)
                        .setFields(fields)
                        .setSpaces("drive")
                        .setOrderBy("createdTime desc")
                        .setPageSize(pageSize)
                        .execute()
                }

                false -> {
                    mDriveService
                        .files()
                        .list()
                        .setQ(q)
                        .setFields(fields)
                        .setSpaces("drive")
                        .setOrderBy("createdTime desc")
                        .setPageSize(pageSize)
                        .setPageToken(nextPageToken)
                        .execute()
                }
            }
        }
    }
}
