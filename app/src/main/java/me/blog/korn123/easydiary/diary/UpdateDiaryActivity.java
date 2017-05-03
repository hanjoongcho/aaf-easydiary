package me.blog.korn123.easydiary.diary;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.blog.korn123.commons.constants.Constants;
import me.blog.korn123.commons.utils.CommonUtils;
import me.blog.korn123.commons.utils.DialogUtils;
import me.blog.korn123.commons.utils.FontUtils;
import me.blog.korn123.easydiary.R;
import me.blog.korn123.easydiary.helper.EasyDiaryActivity;
import me.blog.korn123.easydiary.setting.SettingsActivity;

/**
 * Created by CHO HANJOONG on 2017-03-16.
 */

public class UpdateDiaryActivity extends EasyDiaryActivity {

    private final int REQUEST_CODE_SPEECH_INPUT = 100;
    private Intent mRecognizerIntent;
    private long mCurrentTimeMillis;
    private int mSequence;
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
        setContentView(R.layout.activity_update_diary);
        ButterKnife.bind(this);
        mCurrentTimeMillis = System.currentTimeMillis();
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.update_diary_title));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        mRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        FontUtils.setTypeface(getAssets(), this.mContents);
        FontUtils.setTypeface(getAssets(), this.mTitle);

        bindView();
        bindEvent();
        initFontStyle();
        initData();
        initSpinner();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    public void initSpinner() {
        String[]  weatherArr = {"날씨", "맑음", "흐림", "비", "번개", "눈"};
        ArrayAdapter arrayAdapter = new DiaryWeatherArrayAdapter(UpdateDiaryActivity.this, R.layout.spinner_item_diary_weather_array_adapter, Arrays.asList(weatherArr));
        mWeatherSpinner.setAdapter(arrayAdapter);
        mWeatherSpinner.setSelection(getIntent().getIntExtra("weather", 0));
    }

    public void initData() {
        Intent intent = getIntent();
        mTitle.setText(intent.getStringExtra("title"));
        getSupportActionBar().setSubtitle(getString(R.string.write_date) + ": " + intent.getStringExtra("date"));
        mContents.setText(intent.getStringExtra("contents"));
        mSequence = intent.getIntExtra("sequence", 0);
        mCurrentTimeMillis = intent.getLongExtra("current_time_millis", 0);
        mContents.requestFocus();
    }

    public void initFontStyle() {
        float fontSize = CommonUtils.loadFloatPreference(UpdateDiaryActivity.this, "font_size", 0);
        if (fontSize > 0) {
            mContents.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize);
        }

        FontUtils.setTypeface(getAssets(), mTitle);
        FontUtils.setTypeface(getAssets(), mContents);
    }

    private void bindView() {
    }

    private void bindEvent() {

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
                CommonUtils.saveFloatPreference(UpdateDiaryActivity.this, "font_size", fontSize + 5);
                break;
            case R.id.zoomOut:
                mContents.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize - 5);
                CommonUtils.saveFloatPreference(UpdateDiaryActivity.this, "font_size", fontSize - 5);
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
                            mSequence,
                            mCurrentTimeMillis,
                            String.valueOf(mTitle.getText()),
                            String.valueOf(mContents.getText())
                    );
                    diaryDto.setWeather(mWeatherSpinner.getSelectedItemPosition());
                    DiaryDao.updateDiary(diaryDto);
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
                CommonUtils.saveLongPreference(UpdateDiaryActivity.this, Constants.PAUSE_MILLIS, System.currentTimeMillis());
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
                Intent settingIntent = new Intent(UpdateDiaryActivity.this, SettingsActivity.class);
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
