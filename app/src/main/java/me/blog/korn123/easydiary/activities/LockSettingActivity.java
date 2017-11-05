package me.blog.korn123.easydiary.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.blog.korn123.commons.constants.Constants;
import me.blog.korn123.commons.utils.CommonUtils;
import me.blog.korn123.commons.utils.FontUtils;
import me.blog.korn123.easydiary.R;

/**
 * Created by hanjoong on 2017-05-03.
 */

public class LockSettingActivity extends Activity {

    @BindView(R.id.pass1)
    TextView mPass1;

    @BindView(R.id.pass2)
    TextView mPass2;

    @BindView(R.id.pass3)
    TextView mPass3;

    @BindView(R.id.pass4)
    TextView mPass4;

    @BindView(R.id.infoMessage)
    TextView mInfoMessage;

    TextView[] mPasswordView = new TextView[4];

    private int mCursorIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_setting);
        ButterKnife.bind(this);
        setFontsStyle();

        mPasswordView[0] = mPass1;
        mPasswordView[1] = mPass2;
        mPasswordView[2] = mPass3;
        mPasswordView[3] = mPass4;
    }

    @OnClick({R.id.num0, R.id.num1, R.id.num2, R.id.num3, R.id.num4, R.id.num5, R.id.num6, R.id.num7, R.id.num8, R.id.num9})
    void onClick(View view) {
        String password = "";

        switch(view.getId()) {
            case R.id.num0:
                password = "0";
                break;
            case R.id.num1:
                password = "1";
                break;
            case R.id.num2:
                password = "2";
                break;
            case R.id.num3:
                password = "3";
                break;
            case R.id.num4:
                password = "4";
                break;
            case R.id.num5:
                password = "5";
                break;
            case R.id.num6:
                password = "6";
                break;
            case R.id.num7:
                password = "7";
                break;
            case R.id.num8:
                password = "8";
                break;
            case R.id.num9:
                password = "9";
                break;
        }

        mPasswordView[mCursorIndex].setText(password);
        if (mCursorIndex == 3) {
            String fullPassword = "";
            for (TextView tv : mPasswordView) {
                fullPassword += tv.getText();
            }
            getIntent().putExtra(Constants.APP_LOCK_REQUEST_PASSWORD, fullPassword);
            setResult(RESULT_OK, getIntent());
            finish();
        } else {
            mCursorIndex++;
        }
    }

    private void setFontsStyle() {
        FontUtils.setFontsTypeface(getApplicationContext(), getAssets(), null, (ViewGroup) findViewById(android.R.id.content));
        float fontSize = CommonUtils.loadFloatPreference(this, Constants.SETTING_FONT_SIZE, -1);
        if (fontSize > 0) FontUtils.setFontsSize(fontSize, (ViewGroup) findViewById(android.R.id.content));
    }

}
