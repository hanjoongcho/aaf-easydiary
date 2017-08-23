package me.blog.korn123.easydiary.diary;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Switch;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import me.blog.korn123.commons.constants.Constants;
import me.blog.korn123.commons.utils.CommonUtils;
import me.blog.korn123.commons.utils.DateUtils;
import me.blog.korn123.commons.utils.DialogUtils;
import me.blog.korn123.commons.utils.FontUtils;
import me.blog.korn123.easydiary.R;
import me.blog.korn123.easydiary.calendar.CalendarActivity;
import me.blog.korn123.easydiary.chart.BarChartActivity;
import me.blog.korn123.easydiary.helper.EasyDiaryActivity;
import me.blog.korn123.easydiary.setting.SettingsActivity;
import me.blog.korn123.easydiary.timeline.TimelineActivity;

/**
 * Created by CHO HANJOONG on 2017-03-16.
 */

public class ReadDiaryActivity extends EasyDiaryActivity {

    private final int REQUEST_CODE_SPEECH_INPUT = 100;
    private Intent mRecognizerIntent;
    private FloatingActionButton mSpeechButton;
    private long mCurrentTimeMillis;
    private DiaryCardArrayAdapter mDiaryCardArrayAdapter;
    private List<DiaryDto> mDiaryList;

    @BindView(R.id.diaryList)
    ListView mDiaryListView;

    @BindView(R.id.query)
    EditText mQuery;

    @BindView(R.id.toggleSwitch)
    Switch mToggleSwitch;

    @BindView(R.id.toggleMicOn)
    ImageView mToggleMicOn;

    @BindView(R.id.toggleMicOff)
    ImageView mToggleMicOff;

    @BindView(R.id.insertDiaryButton)
    FloatingActionButton mInsertDiaryButton;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_diary);
        ButterKnife.bind(this);

        // android marshmallow minor version bug workaround
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
            Realm.init(this);
        }

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
        mDiaryCardArrayAdapter = new DiaryCardArrayAdapter(this, R.layout.list_item_diary_card_array_adapter , this.mDiaryList);
        mDiaryListView.setAdapter(mDiaryCardArrayAdapter);

        FontUtils.setToolbarTypeface(toolbar, Typeface.DEFAULT);

        if (!CommonUtils.loadBooleanPreference(this, "init_dummy_data")) {
            initSampleData();
            CommonUtils.saveBooleanPreference(this, "init_dummy_data", true);
        }

        bindView();
        bindEvent();
        initShowcase();
    }

    private int showcaseIndex = 0;
    ShowcaseView mShowcaseView;

    private void initShowcase() {
        int margin = ((Number) (getResources().getDisplayMetrics().density * 12)).intValue();

        final RelativeLayout.LayoutParams centerParams =
                new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        centerParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        centerParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        centerParams.setMargins(0, 0, 0, margin);

        final RelativeLayout.LayoutParams leftParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        leftParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        leftParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        leftParams.setMargins(margin, margin, margin, margin);
        View.OnClickListener showcaseViewOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (showcaseIndex) {
                    case 0:
                        mShowcaseView.setButtonPosition(centerParams);
                        mShowcaseView.setShowcase(new ViewTarget(mQuery), true);
                        mShowcaseView.setContentTitle(getString(R.string.read_diary_showcase_title_2));
                        mShowcaseView.setContentText(getString(R.string.read_diary_showcase_message_2));
                        break;
                    case 1:
                        mShowcaseView.setButtonPosition(centerParams);
                        mShowcaseView.setShowcase(new ViewTarget(mToggleSwitch), true);
                        mShowcaseView.setContentTitle(getString(R.string.read_diary_showcase_title_3));
                        mShowcaseView.setContentText(getString(R.string.read_diary_showcase_message_3));
                        break;
                    case 2:
                        mShowcaseView.setButtonPosition(centerParams);
                        mShowcaseView.setShowcase(new ViewTarget(mDiaryListView), true);
                        mShowcaseView.setContentTitle(getString(R.string.read_diary_showcase_title_8));
                        mShowcaseView.setContentText(getString(R.string.read_diary_showcase_message_8));
                        break;
                    case 3:
                        mShowcaseView.setButtonPosition(centerParams);
                        mShowcaseView.setTarget(new ViewTarget(R.id.planner, ReadDiaryActivity.this));
                        mShowcaseView.setContentTitle(getString(R.string.read_diary_showcase_title_4));
                        mShowcaseView.setContentText(getString(R.string.read_diary_showcase_message_4));
                        break;
                    case 4:
                        mShowcaseView.setButtonPosition(centerParams);
                        mShowcaseView.setTarget(new ViewTarget(R.id.timeline, ReadDiaryActivity.this));
                        mShowcaseView.setContentTitle(getString(R.string.read_diary_showcase_title_5));
                        mShowcaseView.setContentText(getString(R.string.read_diary_showcase_message_5));
                        break;
                    case 5:
                        mShowcaseView.setButtonPosition(centerParams);
                        mShowcaseView.setTarget(new ViewTarget(R.id.chart, ReadDiaryActivity.this));
                        mShowcaseView.setContentTitle(getString(R.string.read_diary_showcase_title_6));
                        mShowcaseView.setContentText(getString(R.string.read_diary_showcase_message_6));
                        break;
                    case 6:
                        mShowcaseView.setButtonPosition(centerParams);
                        mShowcaseView.setTarget(new ViewTarget(R.id.settings, ReadDiaryActivity.this));
                        mShowcaseView.setContentTitle(getString(R.string.read_diary_showcase_title_7));
                        mShowcaseView.setContentText(getString(R.string.read_diary_showcase_message_7));
                        mShowcaseView.setButtonText(getString(R.string.read_diary_showcase_button_2));
                        break;
                    case 7:
                        mShowcaseView.hide();
                        break;
                }
                showcaseIndex++;
            }
        };

        mShowcaseView = new ShowcaseView.Builder(this)
                .withMaterialShowcase()
                .setTarget(new ViewTarget(mInsertDiaryButton))
                .setContentTitle(getString(R.string.read_diary_showcase_title_1))
                .setContentText(getString(R.string.read_diary_showcase_message_1))
                .setStyle(R.style.ShowcaseTheme)
                .singleShot(Constants.SHOWCASE_SINGLE_SHOT_READ_DIARY_NUMBER)
                .setOnClickListener(showcaseViewOnClickListener)
                .build();
        mShowcaseView.setButtonText(getString(R.string.read_diary_showcase_button_1));
        mShowcaseView.setButtonPosition(centerParams);
    }

    private void bindView() {
        mSpeechButton = (FloatingActionButton)findViewById(R.id.speechButton);
    }

    private void bindEvent() {

        mToggleSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    enableRecognizer();
                } else {
                    disableRecognizer();
                }
            }
        });

        mToggleMicOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                disableRecognizer();
            }
        });

        mToggleMicOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enableRecognizer();
            }
        });

        mQuery.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.i("query", String.valueOf(charSequence));
                refreshList(String.valueOf(charSequence));
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
                detailIntent.putExtra("query", mDiaryCardArrayAdapter.getCurrentQuery());
                startActivity(detailIntent);
            }
        });
    }

    private void enableRecognizer() {
        mToggleMicOff.setVisibility(View.GONE);
        mToggleMicOn.setVisibility(View.VISIBLE);
        mSpeechButton.setVisibility(View.VISIBLE);
        mToggleSwitch.setChecked(true);
    }

    private void disableRecognizer() {
        mToggleMicOn.setVisibility(View.GONE);
        mToggleMicOff.setVisibility(View.VISIBLE);
        mSpeechButton.setVisibility(View.GONE);
        mToggleSwitch.setChecked(false);
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
                    mQuery.setSelection(result.get(0).length());
                }
                CommonUtils.saveLongPreference(ReadDiaryActivity.this, Constants.PAUSE_MILLIS, System.currentTimeMillis());
                break;
        }
    }

    protected void onResume() {
        super.onResume();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        FontUtils.setTypeface(this, getAssets(), this.mQuery);
        setDiaryFontSize();

        refreshList();

        int previousActivity = CommonUtils.loadIntPreference(ReadDiaryActivity.this, Constants.PREVIOUS_ACTIVITY, -1);
        if (previousActivity == Constants.PREVIOUS_ACTIVITY_CREATE) {
            mDiaryListView.smoothScrollToPosition(0);
//            mDiaryListView.setSelection(0);
            CommonUtils.saveIntPreference(ReadDiaryActivity.this, Constants.PREVIOUS_ACTIVITY, -1);
        }
    }

    private void setDiaryFontSize() {
        float fontSize = CommonUtils.loadFloatPreference(this, "font_size", 0);
        if (fontSize > 0) {
            mQuery.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize);
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
            case R.id.timeline:
                Intent timelineIntent = new Intent(ReadDiaryActivity.this, TimelineActivity.class);
                startActivity(timelineIntent);
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
        if (StringUtils.isNotEmpty(mQuery.getText())) query = String.valueOf(mQuery.getText());
        refreshList(query);
    }

    public void refreshList(String query) {
        mDiaryList.clear();
        mDiaryList.addAll(DiaryDao.readDiary(query));
        mDiaryCardArrayAdapter.setCurrentQuery(query);
        mDiaryCardArrayAdapter.notifyDataSetChanged();
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
