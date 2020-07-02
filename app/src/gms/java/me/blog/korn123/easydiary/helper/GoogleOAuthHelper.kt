package me.blog.korn123.easydiary.helper

import android.accounts.Account
import android.app.Activity
import android.content.Context
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.makeSnackBar

class GoogleOAuthHelper {
    companion object {
        private lateinit var mAccountCallback: (Account) -> Unit

        fun signOutGoogleOAuth(activity: Activity, showCompleteMessage: Boolean = true) {
            // Configure sign-in to request the user's ID, email address, and basic
            // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
            activity.run {
                val gso: GoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.oauth_requerst_id_token))
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

        fun initGoogleSignAccount(fragment: Fragment?, callback: (account: Account) -> Unit) {
            mAccountCallback = callback

            fragment?.run {
                this.activity?.let {
                    // Check for existing Google Sign In account, if the user is already signed in
                    // the GoogleSignInAccount will be non-null.
                    val googleSignInAccount: GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(it)

                    if (googleSignInAccount == null) {
                        // Configure sign-in to request the user's ID, email address, and basic
                        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
                        val gso: GoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .requestIdToken(getString(R.string.oauth_requerst_id_token))
                                .requestEmail()
                                .build()
                        val client = GoogleSignIn.getClient(it, gso)
                        startActivityForResult(client.signInIntent, REQUEST_CODE_GOOGLE_SIGN_IN)
                    } else {
                        googleSignInAccount.account?.let {
                            mAccountCallback.invoke(it)
                        }
                    }
                }
            }
        }

        fun initGoogleSignAccount(activity: Activity?, callback: (account: Account) -> Unit) {
            mAccountCallback = callback
            activity?.run {
                // Check for existing Google Sign In account, if the user is already signed in
                // the GoogleSignInAccount will be non-null.
                val googleSignInAccount: GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(this)

                if (googleSignInAccount == null) {
                    // Configure sign-in to request the user's ID, email address, and basic
                    // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
                    val gso: GoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(getString(R.string.oauth_requerst_id_token))
                            .requestEmail()
                            .build()
                    val client = GoogleSignIn.getClient(this, gso)
                    startActivityForResult(client.signInIntent, REQUEST_CODE_GOOGLE_SIGN_IN)
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
    }
}