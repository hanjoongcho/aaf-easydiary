package me.blog.korn123.easydiary.helper

import android.support.multidex.MultiDexApplication
import com.squareup.leakcanary.LeakCanary
import io.realm.Realm
import me.blog.korn123.easydiary.BuildConfig

/**
 * Created by CHO HANJOONG on 2017-03-16.
 */

class EasyDiaryApplication : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            if (LeakCanary.isInAnalyzerProcess(this)) {
                return
            }
            LeakCanary.install(this)
        }
        Realm.init(this)
    }
}
