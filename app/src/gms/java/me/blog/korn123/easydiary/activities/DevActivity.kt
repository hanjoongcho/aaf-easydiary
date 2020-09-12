package me.blog.korn123.easydiary.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import kotlinx.android.synthetic.main.activity_dev.*
import me.blog.korn123.easydiary.extensions.makeSnackBar
import me.blog.korn123.easydiary.extensions.pauseLock
import me.blog.korn123.easydiary.helper.DriveServiceHelper
import me.blog.korn123.easydiary.helper.GoogleOAuthHelper
import me.blog.korn123.easydiary.helper.REQUEST_CODE_GOOGLE_SIGN_IN
import me.blog.korn123.easydiary.services.FullBackupService

class DevActivity : BaseDevActivity() {

    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fullBackupService.setOnClickListener {
            GoogleOAuthHelper.getGoogleSignAccount(this)?.account?.let { account ->
                DriveServiceHelper(this, account).run {
                    initDriveWorkingDirectory(DriveServiceHelper.AAF_EASY_DIARY_PHOTO_FOLDER_NAME) { photoFolderId ->
                        if (photoFolderId != null) {
                            Intent(context, FullBackupService::class.java).apply {
                                putExtra(DriveServiceHelper.WORKING_FOLDER_ID, photoFolderId)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        pauseLock()

        when (resultCode == Activity.RESULT_OK && intent != null) {
            true -> {
                when (requestCode) {
                    REQUEST_CODE_GOOGLE_SIGN_IN -> {
                        val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(intent)
                        val googleSignAccount = task.getResult(ApiException::class.java)
                        googleSignAccount?.account?.let {
                            GoogleOAuthHelper.callAccountCallback(it)
                        }
                    }
                }
            }
            false -> {
                when (requestCode) {
                    REQUEST_CODE_GOOGLE_SIGN_IN -> {
                        makeSnackBar("Google account verification failed.")
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


/***************************************************************************************************
 *   classes
 *
 ***************************************************************************************************/


/***************************************************************************************************
 *   extensions
 *
 ***************************************************************************************************/





