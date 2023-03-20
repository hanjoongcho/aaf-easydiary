package me.blog.korn123.easydiary.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.core.content.ContextCompat
import me.blog.korn123.easydiary.extensions.makeSnackBar
import me.blog.korn123.easydiary.helper.DriveServiceHelper
import me.blog.korn123.easydiary.helper.GoogleOAuthHelper
import me.blog.korn123.easydiary.services.FullBackupService

class DevActivity : BaseDevActivity() {

    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
            })
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





