package me.blog.korn123.easydiary.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import me.blog.korn123.easydiary.enums.DialogMode
import me.blog.korn123.easydiary.extensions.makeSnackBar
import me.blog.korn123.easydiary.extensions.pauseLock
import me.blog.korn123.easydiary.extensions.showAlertDialog
import me.blog.korn123.easydiary.helper.DriveServiceHelper
import me.blog.korn123.easydiary.helper.GoogleOAuthHelper
import me.blog.korn123.easydiary.services.FullBackupService

class DevActivity : BaseDevActivity() {

    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    private lateinit var mRequestGoogleSignInLauncher: ActivityResultLauncher<Intent>
    private lateinit var mPermissionCallback: () -> Unit

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

        // GMSP
        val gmsButtons = arrayOf(
            Button(this).apply {
                text = "Check Google SignAccount"
                layoutParams = mFlexboxLayoutParams
                setOnClickListener {
                    if (GoogleOAuthHelper.isValidGoogleSignAccount(this@DevActivity)) {
                        GoogleOAuthHelper.getGoogleSignAccount(this@DevActivity)?.run {
                            showAlertDialog(account!!.name, null, null, DialogMode.DEFAULT, false)
                        }
                    } else {
                        showAlertDialog("Sign account is invalid.", null)
                    }
                }
            },
            Button(this).apply {
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
            }
        )
        mBinding.linearDevContainer.addView(
            createBaseCardView("GMSP", null, *gmsButtons)
        )
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





