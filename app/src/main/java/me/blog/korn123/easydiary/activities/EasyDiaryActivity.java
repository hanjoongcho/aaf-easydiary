package me.blog.korn123.easydiary.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import me.blog.korn123.commons.constants.Constants;
import me.blog.korn123.commons.utils.CommonUtils;

/**
 * Created by hanjoong on 2017-05-03.
 */

public class EasyDiaryActivity extends AppCompatActivity {

    @Override
    protected void onPause() {
        super.onPause();
        boolean enableLock = CommonUtils.loadBooleanPreference(EasyDiaryActivity.this, "application_lock");
        if (enableLock) {
            long currentMillis = System.currentTimeMillis();
            CommonUtils.saveLongPreference(EasyDiaryActivity.this, Constants.SETTING_PAUSE_MILLIS, currentMillis);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean enableLock = CommonUtils.loadBooleanPreference(EasyDiaryActivity.this, "application_lock");
        long pauseMillis = CommonUtils.loadLongPreference(EasyDiaryActivity.this, Constants.SETTING_PAUSE_MILLIS, 0);
        if (enableLock && pauseMillis != 0) {
            if (System.currentTimeMillis() - pauseMillis > 1000) {
                // 잠금해제 화면
                Intent lockDiaryIntent = new Intent(EasyDiaryActivity.this, DiaryLockActivity.class);
                startActivity(lockDiaryIntent);
            }
        }
    }
}
