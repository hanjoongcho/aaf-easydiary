package me.blog.korn123.easydiary.helper;

import android.app.Application;

import io.realm.Realm;

/**
 * Created by CHO HANJOONG on 2017-03-16.
 */

public class EasyDiaryApplication extends Application {
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
    }
}
