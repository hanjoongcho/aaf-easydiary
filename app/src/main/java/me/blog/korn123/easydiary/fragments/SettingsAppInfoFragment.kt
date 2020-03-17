package me.blog.korn123.easydiary.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.layout_settings_app_info.*
import me.blog.korn123.easydiary.BuildConfig
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.MarkDownViewActivity
import me.blog.korn123.easydiary.activities.SettingsActivity
import me.blog.korn123.easydiary.extensions.openGooglePlayBy
import me.blog.korn123.easydiary.extensions.updateFragmentUI
import me.blog.korn123.easydiary.helper.TransitionHelper

class SettingsAppInfoFragment() : androidx.fragment.app.Fragment() {


    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    private lateinit var mRootView: ViewGroup
    private lateinit var mContext: Context
    private lateinit var mActivity: Activity


    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mRootView = inflater.inflate(R.layout.layout_settings_app_info, container, false) as ViewGroup
        return mRootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mContext = context!!
        mActivity = activity!!

        bindEvent()
        updateFragmentUI(mRootView)
        initPreference()
    }

    override fun onResume() {
        super.onResume()
        updateFragmentUI(mRootView)
        initPreference()
    }


    /***************************************************************************************************
     *   etc functions
     *
     ***************************************************************************************************/
    private val mOnClickListener = View.OnClickListener { view ->
        when (view.id) {
            R.id.rateAppSetting -> {
                if (BuildConfig.FLAVOR == "foss") {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getStoreUrl())))
                } else {
                    mActivity.openGooglePlayBy("me.blog.korn123.easydiary")
                }
            }
            R.id.licenseView -> {
                TransitionHelper.startActivityWithTransition(mActivity, Intent(mContext, MarkDownViewActivity::class.java).apply {
                    putExtra(MarkDownViewActivity.OPEN_URL_INFO, "https://raw.githubusercontent.com/hanjoongcho/aaf-easydiary/master/THIRDPARTY.md")
                    putExtra(MarkDownViewActivity.OPEN_URL_DESCRIPTION, getString(R.string.preferences_information_licenses))
                })
            }
            R.id.releaseNotes -> (mActivity as SettingsActivity).checkWhatsNewDialog(false)
            R.id.faq -> {
                TransitionHelper.startActivityWithTransition(mActivity, Intent(mContext, MarkDownViewActivity::class.java).apply {
                    putExtra(MarkDownViewActivity.OPEN_URL_INFO, getString(R.string.faq_url))
                    putExtra(MarkDownViewActivity.OPEN_URL_DESCRIPTION, getString(R.string.faq_title))
                })
            }
            R.id.privacyPolicy -> {
                TransitionHelper.startActivityWithTransition(mActivity, Intent(mContext, MarkDownViewActivity::class.java).apply {
                    putExtra(MarkDownViewActivity.OPEN_URL_INFO, getString(R.string.privacy_policy_url))
                    putExtra(MarkDownViewActivity.OPEN_URL_DESCRIPTION, getString(R.string.privacy_policy_title))
                })
            }
        }
    }

    private fun bindEvent() {
        rateAppSetting.setOnClickListener(mOnClickListener)
        licenseView.setOnClickListener(mOnClickListener)
        releaseNotes.setOnClickListener(mOnClickListener)
        faq.setOnClickListener(mOnClickListener)
        privacyPolicy.setOnClickListener(mOnClickListener)
        setupInvite()
    }

    private fun initPreference() {
        rateAppSettingSummary.text = String.format("Easy Diary v%s_%s", BuildConfig.VERSION_NAME, BuildConfig.FLAVOR)
    }

    private fun setupInvite() {
        inviteSummary.text = String.format(getString(R.string.invite_friends_summary), getString(R.string.app_name))
        invite.setOnClickListener {
            val text = String.format(getString(io.github.aafactory.commons.R.string.share_text), getString(R.string.app_name), getStoreUrl())
            Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
                putExtra(Intent.EXTRA_TEXT, text)
                type = "text/plain"
                startActivity(Intent.createChooser(this, getString(io.github.aafactory.commons.R.string.invite_via)))
            }
        }
    }

    private fun getStoreUrl(): String {
        return if (BuildConfig.FLAVOR == "foss") "https://f-droid.org/packages/${mContext.packageName}/" else "https://play.google.com/store/apps/details?id=${mContext.packageName}"
    }
}