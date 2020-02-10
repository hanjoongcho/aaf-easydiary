package me.blog.korn123.easydiary.helper

import androidx.multidex.MultiDexApplication
import io.realm.Realm

/**
 * Created by CHO HANJOONG on 2017-03-16.
 */

class EasyDiaryApplication : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
    }
}
