package me.blog.korn123.easydiary.helper

import android.accounts.Account
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.DateTime
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.calendar.model.Event
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.blog.korn123.commons.utils.DateUtils
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.BaseDiaryEditingActivity
import me.blog.korn123.easydiary.adapters.OptionItemAdapter
import me.blog.korn123.easydiary.databinding.DialogFontsBinding
import me.blog.korn123.easydiary.enums.DialogMode
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.makeSnackBar
import me.blog.korn123.easydiary.extensions.makeToast
import me.blog.korn123.easydiary.extensions.showAlertDialog
import me.blog.korn123.easydiary.extensions.updateAlertDialogWithIcon
import me.blog.korn123.easydiary.models.Diary

class GoogleOAuthHelper { 
    companion object {
        private lateinit var mAccountCallback: (Account) -> Unit

        fun signOutGoogleOAuth(activity: Activity, showCompleteMessage: Boolean = true) {
            // Configure sign-in to request the user's ID, email address, and basic
            // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
            activity.run {
                val gso: GoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.oauth_request_id_token))
                        .requestEmail()
                        .build()
                val client = GoogleSignIn.getClient(this, gso)
                client.signOut().addOnCompleteListener {
                    config.clearLegacyToken = true
                    if (showCompleteMessage) makeSnackBar("Sign out complete:)")
                }
            }
        }

        fun isValidGoogleSignAccount(context: Context): Boolean = GoogleSignIn.getLastSignedInAccount(context) != null

        fun getGoogleSignAccount(context: Context) = GoogleSignIn.getLastSignedInAccount(context)

        fun initGoogleSignAccount(activity: Activity?, activityResultLauncher: ActivityResultLauncher<Intent>, callback: (account: Account) -> Unit) {
            mAccountCallback = callback

            activity?.let {
                // Check for existing Google Sign In account, if the user is already signed in
                // the GoogleSignInAccount will be non-null.
                val googleSignInAccount: GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(it)

                if (googleSignInAccount == null) {
                    // Configure sign-in to request the user's ID, email address, and basic
                    // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
                    val gso: GoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(it.getString(R.string.oauth_request_id_token))
                            .requestEmail()
                            .build()
                    val client = GoogleSignIn.getClient(it, gso)
                    activityResultLauncher.launch(client.signInIntent)
                } else {
                    googleSignInAccount.account?.let {
                        mAccountCallback.invoke(it)
                    }
                }
            }
        }

        fun callAccountCallback(account: Account) {
            mAccountCallback.invoke(account)
        }

        fun getCalendarCredential(context: Context, account: Account = getGoogleSignAccount(context)?.account!!): GoogleAccountCredential = GoogleAccountCredential.usingOAuth2(
            context,
            arrayListOf(CalendarScopes.CALENDAR_READONLY, CalendarScopes.CALENDAR_EVENTS_READONLY)
        ).apply {
            selectedAccount = account
        }

        fun getCalendarService(context: Context, credential: GoogleAccountCredential): Calendar = Calendar.Builder(
            AndroidHttp.newCompatibleTransport(),
            GsonFactory(),
            credential
        )
            .setApplicationName(context.getString(R.string.app_name))
            .build()

        fun fetchData(
            context: Context,
            calendarService: Calendar,
            calendarId: String,
            nextPageToken: String?,
            total: Int = 0
        ) {
            var insertCount = 0
            val fromCalendar =
                EasyDiaryUtils.getCalendarInstance(false, java.util.Calendar.MONTH, -1)
            val toCalendar = EasyDiaryUtils.getCalendarInstance(true, java.util.Calendar.MONTH, 1)
            val mTimeMin = DateTime(fromCalendar.timeInMillis)
            val mTimeMax = DateTime(toCalendar.timeInMillis)

            CoroutineScope(Dispatchers.IO).launch {
                val result = if (nextPageToken == null) {
                    calendarService
                        .events()
                        .list(calendarId)
                        .setMaxResults(2000)
                        .setTimeMin(mTimeMin)
                        .setTimeMax(mTimeMax)
                        .setSingleEvents(true)
                        .execute()
                } else {
                    calendarService
                        .events()
                        .list(calendarId)
                        .setPageToken(nextPageToken)
                        .setMaxResults(2000)
                        .setTimeMin(mTimeMin)
                        .setTimeMax(mTimeMax)
                        .setSingleEvents(true)
                        .execute()
                }
                result.items.forEachIndexed { index, item ->
                    Log.i(AAF_TEST, "$index ${item.start?.date} ${item.summary} ${item.start?.dateTime}")
//                                descriptions.add(item.summary)
                    withContext(Dispatchers.Main) {
                        insertCount += calendarEventToDiary(item, calendarId)
                    }
                }
                if (result.nextPageToken != null) {
                    fetchData(context, calendarService, calendarId, result.nextPageToken, total.plus(insertCount))
                }
            }
        }

        fun calendarEventToDiary(item: Event, calendarId: String): Int {
            var count = 0
            val timeMillis = if (item.start?.dateTime != null) item.start.dateTime.value else item.start?.date?.value ?: 0
            if (EasyDiaryDbHelper.findDiary(item.summary)
                    .none { diary -> diary.currentTimeMillis == timeMillis }
                && !(item.description == null && item.summary == null)) {
                EasyDiaryDbHelper.insertDiary(
                    Diary(
                        BaseDiaryEditingActivity.DIARY_SEQUENCE_INIT,
                        timeMillis,
                        if (item.description != null) item.summary else "",
                        item.description ?: item.summary,
                        SYMBOL_GOOGLE_CALENDAR,
                        item?.start?.dateTime == null
                    ).apply { isHoliday =
                        calendarId.matches(Regex("ko.south_korea#holiday@group.v.calendar.google.com|en.south_korea#holiday@group.v.calendar.google.com"))
                    }
                )
                count = 1
            }
            return count
        }
    }
}