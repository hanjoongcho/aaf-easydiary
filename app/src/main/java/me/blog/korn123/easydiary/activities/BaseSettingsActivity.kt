package me.blog.korn123.easydiary.activities

import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import androidx.browser.customtabs.CustomTabsIntent
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.adapters.DotIndicatorPager2Adapter
import me.blog.korn123.easydiary.databinding.ActivityBaseSettingsBinding
import me.blog.korn123.easydiary.fragments.SettingsScheduleFragment
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper

abstract class BaseSettingsActivity : EasyDiaryActivity() {

    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    protected lateinit var mBinding: ActivityBaseSettingsBinding
    lateinit var mDotIndicatorPager2Adapter: DotIndicatorPager2Adapter
    var mCurrentPosition = 0


    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityBaseSettingsBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        setSupportActionBar(mBinding.toolbar)
        supportActionBar?.run {
            setTitle(R.string.preferences_category_settings)
            setDisplayHomeAsUpEnabled(true)
        }

        mBinding.run {
            buttonAddSchedule.setOnClickListener {
                mDotIndicatorPager2Adapter.instantiateItem(mBinding.viewPager, mBinding.viewPager.currentItem).run {
                    if (this is SettingsScheduleFragment) openAlarmDialog(EasyDiaryDbHelper.makeTemporaryAlarm())
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val scheduleFragment = mDotIndicatorPager2Adapter.instantiateItem(mBinding.viewPager, mBinding.viewPager.currentItem)
        when (item.itemId) {
//            R.id.addSchedule -> {
//                if (scheduleFragment is SettingsScheduleFragment) {
//                    scheduleFragment.openAlarmDialog(EasyDiaryDbHelper.makeTemporaryAlarm())
//                }
//            }
            R.id.openManual -> {
                val customTabsIntent =
                    CustomTabsIntent.Builder().setUrlBarHidingEnabled(false).build()
                customTabsIntent.launchUrl(
                    this@BaseSettingsActivity,
                    Uri.parse(manualUrl())
                )
            }
        }
        return super.onOptionsItemSelected(item)
    }


    /***************************************************************************************************
     *   etc functions
     *
     ***************************************************************************************************/
    abstract fun manualUrl(): String

    fun updateUI() {
        super.onResume()
    }

    fun getProgressContainer() = mBinding.partialSettingsProgress.progressContainer
}