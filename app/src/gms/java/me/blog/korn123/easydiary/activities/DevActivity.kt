package me.blog.korn123.easydiary.activities

import GoogleAuthManager
import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.services.calendar.CalendarScopes
import kotlinx.coroutines.launch
import me.blog.korn123.easydiary.BuildConfig
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.enums.DialogMode
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.isVanillaIceCreamPlus
import me.blog.korn123.easydiary.extensions.makeSnackBar
import me.blog.korn123.easydiary.extensions.pauseLock
import me.blog.korn123.easydiary.extensions.showAlertDialog
import me.blog.korn123.easydiary.helper.DriveServiceHelper
import me.blog.korn123.easydiary.helper.GDriveConstants
import me.blog.korn123.easydiary.services.FullBackupService
import me.blog.korn123.easydiary.ui.components.CardContainer
import me.blog.korn123.easydiary.ui.components.CategoryTitleCard
import me.blog.korn123.easydiary.ui.components.EasyDiaryActionBar
import me.blog.korn123.easydiary.ui.components.LoadingScreen
import me.blog.korn123.easydiary.ui.components.SimpleCard
import me.blog.korn123.easydiary.ui.components.SimpleCardWithImage
import me.blog.korn123.easydiary.ui.theme.AppTheme
import me.blog.korn123.easydiary.viewmodels.BaseDevViewModel
import java.util.Locale

class DevActivity : BaseDevActivity() {
    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    private val authManager by lazy { GoogleAuthManager(this) }
    private lateinit var mRequestGoogleDrivePermissions: ActivityResultLauncher<Intent>
    private lateinit var mPermissionCallback: () -> Unit

    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mRequestGoogleDrivePermissions =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                pauseLock()
                when (it.resultCode == Activity.RESULT_OK && it.data != null) {
                    true -> {
                        mPermissionCallback.invoke()
                    }

                    false -> {
                        makeSnackBar("Google account verification failed.")
//                        progressContainer.visibility = View.GONE
                    }
                }
            }

        val viewModel: BaseDevViewModel by viewModels()

        mBinding.composeView.setContent {
            AppTheme {
                window.navigationBarColor = if (isSystemInDarkTheme()) Color.Black.toArgb() else Color.Transparent.toArgb()
                WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightNavigationBars = isSystemInDarkTheme().not()
                val configuration = LocalConfiguration.current
                val maxItemsInEachRow = if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) 2 else 3
                val bottomPadding = if (isVanillaIceCreamPlus()) WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() else 0.dp
                Scaffold(
                    contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
                    topBar = {
                        EasyDiaryActionBar(
                            title = "Easy-Diary Dev Mode",
                            subTitle = String.format(Locale.getDefault(), "v%s_%s_%s (%d)", BuildConfig.VERSION_NAME, BuildConfig.FLAVOR, BuildConfig.BUILD_TYPE, BuildConfig.VERSION_CODE),
                        ) {
                            finishActivityWithPauseLock()
                        }
                    },
                    containerColor = Color(config.screenBackgroundColor),
                    content = { innerPadding ->
                        Box(modifier = Modifier.fillMaxSize()) {
                            CardContainer(
                                modifier = Modifier.padding(innerPadding),
                            ) {
                                val settingCardModifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .weight(1f)

                                CustomLauncher(settingCardModifier, maxItemsInEachRow)
                                Etc(settingCardModifier, maxItemsInEachRow, viewModel)
                                Notification(settingCardModifier, maxItemsInEachRow)
                                AlertDialog(settingCardModifier, maxItemsInEachRow)
                                LocationManager(settingCardModifier, maxItemsInEachRow, viewModel)
                                DebugToast(settingCardModifier, maxItemsInEachRow)
                                Coroutine(settingCardModifier, maxItemsInEachRow, viewModel)
                                FingerPrint(settingCardModifier, maxItemsInEachRow)
                                GoogleMobileService(settingCardModifier, maxItemsInEachRow)
                                Spacer(
                                    modifier = Modifier.padding(0.dp, 0.dp, 0.dp, bottomPadding),
                                )
                            }
                            if (mViewModel.isLoading) {
                                LoadingScreen()
                            }
                        }
                    },
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        authManager.getProfileUri()?.let {
            mViewModel.profilePicUri = if (authManager.isLoggedInLocal()) it else null
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
        maxItemsInEachRow: Int,
    ) {
        CategoryTitleCard(title = "Google Mobile Service")
        FlowRow(
            modifier = Modifier,
            maxItemsInEachRow = maxItemsInEachRow,
        ) {
            SimpleCardWithImage(
                title = "CredentialManager",
                description = "signIn",
                imageResourceId = R.drawable.logo_google_oauth2,
                imageUrl = mViewModel.profilePicUri?.toUri(),
                modifier = modifier,
            ) {
                lifecycleScope.launch {
                    authManager.signIn().onSuccess { user ->
                        mViewModel.profilePicUri = user.profilePicUri
                    }
                }
            }
            SimpleCard(
                "CredentialManager",
                "signOut",
                modifier = modifier,
            ) {
                mViewModel.isLoading = true
                lifecycleScope.launch {
                    authManager.signOut()
                    mViewModel.isLoading = false
                    mViewModel.profilePicUri = null
                }
            }
            SimpleCard(
                "CredentialManager",
                "tryAutoSignIn",
                modifier = modifier,
            ) {
                lifecycleScope.launch {
                    authManager.tryAutoSignIn()
                }
            }
            SimpleCard(
                "CredentialManager",
                "checkAPI",
                modifier = modifier,
            ) {
                fun check() {
                    lifecycleScope.launch {
                        try {
                            authManager.checkAPI(
                                listOf(
                                    CalendarScopes.CALENDAR_READONLY,
                                    CalendarScopes.CALENDAR_EVENTS_READONLY,
                                ),
                            )
                        } catch (e: UserRecoverableAuthIOException) {
                            mRequestGoogleDrivePermissions.launch(e.intent)
                            mPermissionCallback = {
                                check()
                            }
                        }
                    }
                }
                check()
            }
            SimpleCard(
                title = "Check Google Sign Account",
                description = null,
                modifier = modifier,
            ) {
                if (authManager.isLoggedInLocal()) {
                    authManager.getEmail()?.let {
                        showAlertDialog(it, null, null, DialogMode.DEFAULT, false)
                    }
                } else {
                    authManager.notifyFailedGetGoogleAccount()
                }
            }
            SimpleCard(
                title = "Full Backup",
                description = null,
                modifier = modifier,
            ) {
                authManager.initGoogleAccount(lifecycleScope) { account ->
                    DriveServiceHelper(this@DevActivity, account).run {
                        initDriveWorkingDirectory(GDriveConstants.AAF_EASY_DIARY_PHOTO_FOLDER_NAME) { photoFolderId ->
                            if (photoFolderId != null) {
                                Intent(context, FullBackupService::class.java).apply {
                                    putExtra(
                                        GDriveConstants.WORKING_FOLDER_ID,
                                        photoFolderId,
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

    @Composable
    @Preview(heightDp = 1100)
    private fun DevActivityPreview() {
        AppTheme {
            val configuration = LocalConfiguration.current
            val maxItemsInEachRow = if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) 1 else 3
            val scrollState = rememberScrollState()

            CardContainer {
                val settingCardModifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                CustomLauncher(settingCardModifier, maxItemsInEachRow)
                Notification(settingCardModifier, maxItemsInEachRow)
                AlertDialog(settingCardModifier, maxItemsInEachRow)
//                Etc(settingCardModifier, maxItemsInEachRow, viewModel)
//                LocationManager(settingCardModifier, maxItemsInEachRow, viewModel)
                DebugToast(settingCardModifier, maxItemsInEachRow)
//                Coroutine(settingCardModifier, maxItemsInEachRow, viewModel)
                FingerPrint(settingCardModifier, maxItemsInEachRow)
                GoogleMobileService(settingCardModifier, maxItemsInEachRow)
            }
        }
    }

    /***************************************************************************************************
     *   etc functions
     *
     ***************************************************************************************************/
}
