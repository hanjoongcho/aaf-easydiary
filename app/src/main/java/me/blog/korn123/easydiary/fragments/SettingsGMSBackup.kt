package me.blog.korn123.easydiary.fragments

import android.accounts.Account
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import me.blog.korn123.easydiary.R

class SettingsGMSBackup() : androidx.fragment.app.Fragment() {


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
        mRootView = inflater.inflate(R.layout.layout_settings_backup_gms, container, false) as ViewGroup
        return mRootView
    }
}