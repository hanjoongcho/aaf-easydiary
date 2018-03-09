package me.blog.korn123.easydiary.activities

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.github.hanjoongcho.commons.activities.BaseSimpleActivity
import kotlinx.android.synthetic.main.activity_lock_setting.*
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.helper.APP_LOCK_REQUEST_PASSWORD

/**
 * Created by hanjoong on 2017-05-03.
 */

class LockSettingActivity : BaseSimpleActivity() {
    private var mPasswordView = arrayOfNulls<TextView>(4)
    private var mCursorIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lock_setting)

        mPasswordView[0] = pass1
        mPasswordView[1] = pass2
        mPasswordView[2] = pass3
        mPasswordView[3] = pass4

        num0.setOnClickListener(keyPadClickListener)
        num1.setOnClickListener(keyPadClickListener)
        num2.setOnClickListener(keyPadClickListener)
        num3.setOnClickListener(keyPadClickListener)
        num4.setOnClickListener(keyPadClickListener)
        num5.setOnClickListener(keyPadClickListener)
        num6.setOnClickListener(keyPadClickListener)
        num7.setOnClickListener(keyPadClickListener)
        num8.setOnClickListener(keyPadClickListener)
        num9.setOnClickListener(keyPadClickListener)
    }

    override fun onResume() {
        isBackgroundColorFromPrimaryColor = true
        super.onResume()
        FontUtils.setFontsTypeface(applicationContext, assets, null, container)
    }

    override fun getMainViewGroup(): ViewGroup? = container
    
    private val keyPadClickListener: View.OnClickListener = View.OnClickListener { view ->
        var password = ""

        when (view?.id) {
            R.id.num0 -> password = "0"
            R.id.num1 -> password = "1"
            R.id.num2 -> password = "2"
            R.id.num3 -> password = "3"
            R.id.num4 -> password = "4"
            R.id.num5 -> password = "5"
            R.id.num6 -> password = "6"
            R.id.num7 -> password = "7"
            R.id.num8 -> password = "8"
            R.id.num9 -> password = "9"
        }

        mPasswordView[mCursorIndex]?.setText(password)
        if (mCursorIndex == 3) {
            var fullPassword = ""
            for (tv in mPasswordView) {
                fullPassword += tv?.text
            }
            intent.putExtra(APP_LOCK_REQUEST_PASSWORD, fullPassword)
            setResult(Activity.RESULT_OK, intent)
            finish()
        } else {
            mCursorIndex++
        }
    }
}
