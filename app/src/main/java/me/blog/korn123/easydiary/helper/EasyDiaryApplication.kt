package me.blog.korn123.easydiary.helper

import android.content.Context
import androidx.multidex.MultiDexApplication
import dagger.hilt.android.HiltAndroidApp
import io.realm.Realm

/**
 * Created by CHO HANJOONG on 2017-03-16.
 */

@HiltAndroidApp
class EasyDiaryApplication : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
        context = this
    }

    companion object {
        var context: Context? = null
    }
}