package me.blog.korn123.easydiary.activities

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.viewpager.widget.PagerAdapter
import com.simplemobiletools.commons.extensions.toast
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.layout_settings_progress.*
import me.blog.korn123.easydiary.BuildConfig
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.adapters.DotIndicatorPager2Adapter
import me.blog.korn123.easydiary.enums.DiaryMode
import me.blog.korn123.easydiary.extensions.applyFontToMenuItem
import me.blog.korn123.easydiary.extensions.pauseLock
import me.blog.korn123.easydiary.fragments.*

class SettingsActivity : EasyDiaryActivity() {

    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    lateinit var mAdapter: PagerAdapter
    lateinit var mMenu: Menu
    private val mFragmentList = arrayListOf(
            SettingsBasicFragment(), SettingsFontFragment(),
            SettingsLockFragment(), SettingsGMSBackupFragment(),
            SettingsLocalBackupFragment(), SettingsScheduleFragment(), SettingsAppInfoFragment()
    )

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

        mAdapter = DotIndicatorPager2Adapter(supportFragmentManager, mFragmentList)
        viewPager.adapter = mAdapter
        viewPager.addOnPageChangeListener(object : androidx.viewpager.widget.ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                val menuItem = mMenu.findItem(R.id.addSchedule)
                supportActionBar?.run {
                    when (position) {
                        0 -> {
                            title = getString(R.string.preferences_category_settings)
                            subtitle = ""
                            menuItem.setVisible(false)
                        }
                        1 -> {
                            title = getString(R.string.preferences_category_font)
                            subtitle = ""
                            menuItem.setVisible(false)
                        }
                        2 -> {
                            title = getString(R.string.preferences_category_lock)
                            subtitle = ""
                            menuItem.setVisible(false)
                        }
                        3 -> {
                            title = getString(R.string.preferences_category_backup_restore)
                            subtitle = getString(R.string.preferences_category_backup_restore_sub)
                            pauseLock()
                            updateUI()
                            menuItem.setVisible(false)
                        }
                        4 -> {
                            title = getString(R.string.preferences_category_backup_restore_device)
                            subtitle = getString(R.string.preferences_category_backup_restore_device_sub)
                            menuItem.setVisible(false)
                        }
                        5 -> {
                            title = "Application Schedule"
                            subtitle = ""
                            menuItem.setVisible(true)
                        }
                        else -> {
                            title = getString(R.string.preferences_category_information)
                            subtitle = ""
                            menuItem.setVisible(false)
                        }
                    }
                }
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
        dots_indicator.setViewPager(viewPager)
        progressContainer.setOnTouchListener { _, _ -> true }


    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        this.mMenu = menu
        menuInflater.inflate(R.menu.diary_settings_schedule, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val scheduleFragment = mFragmentList[viewPager.currentItem]
        when (item.itemId) {
            R.id.addSchedule -> {
                if (scheduleFragment is SettingsScheduleFragment) {
                    scheduleFragment.context?.toast("OK!!!")

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