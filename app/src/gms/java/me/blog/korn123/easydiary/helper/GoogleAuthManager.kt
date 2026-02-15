import android.accounts.Account
import android.content.Context
import android.util.Log
import androidx.core.content.edit
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.DateTime
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.calendar.model.Event
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.makeToast
import me.blog.korn123.easydiary.extensions.showAlertDialog
import me.blog.korn123.easydiary.helper.AAF_TEST
import me.blog.korn123.easydiary.helper.AuthManager
import me.blog.korn123.easydiary.helper.DiaryEditingConstants
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import me.blog.korn123.easydiary.helper.SYMBOL_GOOGLE_CALENDAR
import me.blog.korn123.easydiary.models.Diary
import java.util.UUID

/**
 * Google Credential Manager
 */
class GoogleAuthManager(
    private val context: Context,
) {
    private val credentialManager = CredentialManager.create(context)
    private val webClientId = context.getString(R.string.oauth_request_id_token)

    // Prefs for simple login status storage (DataStore is recommended for production)
    private val prefs = context.getSharedPreferences(AuthManager.AUTH_PREFS, Context.MODE_PRIVATE)

    // Local state management (simple version)
    fun isLoggedInLocal(): Boolean = prefs.getBoolean(AuthManager.IS_LOGGED_IN, false)

    private fun setLoggedInLocal(isLoggedIn: Boolean) =
        prefs.edit {
            putBoolean(
                AuthManager.IS_LOGGED_IN,
                isLoggedIn,
            )
        }

    fun getEmail(): String? = prefs.getString(AuthManager.GOOGLE_EMAIL, null)

    private fun setEmail(email: String) =
        prefs.edit {
            putString(
                AuthManager.GOOGLE_EMAIL,
                email,
            )
        }

    fun getProfileUri(): String? = prefs.getString(AuthManager.GOOGLE_PROFILE_PIC_URI, null)

    private fun setProfileUri(googleProfileUri: String) =
        prefs.edit {
            putString(
                AuthManager.GOOGLE_PROFILE_PIC_URI,
                googleProfileUri,
            )
        }

    fun getDisplayName(): String? = prefs.getString(AuthManager.GOOGLE_DISPLAY_NAME, null)

    private fun setDisplayName(displayName: String) =
        prefs.edit {
            putString(
                AuthManager.GOOGLE_DISPLAY_NAME,
                displayName,
            )
        }

    /**
     * Sign-in request (called when sign-in button is clicked)
     * - Displays BottomSheet UI to the user
     */
    suspend fun signIn(): Result<UserInfo> =
        try {
            val request = buildLoginRequest(autoSelect = false) // Manual selection
            val result = credentialManager.getCredential(context, request)
            val userInfo = handleSignInResult(result)

            // Save login state locally on success
            setLoggedInLocal(true)
            Result.success(userInfo)
        } catch (e: GetCredentialCancellationException) {
            Result.failure(Exception("User canceled"))
        } catch (e: Exception) {
            Result.failure(e)
        }

    /**
     * Check sign-in status (called at app launch)
     * - If the locally stored flag is true -> Attempt auto sign-in (Silent)
     * - Returns UserInfo on success, null on failure
     */
    suspend fun tryAutoSignIn(): UserInfo? {
        if (!isLoggedInLocal()) return null // Don't even try if there's no record locally

        return try {
            val request = buildLoginRequest(autoSelect = true) // Auto-select option ON
            val result = credentialManager.getCredential(context, request)
            handleSignInResult(result)
        } catch (e: Exception) {
            context.makeToast("Auto sign-in failed: ${e.message}")
            // Safe to reset local state if auto sign-in fails
            if (e is NoCredentialException) setLoggedInLocal(false)
            null
        }
    }

    /**
     * Sign-out
     * - Resets Credential Manager state and deletes local data
     */
    suspend fun signOut() {
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
            setLoggedInLocal(false)
            context.makeToast("Signed out successfully")
        } catch (e: Exception) {
            Log.e("Auth", "Sign out failed", e)
        }
    }

    suspend fun getGoogleAccount(): Account {
        if (!isLoggedInLocal()) {
            val result = signIn()
            if (result.isFailure) {
                throw result.exceptionOrNull() ?: Exception("User cancel login")
            }
        }

        val email = requireNotNull(getEmail()) { "Email is null" }
        return Account(email, AuthManager.ACCOUNT_TYPE_GOOGLE)
    }

    fun getLastSignedInAccount(): Account? =
        getEmail()?.let {
            Account(it, AuthManager.ACCOUNT_TYPE_GOOGLE)
        }

    // --- Internal helper functions ---

    private fun buildLoginRequest(autoSelect: Boolean): GetCredentialRequest {
        val googleIdOption =
            GetGoogleIdOption
                .Builder()
                .setFilterByAuthorizedAccounts(autoSelect) // Filter existing accounts for auto sign-in
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(autoSelect) // Key: If true, attempt sign-in immediately without UI
                .setNonce(UUID.randomUUID().toString()) // Nonce for security
                .build()

        return GetCredentialRequest
            .Builder()
            .addCredentialOption(googleIdOption)
            .build()
    }

    private fun handleSignInResult(result: GetCredentialResponse): UserInfo {
        val credential = result.credential
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)

            return UserInfo(
                email = googleIdTokenCredential.id,
                displayName = googleIdTokenCredential.displayName ?: "Unknown",
                idToken = googleIdTokenCredential.idToken,
                profilePicUri = googleIdTokenCredential.profilePictureUri?.toString(),
            ).also {
                setEmail(it.email)
                setProfileUri(it.profilePicUri.toString())
                setDisplayName(it.displayName)
            }
        }
        throw IllegalStateException("Unexpected credential type")
    }

    suspend fun checkAPI(
        scopes: Collection<String>,
    ) {
        if (isLoggedInLocal()) {
            getCalendarList(scopes)
        } else {
            context.makeToast("Not logged in")
        }
    }

    fun createGoogleAccountCredential(
        email: String,
        scopes: Collection<String>,
    ): GoogleAccountCredential =
        GoogleAccountCredential
            .usingOAuth2(
                context,
                scopes,
            ).apply {
//                selectedAccountName = email.trim()
                selectedAccount = Account(email, AuthManager.ACCOUNT_TYPE_GOOGLE)
            }

    fun getCalendarCredential(): GoogleAccountCredential? =
        getEmail()?.let {
            createGoogleAccountCredential(
                it,
                arrayListOf(
                    CalendarScopes.CALENDAR_READONLY,
                    CalendarScopes.CALENDAR_EVENTS_READONLY,
                ),
            )
        }

    suspend fun getCalendarList(
        scopes: Collection<String>,
    ) {
        getEmail()?.let {
            Log.i(AAF_TEST, "Value: $it, Length: ${it.length}, isNull: {$it == null}")
            val credential = createGoogleAccountCredential(it, scopes)
            val calendarService = getCalendarService(context, credential)
            withContext(Dispatchers.IO) {
                val result = calendarService.calendarList().list().execute()
                withContext(Dispatchers.Main) {
                    context.showAlertDialog(
                        result.items.joinToString(
                            separator = "\n",
                            transform = { it.summary },
                        ),
                    )
                }
            }
        } ?: run {
            context.makeToast("Not logged in")
        }
    }

    fun notifyFailedGetGoogleAccount() {
        context.makeToast("Failed to get Google account")
    }

    fun getCalendarService(
        context: Context,
        credential: GoogleAccountCredential,
    ): Calendar =
        Calendar
            .Builder(
                NetHttpTransport(),
                GsonFactory(),
                credential,
            ).setApplicationName(context.getString(R.string.app_name))
            .build()

    fun fetchData(
        context: Context,
        calendarService: Calendar,
        calendarId: String,
        nextPageToken: String?,
        total: Int = 0,
    ) {
        var insertCount = 0
        val fromCalendar =
            EasyDiaryUtils.getCalendarInstance(false, java.util.Calendar.MONTH, -1)
        val toCalendar = EasyDiaryUtils.getCalendarInstance(true, java.util.Calendar.MONTH, 1)
        val mTimeMin = DateTime(fromCalendar.timeInMillis)
        val mTimeMax = DateTime(toCalendar.timeInMillis)

        CoroutineScope(Dispatchers.IO).launch {
            val result =
                if (nextPageToken == null) {
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
                Log.i(
                    AAF_TEST,
                    "$index ${item.start?.date} ${item.summary} ${item.start?.dateTime}",
                )
//                                descriptions.add(item.summary)
                withContext(Dispatchers.Main) {
                    insertCount += calendarEventToDiary(item, calendarId)
                }
            }
            if (result.nextPageToken != null) {
                fetchData(
                    context,
                    calendarService,
                    calendarId,
                    result.nextPageToken,
                    total.plus(insertCount),
                )
            }
        }
    }

    fun calendarEventToDiary(
        item: Event,
        calendarId: String,
    ): Int {
        var count = 0
        val timeMillis =
            if (item.start?.dateTime != null) {
                item.start.dateTime.value
            } else {
                item.start?.date?.value
                    ?: 0
            }
        val holidayCalendarIdPattern =
            "ko.south_korea#holiday@group.v.calendar.google.com|en.south_korea#holiday@group.v.calendar.google.com"
        if (EasyDiaryDbHelper
                .findDiary(item.summary)
                .none { diary -> diary.currentTimeMillis == timeMillis } &&
            !(item.description == null && item.summary == null) &&
            !(
                calendarId.matches(Regex(holidayCalendarIdPattern)) && item.description.isNotEmpty() &&
                    item.description.contains(
                        "Observance",
                    )
            )
        ) {
            EasyDiaryDbHelper.insertDiary(
                Diary(
                    DiaryEditingConstants.DIARY_SEQUENCE_INIT,
                    timeMillis,
                    if (item.description != null) item.summary else "",
                    item.description ?: item.summary,
                    SYMBOL_GOOGLE_CALENDAR,
                    item.start?.dateTime == null,
                ).apply {
                    isHoliday =
                        calendarId.matches(Regex(holidayCalendarIdPattern))
                },
            )
            count = 1
        }
        return count
    }
}

/**
 * Google Account Information
 */
data class UserInfo(
    val email: String,
    val displayName: String,
    val idToken: String,
    val profilePicUri: String?,
)
