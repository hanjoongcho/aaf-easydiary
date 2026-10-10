package me.blog.korn123.easydiary.presentation.calendar

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import me.blog.korn123.easydiary.extensions.applyFullScreenStatusBarTheme
import me.blog.korn123.easydiary.extensions.updateNavigationBarAppearance
import me.blog.korn123.easydiary.presentation.base.EasyDiaryComposeBaseActivity

@AndroidEntryPoint
class CalendarActivity : EasyDiaryComposeBaseActivity() {
    private val calendarViewModel: CalendarViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        applyFullScreenStatusBarTheme()
        updateNavigationBarAppearance()
        setContent {
            CalendarScreen(calendarViewModel = calendarViewModel)
        }
    }
}
