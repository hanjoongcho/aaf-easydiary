package me.blog.korn123.easydiary.helper

import androidx.multidex.MultiDexApplication
import dagger.hilt.android.HiltAndroidApp

/**
 * Created by CHO HANJOONG on 2017-03-16.
 */

@HiltAndroidApp
class EasyDiaryApplication : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
    }
}
