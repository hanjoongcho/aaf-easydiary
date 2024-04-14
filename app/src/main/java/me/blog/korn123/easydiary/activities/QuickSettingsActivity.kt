package me.blog.korn123.easydiary.activities

import android.os.Bundle
import android.view.View
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.databinding.ActivityQuickSettingsBinding
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.executeScheduledTask
import me.blog.korn123.easydiary.helper.AlarmWorkExecutor
import me.blog.korn123.easydiary.models.Alarm

class QuickSettingsActivity : EasyDiaryActivity() {

    private lateinit var mBinding: ActivityQuickSettingsBinding

    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityQuickSettingsBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        setSupportActionBar(mBinding.toolbar)
        supportActionBar?.run {
            setTitle("Quick Settings")
            setHomeAsUpIndicator(R.drawable.ic_cross)
            setDisplayHomeAsUpEnabled(true)
        }

        bindEvent()
        initPreference()
    }


    /***************************************************************************************************
     *   etc functions
     *
     ***************************************************************************************************/
    private val mOnClickListener = View.OnClickListener { view ->
        mBinding.run {
            when (view.id) {
                R.id.enable_photo_highlight -> {
                    enablePhotoHighlightSwitcher.toggle()
                    config.enablePhotoHighlight = enablePhotoHighlightSwitcher.isChecked
                }
                R.id.disable_future_diary -> {
                    disableFutureDiarySwitcher.toggle()
                    config.disableFutureDiary = disableFutureDiarySwitcher.isChecked
                }
                R.id.syncGoogleCalendar -> {
                    val alarm = Alarm().apply {
                        sequence = Int.MAX_VALUE
                        workMode = Alarm.WORK_MODE_CALENDAR_SCHEDULE_SYNC
                        label = "Quick Settings"
                    }
                    AlarmWorkExecutor(this@QuickSettingsActivity).run { executeWork(alarm) }
                }
            }
        }
        updateCardAlpha()
    }

    private fun bindEvent() {
        mBinding.run {
            enablePhotoHighlight.setOnClickListener(mOnClickListener)
            disableFutureDiary.setOnClickListener(mOnClickListener)
            syncGoogleCalendar.setOnClickListener(mOnClickListener)
        }
    }

    private fun initPreference() {
        mBinding.run {
            enablePhotoHighlightSwitcher.isChecked = config.enablePhotoHighlight
            disableFutureDiarySwitcher.isChecked = config.disableFutureDiary
            updateCardAlpha()
        }
    }

    private fun updateCardAlpha() {
        mBinding.run {
            enablePhotoHighlight.alpha = if (enablePhotoHighlightSwitcher.isChecked) 1.0f else 0.5f
            disableFutureDiary.alpha = if (disableFutureDiarySwitcher.isChecked) 1.0f else 0.5f
        }
    }
}