package me.blog.korn123.easydiary.fragments

import android.accounts.Account
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import io.github.aafactory.commons.extensions.updateAppViews
import io.github.aafactory.commons.extensions.updateTextColors
import kotlinx.android.synthetic.main.layout_settings_basic.*
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.updateCardViewPolicy

class SettingsLock : androidx.fragment.app.Fragment() {


    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    private lateinit var mAccountCallback: (Account) -> Unit
    private lateinit var mRootView: ViewGroup
    private lateinit var mContext: Context
    private lateinit var mActivity: Activity
    private var mAlertDialog: AlertDialog? = null
    private var mTaskFlag = 0


    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mRootView = inflater.inflate(R.layout.layout_settings_lock, container, false) as ViewGroup
        return mRootView
    }
}