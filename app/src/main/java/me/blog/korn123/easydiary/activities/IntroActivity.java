package me.blog.korn123.easydiary.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.TypedValue;
import android.widget.TextView;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.blog.korn123.commons.constants.Constants;
import me.blog.korn123.commons.utils.CommonUtils;
import me.blog.korn123.commons.utils.FontUtils;
import me.blog.korn123.easydiary.R;
import me.blog.korn123.easydiary.diary.ReadDiaryActivity;

/**
 * Created by CHO HANJOONG on 2016-12-31.
 */

public class IntroActivity extends Activity implements Handler.Callback {

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
            CommonUtils.saveStringPreference(this, "font_setting", "Default");
        }

        FontUtils.setTypeface(this, getAssets(), mAppName);
        FontUtils.setTypeface(this, getAssets(), mCompanyName);
        float fontSize = CommonUtils.loadFloatPreference(this, "font_size", 0);
        if (fontSize > 0) {
            mAppName.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize);
            mCompanyName.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize);
        }
        new Handler(this).sendEmptyMessageDelayed(START_MAIN_ACTIVITY, 500);
    }

    @Override
    public boolean handleMessage(Message message) {
        switch (message.what) {
            case START_MAIN_ACTIVITY:
                startActivity(new Intent(this, ReadDiaryActivity.class));
                finish();
                break;
            default:
                break;
        }
        return false;
    }

}
