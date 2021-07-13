package me.blog.korn123.easydiary.activities

import android.os.Bundle
import android.view.Menu
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.adapters.DotIndicatorPager2Adapter
import me.blog.korn123.easydiary.fragments.*

class SettingsActivity : BaseSettingsActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val fragmentList = arrayListOf(
                SettingsBasicFragment(), SettingsFontFragment(),
                SettingsLockFragment(), SettingsLocalBackupFragment(),
                SettingsScheduleFragment(), SettingsAppInfoFragment()
        )
        mDotIndicatorPager2Adapter = DotIndicatorPager2Adapter(supportFragmentManager, fragmentList)
        mBinding.run {
            viewPager.adapter = mDotIndicatorPager2Adapter
            viewPager.addOnPageChangeListener(object : androidx.viewpager.widget.ViewPager.OnPageChangeListener {
                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

                override fun onPageSelected(position: Int) {
                    mCurrentPosition = position
                    supportActionBar?.run {
                        when (position) {
                            0 -> {
                                title = getString(R.string.preferences_category_settings)
                                subtitle = ""
                            }
                            1 -> {
                                title = getString(R.string.preferences_category_font)
                                subtitle = ""
                            }
                            2 -> {
                                title = getString(R.string.preferences_category_lock)
                                subtitle = ""
                            }
                            3 -> {
                                title = getString(R.string.preferences_category_backup_restore_device)
                                subtitle = getString(R.string.preferences_category_backup_restore_device_sub)
                                updateUI()
                            }
                            4 -> {
                                title = getString(R.string.preferences_category_schedule)
                                subtitle = ""

                            }
                            else -> {
                                title = getString(R.string.preferences_category_information)
                                subtitle = ""
                            }
                        }
                    }
                    invalidateOptionsMenu()
                }

                override fun onPageScrollStateChanged(state: Int) {}
            })
            dotsIndicator.setViewPager(viewPager)
            getProgressContainer().setOnTouchListener { _, _ -> true }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.diary_settings_schedule, menu)
        if (mCurrentPosition == 4) menu.findItem(R.id.addSchedule).isVisible = true
        return true
    }
}