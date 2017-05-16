package me.blog.korn123.easydiary.diary;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.blog.korn123.commons.constants.Constants;
import me.blog.korn123.commons.utils.CommonUtils;
import me.blog.korn123.commons.utils.DateUtils;
import me.blog.korn123.commons.utils.DialogUtils;
import me.blog.korn123.commons.utils.FontUtils;
import me.blog.korn123.easydiary.R;
import me.blog.korn123.easydiary.helper.EasyDiaryActivity;
import me.blog.korn123.easydiary.setting.SettingsActivity;

/**
 * Created by CHO HANJOONG on 2017-03-16.
 */

public class CreateDiaryActivity extends EasyDiaryActivity {

    private final int REQUEST_CODE_SPEECH_INPUT = 100;
    private Intent mRecognizerIntent;
    private Switch mInputMode;
    private long mCurrentTimeMillis;
    private int mCurrentCursor = 1;

    @BindView(R.id.contents)
    EditText mContents;

    @BindView(R.id.title)
    EditText mTitle;

    @BindView(R.id.saveContents)
    ImageView mSaveContents;

    @BindView(R.id.weatherSpinner)
    Spinner mWeatherSpinner;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_diary);
        ButterKnife.bind(this);
        mCurrentTimeMillis = System.currentTimeMillis();
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.create_diary_title));
        getSupportActionBar().setSubtitle(getString(R.string.write_date) + ": " + DateUtils.timeMillisToDateTime(this.mCurrentTimeMillis));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        mRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        FontUtils.setToolbarTypeface(toolbar, Typeface.DEFAULT);
        FontUtils.setTypeface(getAssets(), this.mContents);
        FontUtils.setTypeface(getAssets(), this.mTitle);

        bindView();
        bindEvent();
        initFontStyle();
        initSpinner();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    public void initSpinner() {
        String[]  weatherArr = {"날씨", "맑음", "흐림", "비", "번개", "눈"};
        ArrayAdapter arrayAdapter = new DiaryWeatherArrayAdapter(CreateDiaryActivity.this, R.layout.spinner_item_diary_weather_array_adapter, Arrays.asList(weatherArr));
        mWeatherSpinner.setAdapter(arrayAdapter);
    }

    public void initFontStyle() {
        float fontSize = CommonUtils.loadFloatPreference(CreateDiaryActivity.this, "font_size", 0);
        if (fontSize > 0) {
            mContents.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize);
        }

        FontUtils.setTypeface(getAssets(), mTitle);
        FontUtils.setTypeface(getAssets(), mContents);
    }

    private void bindView() {
        mInputMode = ((Switch) findViewById(R.id.inputMode));
    }

    private void bindEvent() {
        mInputMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    mTitle.setEnabled(false);
                    mContents.setEnabled(false);
                    DialogUtils.makeSnackBar(findViewById(android.R.id.content), getString(R.string.input_mode_a));
                } else {
                    CreateDiaryActivity.this.mTitle.setEnabled(true);
                    CreateDiaryActivity.this.mContents.setEnabled(true);
                    InputMethodManager imm = (InputMethodManager)CreateDiaryActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (StringUtils.isEmpty(CreateDiaryActivity.this.mTitle.getText())) {
                        imm.showSoftInput(mTitle, InputMethodManager.HIDE_IMPLICIT_ONLY);
                        CreateDiaryActivity.this.mTitle.clearFocus();
                    } else  {
                        CreateDiaryActivity.this.mContents.requestFocus();
                        imm.showSoftInput(mContents, InputMethodManager.HIDE_IMPLICIT_ONLY);
                        CreateDiaryActivity.this.mContents.setSelection(CreateDiaryActivity.this.mContents.getText().length());
                    }
                    DialogUtils.makeSnackBar(findViewById(android.R.id.content), getString(R.string.input_mode_b));
                }
            }
        });

        mTitle.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mCurrentCursor = 0;
                return false;
            }
        });

        mContents.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mCurrentCursor = 1;
                return false;
            }
        });
    }

    @OnClick({R.id.speechButton, R.id.zoomIn, R.id.zoomOut, R.id.saveContents})
    public void onClick(View view) {
        float fontSize = mContents.getTextSize();

        switch(view.getId()) {
            case R.id.speechButton:
                showSpeechDialog();
                break;
            case R.id.zoomIn:
                mContents.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize + 5);
                CommonUtils.saveFloatPreference(CreateDiaryActivity.this, "font_size", fontSize + 5);
                break;
            case R.id.zoomOut:
                mContents.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize - 5);
                CommonUtils.saveFloatPreference(CreateDiaryActivity.this, "font_size", fontSize - 5);
                break;
            case R.id.saveContents:
                if (StringUtils.isEmpty(mTitle.getText())) {
                    mTitle.requestFocus();
                    DialogUtils.makeSnackBar(findViewById(android.R.id.content), getString(R.string.request_title_message));
                } else if (StringUtils.isEmpty(mContents.getText())) {
                    mContents.requestFocus();
                    DialogUtils.makeSnackBar(findViewById(android.R.id.content), getString(R.string.request_content_message));
                } else {
                    DiaryDto diaryDto = new DiaryDto(
                            -1,
                            mCurrentTimeMillis,
                            String.valueOf(CreateDiaryActivity.this.mTitle.getText()),
                            String.valueOf(CreateDiaryActivity.this.mContents.getText()),
                            mWeatherSpinner.getSelectedItemPosition()
                    );
                    DiaryDao.createDiary(diaryDto);
                    CommonUtils.saveIntPreference(CreateDiaryActivity.this, Constants.PREVIOUS_ACTIVITY, Constants.PREVIOUS_ACTIVITY_CREATE);
                    finish();
                }
                break;
        }
    }

    private void showSpeechDialog() {
        startActivityForResult(this.mRecognizerIntent, REQUEST_CODE_SPEECH_INPUT);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_SPEECH_INPUT:
                if ((resultCode == RESULT_OK) && (data != null)) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (mInputMode.isChecked()) {
                        if ((resultCode == RESULT_OK) && (data != null)) {
                            if (StringUtils.isEmpty(this.mTitle.getText())) {
                                this.mTitle.setText((CharSequence)result.get(0));
                                CreateDiaryActivity.this.mContents.requestFocus();
                            } else {
                                this.mContents.setText(String.valueOf(this.mContents.getText()) + (String)result.get(0) + "\n");
                                this.mContents.setSelection(this.mContents.getText().length());
                            }
                        }
                    } else {
                        if (mCurrentCursor == 0) { // edit title
                            String title = String.valueOf(mTitle.getText());
                            StringBuilder sb = new StringBuilder(title);
                            sb.insert(mTitle.getSelectionStart(), result.get(0));
                            int cursorPosition = mTitle.getSelectionStart() + result.get(0).length();
                            mTitle.setText(sb.toString());
                            mTitle.setSelection(cursorPosition);
                        } else {                   // edit contents
                            String contents = String.valueOf(mContents.getText());
                            StringBuilder sb = new StringBuilder(contents);
                            sb.insert(mContents.getSelectionStart(), result.get(0));
                            int cursorPosition = mContents.getSelectionStart() + result.get(0).length();
                            mContents.setText(sb.toString());
                            mContents.setSelection(cursorPosition);
                        }
                    }
                }
                CommonUtils.saveLongPreference(CreateDiaryActivity.this, Constants.PAUSE_MILLIS, System.currentTimeMillis());
                break;
            default:
                break;
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
//                this.overridePendingTransition(R.anim.anim_left_to_center, R.anim.anim_center_to_right);
                break;
            case R.id.action_settings:
//                DialogUtils.makeSnackBar(findViewById(R.id.contents), getString(R.string.notice_message));
                Intent settingIntent = new Intent(CreateDiaryActivity.this, SettingsActivity.class);
                startActivity(settingIntent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

}
