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
import androidx.fragment.app.add
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.DateTime
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventDateTime
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.DiaryRepositoryEntryPoint
import me.blog.korn123.easydiary.extensions.makeToast
import me.blog.korn123.easydiary.extensions.showAlertDialog
import me.blog.korn123.easydiary.helper.AAF_TEST
import me.blog.korn123.easydiary.helper.AuthManager
import me.blog.korn123.easydiary.helper.DiaryEditingConstants
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import me.blog.korn123.easydiary.helper.GCalendarConstants
import me.blog.korn123.easydiary.helper.SYMBOL_GOOGLE_CALENDAR
import me.blog.korn123.easydiary.helper.toDomain
import me.blog.korn123.easydiary.models.Diary
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
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

    fun getLastSignedInAccount(): Account? {
        val email = getEmail()
        return if (isLoggedInLocal() && email != null) {
            Account(
                email,
                AuthManager.ACCOUNT_TYPE_GOOGLE,
            )
        } else {
            null
        }
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

    suspend fun checkCalendarAPI(
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

    suspend fun calendarEventToDiary(
        item: Event,
        calendarId: String,
    ): Int {
        val summary = item.summary
        val description = item.description

        // 1. 유효성 및 필터링 검사
        if (summary.isNullOrEmpty() && description.isNullOrEmpty()) return 0

        val isHolidayCalendar = GCalendarConstants.HOLIDAY_CALENDAR_IDS.contains(calendarId)
        if (isHolidayCalendar && description?.contains("Observance") == true) return 0

        // 2. 종일 일정 여부 확인 (start.date가 있으면 종일 일정)
        val isAllDay = item.start?.date != null
        val systemZone = ZoneId.systemDefault()

        // 3. 시작/종료 날짜 및 밀리초 추출
        val (startDate, startMillis) = parseEventDateTime(item.start, isAllDay, systemZone)
        val (rawEndDate, rawEndMillis) = parseEventDateTime(item.end, isAllDay, systemZone)

        // 4. 종일 일정인 경우 Exclusive 규격에 따라 종료일 -1일 보정
        val endDate = if (isAllDay) rawEndDate.minusDays(1) else rawEndDate
        val endMillis =
            if (isAllDay) {
                endDate.atStartOfDay(systemZone).toInstant().toEpochMilli()
            } else {
                rawEndMillis
            }

        // 종료일이 시작일보다 앞서는 예외 상황 방지
        val effectiveEndDate = if (endDate.isBefore(startDate)) startDate else endDate

        var insertedCount = 0

        // --- CASE A: 시작일과 종료일이 같은 경우 ---
        if (startDate == effectiveEndDate) {
            if (insertDiaryIfNotExists(summary, startMillis, isAllDay, isHolidayCalendar, description)) {
                insertedCount++
            }
            return insertedCount
        }

        // --- CASE B: 시작일과 종료일이 다른 경우 (다일 이벤트) ---
        var currentDate = startDate

        while (!currentDate.isAfter(effectiveEndDate)) {
            val currentMillis: Long
            val currentIsAllDay: Boolean

            when (currentDate) {
                startDate -> {
                    currentMillis = startMillis
                    currentIsAllDay = isAllDay
                }

                effectiveEndDate -> {
                    currentMillis = endMillis
                    currentIsAllDay = isAllDay
                }

                else -> {
                    // 중간 일자: 00:00:00 기준 & 무조건 isAllDay = true
                    currentMillis = currentDate.atStartOfDay(systemZone).toInstant().toEpochMilli()
                    currentIsAllDay = true
                }
            }

            if (insertDiaryIfNotExists(summary, currentMillis, currentIsAllDay, isHolidayCalendar, description)) {
                insertedCount++
            }

            currentDate = currentDate.plusDays(1)
        }

        return insertedCount
    }

    /**
     * EventDateTime(start/end) 객체에서 LocalDate 및 Epoch Millis 추출 헬퍼 함수
     */
    private fun parseEventDateTime(
        eventDateTime: EventDateTime?,
        isAllDay: Boolean,
        zoneId: ZoneId,
    ): Pair<LocalDate, Long> {
        val dateTimeValue = eventDateTime?.dateTime?.value
        val dateValue = eventDateTime?.date?.value

        return when {
            // 1) 종일 일정인 경우: date (YYYY-MM-DD) 값을 UTC LocalDate로 직접 파싱
            isAllDay && dateValue != null -> {
                val localDate = LocalDate.parse(eventDateTime.date.toStringRfc3339())
                val millis = localDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
                Pair(localDate, millis)
            }

            // 2) 일반 일정인 경우: dateTime epoch milli 사용
            dateTimeValue != null -> {
                val instant = Instant.ofEpochMilli(dateTimeValue)
                Pair(instant.atZone(zoneId).toLocalDate(), dateTimeValue)
            }

            // 3) 예외 케이스 처리
            else -> {
                Pair(LocalDate.now(zoneId), 0L)
            }
        }
    }

    // DB 중복 체크 및 Insert 헬퍼 함수
    private suspend fun insertDiaryIfNotExists(
        summary: String?,
        millis: Long,
        isAllDay: Boolean,
        isHolidayCalendar: Boolean,
        description: String?,
    ): Boolean {
        val isAlreadyExists =
            EntryPointAccessors
                .fromApplication(
                    context,
                    DiaryRepositoryEntryPoint::class.java,
                ).diaryRepository()
                .getDiariesWithPhotos(summary)
                .first()
                .any { diary -> diary.currentTimeMillis == millis }

        if (isAlreadyExists) return false

        val diary =
            Diary(
                sequence = DiaryEditingConstants.DIARY_SEQUENCE_INIT,
                currentTimeMillis = millis,
                title = if (description != null) summary.orEmpty() else "",
                contents = description ?: summary.orEmpty(),
                weather = SYMBOL_GOOGLE_CALENDAR,
                isAllDay = isAllDay,
            ).apply {
                isHoliday = isHolidayCalendar
            }

        diary
        EntryPointAccessors
            .fromApplication(
                context,
                DiaryRepositoryEntryPoint::class.java,
            ).diaryRepository()
            .insertDiary(diary.toDomain())
        return true
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
