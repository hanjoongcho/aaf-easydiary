package me.blog.korn123.easydiary.activities

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.credentials.GetCredentialException
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.core.content.ContextCompat
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.PasswordCredential
import androidx.credentials.PublicKeyCredential
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.enums.DialogMode
import me.blog.korn123.easydiary.extensions.makeSnackBar
import me.blog.korn123.easydiary.extensions.pauseLock
import me.blog.korn123.easydiary.extensions.showAlertDialog
import me.blog.korn123.easydiary.helper.DriveServiceHelper
import me.blog.korn123.easydiary.helper.GoogleOAuthHelper
import me.blog.korn123.easydiary.services.FullBackupService
import me.blog.korn123.easydiary.ui.components.CardContainer
import me.blog.korn123.easydiary.ui.components.CategoryTitleCard
import me.blog.korn123.easydiary.ui.components.SimpleCard
import me.blog.korn123.easydiary.ui.theme.AppTheme
import me.blog.korn123.easydiary.viewmodels.BaseDevViewModel
import java.security.SecureRandom
import java.util.Base64

class DevActivity : BaseDevActivity() {

    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    private lateinit var mRequestGoogleSignInLauncher: ActivityResultLauncher<Intent>


    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
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
        val viewModel: BaseDevViewModel by viewModels()

        mBinding.composeView.setContent {
            AppTheme {
                val configuration = LocalConfiguration.current
                val maxItemsInEachRow = if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) 2 else 3
                val scrollState = rememberScrollState()

                CardContainer {
                    val settingCardModifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                    CustomLauncher(settingCardModifier, maxItemsInEachRow)
                    Notification(settingCardModifier, maxItemsInEachRow)
                    AlertDialog(settingCardModifier, maxItemsInEachRow)
                    Etc(settingCardModifier, maxItemsInEachRow, viewModel)
                    LocationManager(settingCardModifier, maxItemsInEachRow, viewModel)
                    DebugToast(settingCardModifier, maxItemsInEachRow)
                    Coroutine(settingCardModifier, maxItemsInEachRow, viewModel)
                    FingerPrint(settingCardModifier, maxItemsInEachRow)
                    GoogleMobileService(settingCardModifier, maxItemsInEachRow)
                }
            }
        }
    }


    /***************************************************************************************************
     *   Define Compose
     *
     ***************************************************************************************************/
    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    private fun GoogleMobileService(
        modifier: Modifier,
        maxItemsInEachRow: Int
    ) {
        CategoryTitleCard(title = "Google Mobile Service")
        FlowRow(
            modifier = Modifier,
            maxItemsInEachRow = maxItemsInEachRow
        ) {
            SimpleCard(
                "Check Google Sign Account",
                null,
                modifier,
            ) {
                if (GoogleOAuthHelper.isValidGoogleSignAccount(this@DevActivity)) {
                    GoogleOAuthHelper.getGoogleSignAccount(this@DevActivity)?.run {
                        showAlertDialog(account!!.name, null, null, DialogMode.DEFAULT, false)
                    }
                } else {
                    showAlertDialog("Sign account is invalid.")
                }
            }
            SimpleCard(
                "Full Backup",
                null,
                modifier,
            ) {
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
            SimpleCard(
                "Login",
                "Google Credential Manager Login",
                modifier,
            ) {
                val credentialManager = CredentialManager.create(this@DevActivity)

                val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(true)
                    .setServerClientId(getString(R.string.oauth_request_id_token))
                    .setAutoSelectEnabled(true)
                    .setNonce(generateNonce())
                .build()

                val request: GetCredentialRequest = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    CoroutineScope(Dispatchers.Main).launch {
                        try {
                            val result = credentialManager.getCredential(
                                request = request,
                                context = this@DevActivity,
                            )
                            handleSignIn(result)
                        } catch (e: GetCredentialException) {
//                        handleFailure(e)
                        }
                    }
                }
            }
        }
    }


    /***************************************************************************************************
     *   etc functions
     *
     ***************************************************************************************************/

    private fun generateNonce(): String {
        // https://chatgpt.com/c/67c2fa4c-c12c-8010-844f-079ae6da7520
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val random = ByteArray(16) // 16바이트(128비트) 난수 생성
            SecureRandom().nextBytes(random) // 난수 채움
            return Base64.getUrlEncoder().withoutPadding().encodeToString(random) // Base64로 인코딩
        } else {
         return ""
        }
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    fun handleSignIn(result: GetCredentialResponse) {
        // Handle the successfully returned credential.
        val credential = result.credential

        when (credential) {

            // Passkey credential
            is PublicKeyCredential -> {
                // Share responseJson such as a GetCredentialResponse on your server to
                // validate and authenticate
//                responseJson = credential.authenticationResponseJson
            }

            // Password credential
            is PasswordCredential -> {
                // Send ID and password to your server to validate and authenticate.
                val username = credential.id
                val password = credential.password
            }

            // GoogleIdToken credential
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        // Use googleIdTokenCredential and extract the ID to validate and
                        // authenticate on your server.
                        val googleIdTokenCredential = GoogleIdTokenCredential
                            .createFrom(credential.data)
                        // You can use the members of googleIdTokenCredential directly for UX
                        // purposes, but don't use them to store or control access to user
                        // data. For that you first need to validate the token:
                        // pass googleIdTokenCredential.getIdToken() to the backend server.
//                        GoogleIdTokenVerifier verifier = ... // see validation instructions
//                        val idToken = verifier.verify(idTokenString);
                        // To get a stable account identifier (e.g. for storing user data),
                        // use the subject ID:
//                        idToken.getPayload().getSubject()
                    } catch (e: GoogleIdTokenParsingException) {
//                        Log.e(TAG, "Received an invalid google id token response", e)
                    }
                } else {
                    // Catch any unrecognized custom credential type here.
//                    Log.e(TAG, "Unexpected type of credential")
                }
            }

            else -> {
                // Catch any unrecognized credential type here.
//                Log.e(TAG, "Unexpected type of credential")
            }
        }
    }
}


