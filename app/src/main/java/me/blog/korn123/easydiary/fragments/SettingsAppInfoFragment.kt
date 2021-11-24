package me.blog.korn123.easydiary.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.simplemobiletools.commons.extensions.toast
import me.blog.korn123.easydiary.BuildConfig
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.MarkDownViewerActivity
import me.blog.korn123.easydiary.activities.SettingsActivity
import me.blog.korn123.easydiary.databinding.PartialSettingsAppInfoBinding
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.openGooglePlayBy
import me.blog.korn123.easydiary.extensions.updateFragmentUI
import me.blog.korn123.easydiary.helper.TransitionHelper

class SettingsAppInfoFragment() : androidx.fragment.app.Fragment() {


    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    private lateinit var mBinding: PartialSettingsAppInfoBinding


    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = PartialSettingsAppInfoBinding.inflate(layoutInflater)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindEvent()
        updateFragmentUI(mBinding.root)
        initPreference()
    }

    override fun onResume() {
        super.onResume()
        updateFragmentUI(mBinding.root)
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
                    requireActivity().openGooglePlayBy("me.blog.korn123.easydiary")
                }
            }
            R.id.licenseView -> {
                TransitionHelper.startActivityWithTransition(requireActivity(), Intent(requireActivity(), MarkDownViewerActivity::class.java).apply {
                    putExtra(MarkDownViewerActivity.OPEN_URL_INFO, "https://raw.githubusercontent.com/hanjoongcho/aaf-easydiary/master/THIRDPARTY.md")
                    putExtra(MarkDownViewerActivity.OPEN_URL_DESCRIPTION, getString(R.string.preferences_information_licenses))
                    putExtra(MarkDownViewerActivity.FORCE_APPEND_CODE_BLOCK, false)
                })
            }
            R.id.releaseNotes -> (requireActivity() as SettingsActivity).checkWhatsNewDialog(false)
            R.id.faq -> {
                TransitionHelper.startActivityWithTransition(requireActivity(), Intent(requireActivity(), MarkDownViewerActivity::class.java).apply {
                    putExtra(MarkDownViewerActivity.OPEN_URL_INFO, getString(R.string.faq_url))
                    putExtra(MarkDownViewerActivity.OPEN_URL_DESCRIPTION, getString(R.string.faq_title))
                    putExtra(MarkDownViewerActivity.FORCE_APPEND_CODE_BLOCK, false)
                })
            }
            R.id.privacyPolicy -> {
                TransitionHelper.startActivityWithTransition(requireActivity(), Intent(requireActivity(), MarkDownViewerActivity::class.java).apply {
                    putExtra(MarkDownViewerActivity.OPEN_URL_INFO, getString(R.string.privacy_policy_url))
                    putExtra(MarkDownViewerActivity.OPEN_URL_DESCRIPTION, getString(R.string.privacy_policy_title))
                    putExtra(MarkDownViewerActivity.FORCE_APPEND_CODE_BLOCK, false)
                })
            }
        }
    }

    private fun bindEvent() {
        mBinding.run {
            rateAppSetting.setOnClickListener(mOnClickListener)
            licenseView.setOnClickListener(mOnClickListener)
            releaseNotes.setOnClickListener(mOnClickListener)
            faq.setOnClickListener(mOnClickListener)
            privacyPolicy.setOnClickListener(mOnClickListener)
            setupInvite()

            licenseView.setOnLongClickListener {
                requireActivity().run {
                    when (config.enableDebugMode) {
                        true -> {
                            config.enableDebugMode = false
                            toast("Debug console is disabled.")
                        }
                        false -> {
                            config.enableDebugMode = true
                            toast("Debug console is enabled.")
                        }
                    }
                    true
                }
            }
        }
    }

    private fun initPreference() {
        mBinding.rateAppSettingSummary.text = String.format("v%s_%s_%s (%d)", BuildConfig.VERSION_NAME, BuildConfig.FLAVOR, BuildConfig.BUILD_TYPE, BuildConfig.VERSION_CODE)
    }

    private fun setupInvite() {
        mBinding.inviteSummary.text = String.format(getString(R.string.invite_friends_summary), getString(R.string.app_name))
        mBinding.invite.setOnClickListener {
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
        return if (BuildConfig.FLAVOR == "foss") "https://f-droid.org/packages/${requireActivity().packageName}/" else "https://play.google.com/store/apps/details?id=${requireActivity().packageName}"
    }
}