package me.blog.korn123.easydiary.diary;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
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
import me.blog.korn123.easydiary.setting.SettingsActivity;

/**
 * Created by CHO HANJOONG on 2017-03-16.
 */

public class ReadDiaryDetailActivity extends AppCompatActivity {

    private long mCurrentTimeMillis;
    private int mSequence;
    private TextToSpeech mTextToSpeech;

    @BindView(R.id.contents)
    TextView mContents;

    @BindView(R.id.title)
    TextView mTitle;

    @BindView(R.id.date)
    TextView mDate;

    @BindView(R.id.weather)
    ImageView mWeather;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_diary_detail);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.read_diary_detail_title));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        mTitle.setText(intent.getStringExtra("title"));
        mDate.setText(intent.getStringExtra("date"));
        mContents.setText(intent.getStringExtra("contents"));
        mSequence = intent.getIntExtra("sequence", 0);
        mCurrentTimeMillis = intent.getLongExtra("current_time_millis", 0);

        int weather = intent.getIntExtra("weather", 0);
        switch (weather) {
            case 0:
                mWeather.setVisibility(View.GONE);
                break;
            case Constants.SUN:
                mWeather.setImageResource(R.drawable.ic_sun);
                break;
            case Constants.SUN_AND_CLOUD:
                mWeather.setImageResource(R.drawable.ic_cloud);
                break;
            case Constants.RAIN:
                mWeather.setImageResource(R.drawable.ic_rain);
                break;
            case Constants.THUNDER_BOLT:
                mWeather.setImageResource(R.drawable.ic_storm);
                break;
            case Constants.SNOW:
                mWeather.setImageResource(R.drawable.ic_snow_2);
                break;
        }

        initFontStyle();
    }

    private void initModule() {
        mTextToSpeech = new TextToSpeech(ReadDiaryDetailActivity.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    mTextToSpeech.setLanguage(Locale.getDefault());
                    mTextToSpeech.setPitch(1.3f);
                    mTextToSpeech.setSpeechRate(1f);
                }
            }
        });
    }

    private void textToSpeech() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ttsGreater21(String.valueOf(mContents.getText()));
        } else {
            ttsUnder20(String.valueOf(mContents.getText()));
        }
    }

    private void destroyModule() {
        if (mTextToSpeech != null) {
            mTextToSpeech.stop();
            mTextToSpeech.shutdown();
            mTextToSpeech = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initModule();
    }

    @Override
    protected void onPause() {
        super.onPause();
        destroyModule();
    }

    @SuppressWarnings("deprecation")
    private void ttsUnder20(String text) {
        HashMap<String, String> map = new HashMap<>();
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "MessageId");

        mTextToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onError(String utteranceId) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onDone(String utteranceId) {
                // TODO Auto-generated method stub
            }
        });

        mTextToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, map);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void ttsGreater21(String text) {
        String utteranceId = this.hashCode() + "";
        mTextToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
    }


    public void initFontStyle() {
        float fontSize = CommonUtils.loadFloatPreference(ReadDiaryDetailActivity.this, "font_size", 0);
        if (fontSize > 0) {
            mContents.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize);
        }

        FontUtils.setTypeface(getAssets(), mTitle);
        FontUtils.setTypeface(getAssets(), mDate);
        FontUtils.setTypeface(getAssets(), mContents);
    }

    @OnClick({R.id.zoomIn, R.id.zoomOut, R.id.delete, R.id.edit, R.id.speechOutButton})
    public void onClick(View view) {
        float fontSize = mContents.getTextSize();

        switch(view.getId()) {
            case R.id.zoomIn:
                mContents.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize + 5);
                CommonUtils.saveFloatPreference(ReadDiaryDetailActivity.this, "font_size", fontSize + 5);
                break;
            case R.id.zoomOut:
                mContents.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize - 5);
                CommonUtils.saveFloatPreference(ReadDiaryDetailActivity.this, "font_size", fontSize - 5);
                break;
            case R.id.delete:
                DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        DiaryDao.deleteDiary(mSequence);
                        finish();
                    }
                };
                DialogInterface.OnClickListener negativeListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                };
                DialogUtils.showAlertDialog(ReadDiaryDetailActivity.this, getString(R.string.delete_confirm), positiveListener, negativeListener);
                break;
            case R.id.edit:
                Intent updateDiaryIntent = new Intent(ReadDiaryDetailActivity.this, UpdateDiaryActivity.class);
                updateDiaryIntent.putExtra("sequence", mSequence);
                updateDiaryIntent.putExtra("title", String.valueOf(mTitle.getText()));
                updateDiaryIntent.putExtra("contents", String.valueOf(mContents.getText()));
                updateDiaryIntent.putExtra("date", String.valueOf(mDate.getText()));
                updateDiaryIntent.putExtra("current_time_millis", mCurrentTimeMillis);
                updateDiaryIntent.putExtra("current_time_millis", mCurrentTimeMillis);
                updateDiaryIntent.putExtra("weather", getIntent().getIntExtra("weather", 0));
                startActivity(updateDiaryIntent);
                finish();
                break;
            case R.id.speechOutButton:
                textToSpeech();
                break;
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case android.R.id.home:
                finish();
//                this.overridePendingTransition(R.anim.anim_left_to_center, R.anim.anim_center_to_right);
                break;
            case R.id.action_settings:
//                DialogUtils.makeSnackBar(findViewById(R.id.contents), getString(R.string.notice_message));
                Intent settingIntent = new Intent(ReadDiaryDetailActivity.this, SettingsActivity.class);
                startActivity(settingIntent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
}
