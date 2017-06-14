package me.blog.korn123.easydiary.diary;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.blog.korn123.commons.constants.Constants;
import me.blog.korn123.commons.utils.CommonUtils;
import me.blog.korn123.commons.utils.DateUtils;
import me.blog.korn123.commons.utils.DialogUtils;
import me.blog.korn123.commons.utils.FontUtils;
import me.blog.korn123.easydiary.calendar.CalendarActivity;
import me.blog.korn123.easydiary.R;
import me.blog.korn123.easydiary.chart.BarChartActivity;
import me.blog.korn123.easydiary.helper.EasyDiaryActivity;
import me.blog.korn123.easydiary.setting.SettingsActivity;

/**
 * Created by CHO HANJOONG on 2017-03-16.
 */

public class ReadDiaryActivity extends EasyDiaryActivity {

    private final int REQUEST_CODE_SPEECH_INPUT = 100;
    private Intent mRecognizerIntent;
    private Switch mInputMode;
    private FloatingActionButton mSpeechButton;
    private long mCurrentTimeMillis;
    private ArrayAdapter<DiaryDto> mArrayAdapterDiary;
    private List<DiaryDto> mDiaryList;

    @BindView(R.id.diaryList)
    ListView mDiaryListView;

    @BindView(R.id.query)
    EditText mQuery;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_diary);
        ButterKnife.bind(this);

        // application finish 확인
        if(getIntent().getBooleanExtra("app_finish", false)) {
            finish();
        }

        mCurrentTimeMillis = System.currentTimeMillis();
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.read_diary_title));

        mRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        mRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        mDiaryList = DiaryDao.readDiary(null);
        mArrayAdapterDiary = new DiaryCardArrayAdapter(this, R.layout.list_item_diary_card_array_adapter , this.mDiaryList);
        mDiaryListView.setAdapter(this.mArrayAdapterDiary);

        FontUtils.setToolbarTypeface(toolbar, Typeface.DEFAULT);
        FontUtils.setTypeface(getAssets(), this.mQuery);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        if (!CommonUtils.loadBooleanPreference(this, "init_dummy_data")) {
            initSampleData();
            CommonUtils.saveBooleanPreference(this, "init_dummy_data", true);
        }

        bindView();
        bindEvent();
    }



    private void bindView() {
        mInputMode = ((Switch) findViewById(R.id.inputMode));
        mSpeechButton = (FloatingActionButton)findViewById(R.id.speechButton);
    }

    private void bindEvent() {
        mInputMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    mQuery.setEnabled(false);
                    mSpeechButton.setVisibility(View.VISIBLE);
                    DialogUtils.makeSnackBar(findViewById(android.R.id.content), getString(R.string.input_mode_a));
                } else {
                    mQuery.setEnabled(true);
                    mSpeechButton.setVisibility(View.GONE);
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(mQuery, InputMethodManager.HIDE_IMPLICIT_ONLY);
                    DialogUtils.makeSnackBar(findViewById(android.R.id.content), getString(R.string.input_mode_c));
                }
            }
        });

        mQuery.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                refreshList();
            }

            public void afterTextChanged(Editable editable) {}
        });

        mDiaryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                DiaryDto diaryDto = (DiaryDto)adapterView.getAdapter().getItem(i);
                Intent detailIntent = new Intent(ReadDiaryActivity.this, ReadDiaryDetailActivity.class);
                detailIntent.putExtra("sequence", diaryDto.getSequence());
                detailIntent.putExtra("title", diaryDto.getTitle());
                detailIntent.putExtra("contents", diaryDto.getContents());
                detailIntent.putExtra("date", DateUtils.timeMillisToDateTime(diaryDto.getCurrentTimeMillis()));
                detailIntent.putExtra("current_time_millis", diaryDto.getCurrentTimeMillis());
                detailIntent.putExtra("weather", diaryDto.getWeather());
                startActivity(detailIntent);
            }
        });
    }

    @OnClick({R.id.speechButton, R.id.insertDiaryButton})
    void onClick(View view) {
        switch(view.getId()) {
            case R.id.speechButton:
                showSpeechDialog();
                break;
            case R.id.insertDiaryButton:
                Intent createDiary = new Intent(ReadDiaryActivity.this, CreateDiaryActivity.class);
                startActivity(createDiary);
//                ReadDiaryActivity.this.overridePendingTransition(R.anim.anim_right_to_center, R.anim.anim_center_to_left);
                break;
        }
    }

    private void showSpeechDialog() {
        try {
            startActivityForResult(mRecognizerIntent, REQUEST_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException e) {
            DialogUtils.showAlertDialog(this, getString(R.string.recognizer_intent_not_found_message), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_SPEECH_INPUT:
                if ((resultCode == RESULT_OK) && (data != null)) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    mQuery.setText(result.get(0));
                }
                CommonUtils.saveLongPreference(ReadDiaryActivity.this, Constants.PAUSE_MILLIS, System.currentTimeMillis());
                break;
        }
    }

    protected void onResume() {
        super.onResume();
        refreshList();
        int previousActivity = CommonUtils.loadIntPreference(ReadDiaryActivity.this, Constants.PREVIOUS_ACTIVITY, -1);
        if (previousActivity == Constants.PREVIOUS_ACTIVITY_CREATE) {
            mDiaryListView.smoothScrollToPosition(0);
//            mDiaryListView.setSelection(0);
            CommonUtils.saveIntPreference(ReadDiaryActivity.this, Constants.PREVIOUS_ACTIVITY, -1);
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.settings:
                Intent settingIntent = new Intent(ReadDiaryActivity.this, SettingsActivity.class);
                startActivity(settingIntent);
                break;
            case R.id.chart:
                Intent chartIntent = new Intent(ReadDiaryActivity.this, BarChartActivity.class);
                startActivity(chartIntent);
                break;
            case R.id.planner:
                Intent calendarIntent = new Intent(ReadDiaryActivity.this, CalendarActivity.class);
                startActivity(calendarIntent);
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.read_diary, menu);
        return true;
    }

    public void refreshList() {
        String query = "";
        if (!StringUtils.isEmpty(mQuery.getText())) query = String.valueOf(mQuery.getText());

        if (this.mDiaryList != null) {
            this.mDiaryList.clear();
            this.mDiaryList.addAll(DiaryDao.readDiary(query));
            this.mArrayAdapterDiary.notifyDataSetChanged();
        }
    }

    private void initSampleData() {
        DiaryDao.createDiary(new DiaryDto(
                -1,
                this.mCurrentTimeMillis - 395000000L, getString(R.string.sample_diary_title_1), getString(R.string.sample_diary_1),
                1
        ));
        DiaryDao.createDiary(new DiaryDto(
                -1,
                this.mCurrentTimeMillis - 263000000L, getString(R.string.sample_diary_title_2), getString(R.string.sample_diary_2),
                2
        ));
        DiaryDao.createDiary(new DiaryDto(
                -1,
                this.mCurrentTimeMillis - 132000000L, getString(R.string.sample_diary_title_3), getString(R.string.sample_diary_3),
                3
        ));
        DiaryDao.createDiary(new DiaryDto(
                -1,
                this.mCurrentTimeMillis - 4000000L, getString(R.string.sample_diary_title_4), getString(R.string.sample_diary_4),
                4
        ));

//        for (int i = 0; i < 50; i++) {
//            DiaryDao.createDiary(this.mCurrentTimeMillis - (i*3600000), "나쁜 소식을 많이 들을수록 기뻐해야 한다.", "여러분이 지도자로서 가장 들을 필요가 있는 것이\n" +
//                    "바로 나쁜 소식이다.\n" +
//                    "좋은 소식은 내일도 좋은 것이지만\n" +
//                    "나쁜 소식은 내일이면 더 나빠질 것이다.\n" +
//                    "바로 이것이 비록 사실이 가슴 아프더라도\n" +
//                    "언제나 까다로운 질문을 하고 사실을 말하는 것이 안전한 이유다.\n" +
//                    "- 에릭 슈미트 구글 회장, ‘구글은 어떻게 일하는가?’에서");
//        }
    }

}
