package me.blog.korn123.easydiary.activities

import android.content.Intent
import android.os.Bundle
import androidx.viewpager.widget.PagerAdapter
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.layout_settings_progress.*
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.adapters.DotIndicatorPager2Adapter
import me.blog.korn123.easydiary.fragments.*

class SettingsActivity : EasyDiaryActivity() {

    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    lateinit var mAdapter: PagerAdapter

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

        val fragmentList = arrayListOf(SettingsBasic(), SettingsLock(), SettingsGMSBackup(), SettingsLocalBackup(), SettingsAppInfo())
        mAdapter = DotIndicatorPager2Adapter(supportFragmentManager, fragmentList)
        view_pager2.adapter = mAdapter
        view_pager2.addOnPageChangeListener(object : androidx.viewpager.widget.ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                supportActionBar?.title = when (position) {
                    0 -> getString(R.string.preferences_category_settings)
                    1 -> getString(R.string.preferences_category_lock)
                    2 -> getString(R.string.preferences_category_backup_restore)
                    3 -> getString(R.string.preferences_category_backup_restore_device)
                    else -> getString(R.string.preferences_category_information)
                }
                supportActionBar?.subtitle = when (position) {
                    2 -> getString(R.string.preferences_category_backup_restore_sub)
                    3 -> getString(R.string.preferences_category_backup_restore_device_sub)
                    else -> null
                }
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
        dots_indicator.setViewPager(view_pager2)

        progressContainer.setOnTouchListener { _, _ -> true }
    }


    /***************************************************************************************************
     *   etc functions
     *
     ***************************************************************************************************/
    fun updateUI() {
        super.onResume()
    }
}