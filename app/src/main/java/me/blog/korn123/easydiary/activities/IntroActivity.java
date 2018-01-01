package me.blog.korn123.easydiary.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.blog.korn123.commons.constants.Constants;
import me.blog.korn123.commons.utils.CommonUtils;
import me.blog.korn123.commons.utils.FontUtils;
import me.blog.korn123.easydiary.R;
import me.blog.korn123.easydiary.helper.TransitionHelper;

/**
 * Created by CHO HANJOONG on 2016-12-31.
 */

public class IntroActivity extends AppCompatActivity implements Handler.Callback {

    private final int START_MAIN_ACTIVITY = 0;

    @BindView(R.id.appName)
    TextView mAppName;

    @BindView(R.id.companyName)
    TextView mCompanyName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        ButterKnife.bind(this);

        // determine device language
        if (!Locale.getDefault().getLanguage().matches(Constants.CUSTOM_FONTS_SUPPORT_LANGUAGE)) {

            // Initial font typeface setting
            if (!CommonUtils.preferencesContains(this, Constants.SETTING_FONT_NAME)) {
                CommonUtils.saveStringPreference(this, Constants.SETTING_FONT_NAME, Constants.CUSTOM_FONTS_UNSUPPORTED_LANGUAGE_DEFAULT);
            }

            // Initial font size setting
            if (!CommonUtils.preferencesContains(this, Constants.SETTING_FONT_SIZE)) {
                CommonUtils.saveFloatPreference(this, Constants.SETTING_FONT_SIZE, CommonUtils.dpToPixel(this, 15));
            }
        } else {
            // Initial font size setting
            if (!CommonUtils.preferencesContains(this, Constants.SETTING_FONT_SIZE)) {
                CommonUtils.saveFloatPreference(this, Constants.SETTING_FONT_SIZE, CommonUtils.dpToPixel(this, 20));
            }
        }

        setFontsStyle();
        new Handler(this).sendEmptyMessageDelayed(START_MAIN_ACTIVITY, 500);
    }

    @Override
    public boolean handleMessage(Message message) {
        switch (message.what) {
            case START_MAIN_ACTIVITY:
                TransitionHelper.startActivityWithTransition(IntroActivity.this, new Intent(this, DiaryMainActivity.class));
//                finish();
                break;
            default:
                break;
        }
        return false;
    }

    private void setFontsStyle() {
        FontUtils.setFontsTypeface(getApplicationContext(), getAssets(), null, (ViewGroup) findViewById(android.R.id.content));
        float fontSize = CommonUtils.loadFloatPreference(this, Constants.SETTING_FONT_SIZE, -1);
        if (fontSize > 0) FontUtils.setFontsSize(fontSize, (ViewGroup) findViewById(android.R.id.content));
    }

//    private void setFontsSize() {
//        float commonSize = CommonUtils.loadFloatPreference(IntroActivity.this, Constants.SETTING_FONT_SIZE, mAppName.getTextSize());
//        FontUtils.setFontsSize(commonSize, -1, mAppName, mCompanyName);
//    }

}
