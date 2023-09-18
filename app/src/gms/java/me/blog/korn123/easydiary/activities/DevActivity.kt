package me.blog.korn123.easydiary.activities

import android.accounts.Account
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.adapters.OptionItemAdapter
import me.blog.korn123.easydiary.databinding.DialogFontsBinding
import me.blog.korn123.easydiary.enums.DialogMode
import me.blog.korn123.easydiary.extensions.makeSnackBar
import me.blog.korn123.easydiary.extensions.makeToast
import me.blog.korn123.easydiary.extensions.pauseLock
import me.blog.korn123.easydiary.extensions.updateAlertDialogWithIcon
import me.blog.korn123.easydiary.helper.AAF_TEST
import me.blog.korn123.easydiary.helper.DriveServiceHelper
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import me.blog.korn123.easydiary.helper.GoogleOAuthHelper
import me.blog.korn123.easydiary.helper.GoogleOAuthHelper.Companion.initGoogleSignAccount
import me.blog.korn123.easydiary.models.Diary
import me.blog.korn123.easydiary.services.FullBackupService

class DevActivity : BaseDevActivity() {

    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    private lateinit var mRequestGoogleSignInLauncher: ActivityResultLauncher<Intent>
    private lateinit var mRequestGoogleCalendarPermissions: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mRequestGoogleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { it ->
            pauseLock()
            when (it.resultCode == Activity.RESULT_OK && it.data != null) {
                true -> {
                    // The Task returned from this call is always completed, no need to attach
                    // a listener.
                    val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(it.data)
                    val googleSignAccount = task.getResult(ApiException::class.java)
                    googleSignAccount?.account?.let { account ->
                        GoogleOAuthHelper.callAccountCallback(account)
                    }
                }
                false -> {
                    makeSnackBar("Google account verification failed.")
                }
            }
        }

        mRequestGoogleCalendarPermissions = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            pauseLock()
            when (it.resultCode == Activity.RESULT_OK && it.data != null) {
                true -> {
                    mPermissionCallback.invoke()
                }
                false -> {
                    makeSnackBar("Google account verification failed.")
                }
            }
        }

        mBinding.linearDevContainer.addView(
            // GMSP
            createBaseCardView("GMSP", null, Button(this).apply {
                text = "Full-Backup"
                layoutParams = mFlexboxLayoutParams
                setOnClickListener {
                    GoogleOAuthHelper.getGoogleSignAccount(this@DevActivity)?.account?.let { account ->
                        DriveServiceHelper(this@DevActivity, account).run {
                            initDriveWorkingDirectory(DriveServiceHelper.AAF_EASY_DIARY_PHOTO_FOLDER_NAME) { photoFolderId ->
                                if (photoFolderId != null) {
                                    Intent(context, FullBackupService::class.java).apply {
                                        putExtra(
                                            DriveServiceHelper.WORKING_FOLDER_ID,
                                            photoFolderId
                                        )
                                        ContextCompat.startForegroundService(context, this)
                                    }
                                } else {
                                    makeSnackBar("Failed start a service.")
                                }
                            }
                        }
                    }
                }
            }, Button(this@DevActivity).apply {
                text ="Google Calendar"
                layoutParams = mFlexboxLayoutParams
                setOnClickListener {
                    initGoogleSignAccount(this@DevActivity, mRequestGoogleSignInLauncher) { account ->
                        requestCalendarPermissions(account) {
                            val credential: GoogleAccountCredential =
                                GoogleAccountCredential.usingOAuth2(
                                    this@DevActivity,
                                    arrayListOf(CalendarScopes.CALENDAR)
                                ).apply {
                                    selectedAccount = account
                                }
                            val calendarService = Calendar.Builder(
                                AndroidHttp.newCompatibleTransport(),
                                GsonFactory(),
                                credential
                            )
                                .setApplicationName(getString(R.string.app_name))
                                .build()

                            fun fetchData(calendarId: String, nextPageToken: String?, total: Int = 0) {
                                var insertCount = 0
                                CoroutineScope(Dispatchers.IO).launch {
                                    val result = if (nextPageToken == null) calendarService.events().list(calendarId).setMaxResults(2000).execute() else calendarService.events().list("hanjoongcho@gmail.com").setPageToken(nextPageToken).setMaxResults(2000).execute()
                                    withContext(Dispatchers.Main) {
                                        val descriptions = arrayListOf<String>()
                                        result.items.forEachIndexed { index, item ->
                                            Log.i(AAF_TEST, "$index ${item.start?.date} ${item.summary} ${item.start?.dateTime}")
                                            descriptions.add(item.summary)
                                            val timeMillis = if (item.start?.dateTime != null) item.start.dateTime.value else item.start?.date?.value ?: 0
                                            if (EasyDiaryDbHelper.findDiary(item.summary)
                                                    .none { diary -> diary.currentTimeMillis == timeMillis }
                                            ) {
                                                EasyDiaryDbHelper.insertDiary(
                                                    Diary(
                                                        BaseDiaryEditingActivity.DIARY_SEQUENCE_INIT,
                                                        timeMillis,
                                                        if (item.description != null) item.summary else "",
                                                        item.description ?: item.summary,
                                                        10022,
                                                        item?.start?.dateTime == null
                                                    )
                                                )
                                                insertCount++
                                            }
                                        }
                                        if (result.nextPageToken != null) {
                                            fetchData(calendarId, result.nextPageToken, total.plus(insertCount))
                                        } else {
                                            makeToast("Total: ${total.plus(insertCount)}")
                                        }
                                    }
                                }
                            }
                            fun fetchCalendarList() {
                                var alertDialog: AlertDialog? = null
                                CoroutineScope(Dispatchers.IO).launch {
                                    val result = calendarService.calendarList().list().execute()
                                    withContext(Dispatchers.Main) {
                                        val builder = AlertDialog.Builder(this@DevActivity)
                                        builder.setNegativeButton(getString(android.R.string.cancel), null)
                                        val dialogFontsBinding = DialogFontsBinding.inflate(layoutInflater)
                                        val calendarInfo = ArrayList<Map<String, String>>()
                                        result.items.forEach { calendar ->
                                            calendarInfo.add(mapOf(
                                                "optionTitle" to calendar.summary,
                                                "optionValue" to calendar.id
                                            ))
                                        }
                                        val optionItemAdapter = OptionItemAdapter(this@DevActivity, R.layout.item_check_label, calendarInfo, null, null)
                                        dialogFontsBinding.run {
                                            listFont.adapter = optionItemAdapter
                                            listFont.setOnItemClickListener { parent, view, position, id ->
                                                calendarInfo[position]["optionValue"]?.let {
                                                    fetchData(it, null)
                                                    alertDialog?.dismiss()
                                                }
                                            }
                                        }
                                        alertDialog = builder.create().apply {
                                            updateAlertDialogWithIcon(DialogMode.INFO, this, null, dialogFontsBinding.root, "Sync Google Calendar")
                                        }
                                    }
                                }
                            }
                            fetchCalendarList()
                        }
                    }
                }
            })
        )
    }

    private lateinit var mPermissionCallback: () -> Unit
    private fun requestCalendarPermissions(account: Account, permissionCallback: () -> Unit) {
        mPermissionCallback = permissionCallback
        val credential: GoogleAccountCredential =
            GoogleAccountCredential.usingOAuth2(this, arrayListOf(CalendarScopes.CALENDAR))
                .apply { selectedAccount = account }
        val calendarService: Calendar = Calendar.Builder(AndroidHttp.newCompatibleTransport(), GsonFactory(), credential)
            .setApplicationName(getString(R.string.app_name))
            .build()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                calendarService.calendarList().list().execute()
                mPermissionCallback.invoke()
            } catch (e: UserRecoverableAuthIOException) {
                withContext(Dispatchers.Main) {
                    mRequestGoogleCalendarPermissions.launch(e.intent)
                }
            }
        }
    }


    /***************************************************************************************************
     *   etc functions
     *
     ***************************************************************************************************/
}


/***************************************************************************************************
 *   classes
 *
 ***************************************************************************************************/


/***************************************************************************************************
 *   extensions
 *
 ***************************************************************************************************/





