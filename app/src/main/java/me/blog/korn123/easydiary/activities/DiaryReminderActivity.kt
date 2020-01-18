package me.blog.korn123.easydiary.activities

import android.os.Bundle
import me.blog.korn123.easydiary.R

class DiaryReminderActivity : EasyDiaryActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diary_reminder)
        showOverLockScreen()
    }
}