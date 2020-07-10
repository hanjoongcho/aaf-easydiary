package me.blog.korn123.easydiary.activities

import android.os.Bundle
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_settings.*
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.adapters.DotIndicatorPager2Adapter
import me.blog.korn123.easydiary.fragments.SettingsScheduleFragment
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper

open class BaseSettingsActivity : EasyDiaryActivity() {

    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    lateinit var mDotIndicatorPager2Adapter: DotIndicatorPager2Adapter
    var mCurrentPosition = 0


    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setSupportActionBar(toolbar)
        supportActionBar?.run {
            setTitle(R.string.preferences_category_settings)
            setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val scheduleFragment = mDotIndicatorPager2Adapter.instantiateItem(viewPager, viewPager.currentItem)
        when (item.itemId) {
            R.id.addSchedule -> {
                if (scheduleFragment is SettingsScheduleFragment) {
                    scheduleFragment.openAlarmDialog(EasyDiaryDbHelper.createTemporaryAlarm())
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
}