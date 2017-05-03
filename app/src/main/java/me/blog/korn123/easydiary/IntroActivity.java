package me.blog.korn123.easydiary;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.blog.korn123.commons.utils.FontUtils;
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
        FontUtils.setTypeface(getAssets(), mAppName);
        FontUtils.setTypeface(getAssets(), mCompanyName);
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
