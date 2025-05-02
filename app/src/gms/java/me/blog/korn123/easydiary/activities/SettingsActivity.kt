package me.blog.korn123.easydiary.activities

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.adapters.DotIndicatorPager2Adapter
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.pauseLock
import me.blog.korn123.easydiary.fragments.*

class SettingsActivity : BaseSettingsActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val fragmentList = arrayListOf(
                SettingsBasicFragment(), SettingsFontFragment(),
                SettingsLockFragment(), SettingsGMSBackupFragment(),
                SettingsLocalBackupFragment(), SettingsScheduleFragment(), SettingsAppInfoFragment()
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
                                buttonAddSchedule.visibility = View.GONE
                            }
                            1 -> {
                                title = getString(R.string.preferences_category_font)
                                subtitle = ""
                                buttonAddSchedule.visibility = View.GONE
                            }
                            2 -> {
                                title = getString(R.string.preferences_category_lock)
                                subtitle = ""
                                buttonAddSchedule.visibility = View.GONE
                            }
                            3 -> {
                                title = getString(R.string.preferences_category_backup_restore)
                                subtitle = getString(R.string.preferences_category_backup_restore_sub)
                                pauseLock()
                                updateUI()
                                buttonAddSchedule.visibility = View.GONE
                            }
                            4 -> {
                                title = getString(R.string.preferences_category_backup_restore_device)
                                subtitle = getString(R.string.preferences_category_backup_restore_device_sub)
                                buttonAddSchedule.visibility = View.GONE
                            }
                            5 -> {
                                title = getString(R.string.preferences_category_schedule)
                                subtitle = ""
                                buttonAddSchedule.visibility = View.VISIBLE
                            }
                            else -> {
                                title = getString(R.string.preferences_category_information)
                                subtitle = ""
                                buttonAddSchedule.visibility = View.GONE
                            }
                        }
                    }
                    invalidateOptionsMenu()
                }

                override fun onPageScrollStateChanged(state: Int) {}
            })
            dotsIndicator.setViewPager(viewPager)
            getProgressContainer().setOnTouchListener { _, _ -> true }

            ViewCompat.setOnApplyWindowInsetsListener(dotsIndicator) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
                val layoutParams = v.layoutParams
                if (layoutParams is ViewGroup.MarginLayoutParams) {
                    layoutParams.bottomMargin = systemBars.bottom
                    v.layoutParams = layoutParams
                }
                insets
            }
        }
    }

    override fun manualUrl(): String = when (mCurrentPosition) {
        0 -> {
            getString(R.string.user_manual_url_basic)
        }
        1 -> {
            getString(R.string.user_manual_url_font)
        }
        2 -> {
            getString(R.string.user_manual_url_lock)
        }
        3 -> {
            getString(R.string.user_manual_url_backup_restore_google_drive)
        }
        4 -> {
            getString(R.string.user_manual_url_backup_restore_device)
        }
        5 -> {
            getString(R.string.user_manual_url_schedule)

        }
        else -> {
            getString(R.string.user_manual_url_application_information)
        }
    }
}