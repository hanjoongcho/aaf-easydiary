package me.blog.korn123.easydiary.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.TextUnit
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import me.blog.korn123.easydiary.enums.DialogMode
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.makeSnackBar
import me.blog.korn123.easydiary.extensions.pauseLock
import me.blog.korn123.easydiary.extensions.showAlertDialog
import me.blog.korn123.easydiary.helper.DriveServiceHelper
import me.blog.korn123.easydiary.helper.GoogleOAuthHelper
import me.blog.korn123.easydiary.services.FullBackupService
import me.blog.korn123.easydiary.ui.components.CategoryTitleCard
import me.blog.korn123.easydiary.ui.components.SimpleCard
import me.blog.korn123.easydiary.ui.theme.AppTheme

class DevActivity : BaseDevActivity() {

    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    private lateinit var mRequestGoogleSignInLauncher: ActivityResultLauncher<Intent>
    private lateinit var mPermissionCallback: () -> Unit


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

        mBinding.composeViewGms.setContent {
            AppTheme {
                val currentContext = LocalContext.current
                val pixelValue = currentContext.config.settingFontSize
                val density = LocalDensity.current
                val currentTextUnit = with (density) {
                    val temp = pixelValue.toDp()
                    temp.toSp()
                }
                val configuration = LocalConfiguration.current
                val maxItemsInEachRow = if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) 2 else 3

                Column {
                    val settingCardModifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                    GoogleMobileService(currentContext, currentTextUnit, false, settingCardModifier, maxItemsInEachRow)
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
        currentContext: Context,
        currentTextUnit: TextUnit,
        isPreview: Boolean,
        settingCardModifier: Modifier,
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
                settingCardModifier,
            ) {
                if (GoogleOAuthHelper.isValidGoogleSignAccount(this@DevActivity)) {
                    GoogleOAuthHelper.getGoogleSignAccount(this@DevActivity)?.run {
                        showAlertDialog(account!!.name, null, null, DialogMode.DEFAULT, false)
                    }
                } else {
                    showAlertDialog("Sign account is invalid.", null)
                }
            }
            SimpleCard(
                "Full Backup",
                null,
                settingCardModifier,
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
        }
    }



    /***************************************************************************************************
     *   etc functions
     *
     ***************************************************************************************************/
}


