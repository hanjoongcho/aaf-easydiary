package me.blog.korn123.easydiary.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import me.blog.korn123.easydiary.extensions.makeSnackBar
import me.blog.korn123.easydiary.extensions.pauseLock
import me.blog.korn123.easydiary.helper.GoogleOAuthHelper
import me.blog.korn123.easydiary.helper.REQUEST_CODE_GOOGLE_SIGN_IN
import kotlinx.android.synthetic.main.activity_dev.*

class DevActivity : BaseDevActivity() {

    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        clearGoogleOauthToken.setOnClickListener {
            GoogleOAuthHelper.signOutGoogleOAuth(this, true)
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





