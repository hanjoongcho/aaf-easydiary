package me.blog.korn123.easydiary.activities

import android.os.Bundle
import kotlinx.android.synthetic.main.activity_diary_read.*
import me.blog.korn123.easydiary.R

class DevActivity : EasyDiaryActivity() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dev)
        setSupportActionBar(toolbar)
        supportActionBar?.run {
            title = "Easy-Diary Dev Mode"
            setDisplayHomeAsUpEnabled(true)
        }
    }
}