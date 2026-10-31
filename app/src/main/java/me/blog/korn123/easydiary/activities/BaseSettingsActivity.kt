package me.blog.korn123.easydiary.activities

import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.browser.customtabs.CustomTabsIntent
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.adapters.DotIndicatorPager2Adapter
import me.blog.korn123.easydiary.databinding.ActivityBaseSettingsBinding
import me.blog.korn123.easydiary.fragments.SettingsScheduleFragment
import me.blog.korn123.easydiary.presentation.alarm.AlarmViewModel

abstract class BaseSettingsActivity : EasyDiaryActivity() {
    protected lateinit var binding: ActivityBaseSettingsBinding
    protected val alarmViewModel: AlarmViewModel by viewModels()
    lateinit var dotIndicatorPager2Adapter: DotIndicatorPager2Adapter

    var currentPosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBaseSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.run {
            setTitle(R.string.preferences_category_settings)
            setDisplayHomeAsUpEnabled(true)
        }

        binding.run {
            buttonAddSchedule.setOnClickListener {
                dotIndicatorPager2Adapter.instantiateItem(binding.viewPager, binding.viewPager.currentItem).run {
                    if (this is SettingsScheduleFragment) openAlarmDialog(alarmViewModel.makeTemporaryAlarm())
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.fragment_settings_schedule, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.openManual -> {
                val customTabsIntent =
                    CustomTabsIntent.Builder().setUrlBarHidingEnabled(false).build()
                customTabsIntent.launchUrl(
                    this@BaseSettingsActivity,
                    Uri.parse(manualUrl()),
                )
            }
        }
        return super.onOptionsItemSelected(item)
    }

    abstract fun manualUrl(): String

    fun updateUI() {
        super.onResume()
    }

    fun getProgressContainer() = binding.partialSettingsProgress.progressContainer
}

