package me.blog.korn123.easydiary.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.github.aafactory.commons.activities.BaseSimpleActivity
import io.github.aafactory.commons.extensions.baseConfig
import kotlinx.android.synthetic.main.activity_lock_setting.*
import me.blog.korn123.commons.utils.CommonUtils
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.helper.APP_FINISH_FLAG
import me.blog.korn123.easydiary.helper.APP_LOCK_SAVED_PASSWORD
import org.apache.commons.lang3.StringUtils

/**
 * Created by hanjoong on 2017-05-03.
 */

class DiaryLockActivity : BaseSimpleActivity() {
    private var mPasswordView = arrayOfNulls<TextView>(4)
    private var mCursorIndex = 0
    private var password = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diary_lock)

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

    override fun onBackPressed() {
        //        super.onBackPressed();
        val readDiaryIntent = Intent(this@DiaryLockActivity, DiaryMainActivity::class.java)
        readDiaryIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        readDiaryIntent.putExtra(APP_FINISH_FLAG, true)
        startActivity(readDiaryIntent)
        finish()
    }

    private val keyPadClickListener: View.OnClickListener = View.OnClickListener { view ->
        when (view.id) {
            R.id.num0 -> password += "0"
            R.id.num1 -> password += "1"
            R.id.num2 -> password += "2"
            R.id.num3 -> password += "3"
            R.id.num4 -> password += "4"
            R.id.num5 -> password += "5"
            R.id.num6 -> password += "6"
            R.id.num7 -> password += "7"
            R.id.num8 -> password += "8"
            R.id.num9 -> password += "9"
        }
        mPasswordView[mCursorIndex]?.text = "-"

        if (mCursorIndex == 3) {
            Thread(Runnable {
                Handler(Looper.getMainLooper()).post {
                    if (StringUtils.equals(config.aafPinLockSavedPassword, password)) {
                        config.aafPinLockPauseMillis = System.currentTimeMillis()
                        finish()
                    } else {
                        mCursorIndex = 0
                        password = ""
                        for (tv in mPasswordView) {
                            tv?.setText(null)
                        }
                    }
                }
            }).start()

        } else {
            mCursorIndex++

        }
    }
}
