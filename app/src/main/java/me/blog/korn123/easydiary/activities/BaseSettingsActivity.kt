package me.blog.korn123.easydiary.activities

import android.os.Bundle
import android.view.MenuItem
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.adapters.DotIndicatorPager2Adapter
import me.blog.korn123.easydiary.databinding.ActivitySettingsBinding
import me.blog.korn123.easydiary.fragments.SettingsScheduleFragment
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper

open class BaseSettingsActivity : EasyDiaryActivity() {

    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    protected lateinit var mBinding: ActivitySettingsBinding
    lateinit var mDotIndicatorPager2Adapter: DotIndicatorPager2Adapter
    var mCurrentPosition = 0


    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        setSupportActionBar(mBinding.toolbar)
        supportActionBar?.run {
            setTitle(R.string.preferences_category_settings)
            setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val scheduleFragment = mDotIndicatorPager2Adapter.instantiateItem(mBinding.viewPager, mBinding.viewPager.currentItem)
        when (item.itemId) {
            R.id.addSchedule -> {
                if (scheduleFragment is SettingsScheduleFragment) {
                    scheduleFragment.openAlarmDialog(EasyDiaryDbHelper.insertTemporaryAlarm())
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }


    /***************************************************************************************************
     *   etc functions
     *
     ***************************************************************************************************/
    fun updateUI() {
        super.onResume()
    }

    fun getProgressContainer() = mBinding.partialSettingsProgress.progressContainer
}