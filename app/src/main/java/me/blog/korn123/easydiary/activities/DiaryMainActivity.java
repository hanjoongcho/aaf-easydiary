package me.blog.korn123.easydiary.activities;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.speech.RecognizerIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;

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
import me.blog.korn123.commons.constants.Path;
import me.blog.korn123.commons.utils.CommonUtils;
import me.blog.korn123.commons.utils.DialogUtils;
import me.blog.korn123.commons.utils.EasyDiaryUtils;
import me.blog.korn123.commons.utils.FontUtils;
import me.blog.korn123.easydiary.R;
import me.blog.korn123.easydiary.adapters.DiaryMainItemAdapter;
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper;
import me.blog.korn123.easydiary.helper.TransitionHelper;
import me.blog.korn123.easydiary.models.DiaryDto;

/**
 * Created by CHO HANJOONG on 2017-03-16.
 */

public class DiaryMainActivity extends EasyDiaryActivity {

    private Intent mRecognizerIntent;

    private FloatingActionButton mSpeechButton;

    private long mCurrentTimeMillis;

    private DiaryMainItemAdapter mDiaryMainItemAdapter;

    private List<DiaryDto> mDiaryList;

    private int mShowcaseIndex = 0;

    private ShowcaseView mShowcaseView;

    @BindView(R.id.diaryList)
    ListView mDiaryListView;

    @BindView(R.id.query)
    EditText mQuery;

    @BindView(R.id.insertDiaryButton)
    FloatingActionButton mInsertDiaryButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_main);
        ButterKnife.bind(this);

        // android marshmallow minor version bug workaround
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
            Realm.init(this);
        }

        // application finish 확인
        if(getIntent().getBooleanExtra(Constants.APP_FINISH_FLAG, false)) {
            finish();
        }

        mCurrentTimeMillis = System.currentTimeMillis();
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.read_diary_title));

        mRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        mRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        mDiaryList = EasyDiaryDbHelper.readDiary(null);
        mDiaryMainItemAdapter = new DiaryMainItemAdapter(this, R.layout.item_diary_main, this.mDiaryList);
        mDiaryListView.setAdapter(mDiaryMainItemAdapter);

        if (!CommonUtils.loadBooleanPreference(this, Constants.INIT_DUMMY_DATA_FLAG)) {
            initSampleData();
            CommonUtils.saveBooleanPreference(this, Constants.INIT_DUMMY_DATA_FLAG, true);
        }

        bindEvent();
        initShowcase();
        EasyDiaryUtils.initWorkingDirectory(Environment.getExternalStorageDirectory().getAbsolutePath() + Path.USER_CUSTOM_FONTS_DIRECTORY);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setFontsStyle();
        refreshList();
        
        int previousActivity = CommonUtils.loadIntPreference(DiaryMainActivity.this, Constants.PREVIOUS_ACTIVITY, -1);
        if (previousActivity == Constants.PREVIOUS_ACTIVITY_CREATE) {
            mDiaryListView.smoothScrollToPosition(0);
//            mDiaryListView.setSelection(0);
            CommonUtils.saveIntPreference(DiaryMainActivity.this, Constants.PREVIOUS_ACTIVITY, -1);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constants.REQUEST_CODE_SPEECH_INPUT:
                if ((resultCode == RESULT_OK) && (data != null)) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    mQuery.setText(result.get(0));
                    mQuery.setSelection(result.get(0).length());
                }
                CommonUtils.saveLongPreference(DiaryMainActivity.this, Constants.SETTING_PAUSE_MILLIS, System.currentTimeMillis());
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.settings:
                Intent settingIntent = new Intent(DiaryMainActivity.this, SettingsActivity.class);
//                startActivity(settingIntent);
                TransitionHelper.startActivityWithTransition(DiaryMainActivity.this, settingIntent);
                break;
            case R.id.chart:
                Intent chartIntent = new Intent(DiaryMainActivity.this, BarChartActivity.class);
//                startActivity(chartIntent);
                TransitionHelper.startActivityWithTransition(DiaryMainActivity.this, chartIntent);
                break;
            case R.id.timeline:
                Intent timelineIntent = new Intent(DiaryMainActivity.this, TimelineActivity.class);
//                startActivity(timelineIntent);
                TransitionHelper.startActivityWithTransition(DiaryMainActivity.this, timelineIntent);
                break;
            case R.id.planner:
                Intent calendarIntent = new Intent(DiaryMainActivity.this, CalendarActivity.class);
//                startActivity(calendarIntent);
                TransitionHelper.startActivityWithTransition(DiaryMainActivity.this, calendarIntent);
                break;
            case R.id.microphone:
                showSpeechDialog();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.diary_main, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        ActivityCompat.finishAffinity(DiaryMainActivity.this);
    }

    @OnClick({R.id.insertDiaryButton})
    void onClick(View view) {
        switch(view.getId()) {
            case R.id.insertDiaryButton:
                Intent createDiary = new Intent(DiaryMainActivity.this, DiaryInsertActivity.class);
//                startActivity(createDiary);
//                DiaryMainActivity.this.overridePendingTransition(R.anim.anim_right_to_center, R.anim.anim_center_to_left);
                TransitionHelper.startActivityWithTransition(DiaryMainActivity.this, createDiary);
                break;
        }
    }

    private void setFontsStyle() {
        FontUtils.setFontsTypeface(getApplicationContext(), getAssets(), null, (ViewGroup) findViewById(android.R.id.content));
        float fontSize = CommonUtils.loadFloatPreference(this, Constants.SETTING_FONT_SIZE, -1);
        if (fontSize > 0) FontUtils.setFontsSize(fontSize, (ViewGroup) findViewById(android.R.id.content));
    }

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
                switch (mShowcaseIndex) {
                    case 0:
                        mShowcaseView.setButtonPosition(centerParams);
                        mShowcaseView.setShowcase(new ViewTarget(mQuery), true);
                        mShowcaseView.setContentTitle(getString(R.string.read_diary_showcase_title_2));
                        mShowcaseView.setContentText(getString(R.string.read_diary_showcase_message_2));
                        break;
                    case 1:
                        mShowcaseView.setButtonPosition(centerParams);
                        mShowcaseView.setShowcase(new ViewTarget(mDiaryListView), true);
                        mShowcaseView.setContentTitle(getString(R.string.read_diary_showcase_title_8));
                        mShowcaseView.setContentText(getString(R.string.read_diary_showcase_message_8));
                        break;
                    case 2:
                        mShowcaseView.setButtonPosition(centerParams);
                        mShowcaseView.setTarget(new ViewTarget(R.id.planner, DiaryMainActivity.this));
                        mShowcaseView.setContentTitle(getString(R.string.read_diary_showcase_title_4));
                        mShowcaseView.setContentText(getString(R.string.read_diary_showcase_message_4));
                        break;
                    case 3:
                        mShowcaseView.setButtonPosition(centerParams);
                        mShowcaseView.setTarget(new ViewTarget(R.id.timeline, DiaryMainActivity.this));
                        mShowcaseView.setContentTitle(getString(R.string.read_diary_showcase_title_5));
                        mShowcaseView.setContentText(getString(R.string.read_diary_showcase_message_5));
                        break;
                    case 4:
                        mShowcaseView.setButtonPosition(centerParams);
                        mShowcaseView.setTarget(new ViewTarget(R.id.microphone, DiaryMainActivity.this));
                        mShowcaseView.setContentTitle(getString(R.string.read_diary_showcase_title_3));
                        mShowcaseView.setContentText(getString(R.string.read_diary_showcase_message_3));
                        break;
                    case 5:
                        mShowcaseView.hide();
                        break;
                }
                mShowcaseIndex++;
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

    private void bindEvent() {

        mQuery.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                refreshList(String.valueOf(charSequence));
            }

            public void afterTextChanged(Editable editable) {}
        });

        mDiaryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                DiaryDto diaryDto = (DiaryDto)adapterView.getAdapter().getItem(i);
                Intent detailIntent = new Intent(DiaryMainActivity.this, DiaryReadActivity.class);
                detailIntent.putExtra(Constants.DIARY_SEQUENCE, diaryDto.getSequence());
                detailIntent.putExtra(Constants.DIARY_SEARCH_QUERY, mDiaryMainItemAdapter.getCurrentQuery());
                TransitionHelper.startActivityWithTransition(DiaryMainActivity.this, detailIntent);
            }
        });
    }

    private void showSpeechDialog() {
        try {
            startActivityForResult(mRecognizerIntent, Constants.REQUEST_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException e) {
            DialogUtils.showAlertDialog(this, getString(R.string.recognizer_intent_not_found_message), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
        }
    }

    public void refreshList() {
        String query = "";
        if (StringUtils.isNotEmpty(mQuery.getText())) query = String.valueOf(mQuery.getText());
        refreshList(query);
    }

    public void refreshList(String query) {
        mDiaryList.clear();
        mDiaryList.addAll(EasyDiaryDbHelper.readDiary(query, CommonUtils.loadBooleanPreference(this, Constants.DIARY_SEARCH_QUERY_CASE_SENSITIVE)));
        mDiaryMainItemAdapter.setCurrentQuery(query);
        mDiaryMainItemAdapter.notifyDataSetChanged();
    }

    private void initSampleData() {
        EasyDiaryDbHelper.insertDiary(new DiaryDto(
                -1,
                this.mCurrentTimeMillis - 395000000L, getString(R.string.sample_diary_title_1), getString(R.string.sample_diary_1),
                1
        ));
        EasyDiaryDbHelper.insertDiary(new DiaryDto(
                -1,
                this.mCurrentTimeMillis - 263000000L, getString(R.string.sample_diary_title_2), getString(R.string.sample_diary_2),
                2
        ));
        EasyDiaryDbHelper.insertDiary(new DiaryDto(
                -1,
                this.mCurrentTimeMillis - 132000000L, getString(R.string.sample_diary_title_3), getString(R.string.sample_diary_3),
                3
        ));
        EasyDiaryDbHelper.insertDiary(new DiaryDto(
                -1,
                this.mCurrentTimeMillis - 4000000L, getString(R.string.sample_diary_title_4), getString(R.string.sample_diary_4),
                4
        ));
    }
}
