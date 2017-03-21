package me.blog.korn123.easydiary.diary;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
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

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.blog.korn123.commons.utils.CommonUtils;
import me.blog.korn123.commons.utils.DateUtils;
import me.blog.korn123.commons.utils.DialogUtils;
import me.blog.korn123.commons.utils.FontUtils;
import me.blog.korn123.easydiary.R;
import me.blog.korn123.easydiary.setting.SettingsActivity;

/**
 * Created by CHO HANJOONG on 2017-03-16.
 */

public class ReadDiaryActivity extends AppCompatActivity {

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
                detailIntent.putExtra("date", DateUtils.getCurrentDateTime(diaryDto.getCurrentTimeMillis()));
                detailIntent.putExtra("current_time_millis", diaryDto.getCurrentTimeMillis());
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
        startActivityForResult(mRecognizerIntent, REQUEST_CODE_SPEECH_INPUT);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_SPEECH_INPUT:
                if ((resultCode == RESULT_OK) && (data != null)) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    mQuery.setText(result.get(0));
                }
                break;
        }
    }

    protected void onResume() {
        super.onResume();
        refreshList();
        mDiaryListView.smoothScrollToPosition(0);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_settings:
//                DialogUtils.makeSnackBar(findViewById(android.R.id.content), getString(R.string.notice_message));
                Intent settingIntent = new Intent(ReadDiaryActivity.this, SettingsActivity.class);
                startActivity(settingIntent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
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
        DiaryDao.createDiary(this.mCurrentTimeMillis - 5000000L, "조심하기 보다는 위험해져라.", "지나온 인생을 돌아보면 위험에 빠진 경우보다는\n" +
                "너무 조심한 탓에\n" +
                "손해를 자초해 왔음을 분명 알게 될 것이다.\n" +
                "위험해져라.\n" +
                "조심하기 보다는 위험해져라.\n" +
                "- 그랜트 가돈, ‘10배의 법칙’에서");

        DiaryDao.createDiary(this.mCurrentTimeMillis - 4000000L, "풍요는 안락을, 제약은 창의를 부른다.", "제약이 창의성을 가두는 게 아니라,\n" +
                "제약을 극복하기 위해 창의성이 발휘된다.\n" +
                "모든 위대한 작품이나 창의적인 솔루션은 시간적인 제약,\n" +
                "물리적인 제약, 자원의 제약들이 엄청 많았던 것들이었다.\n" +
                "뭐가 됐든 어렵고 제약된 환경에서 창의성이 태어나곤 한다.\n" +
                "- 김봉진 대표, ‘배민다움’에서");

        DiaryDao.createDiary(this.mCurrentTimeMillis - 3000000L, "말할 때는 아는 것만 반복한다.", "당신이 말할 때는 아는 것만 반복한다.\n" +
                "하지만 들으면 새로운 것을 배우게 된다.\n" +
                "(When you talk, you are only repeating what you already know.\n" +
                "But if you listen, you may learn something new.)\n" +
                "- 달라이 라마");

        DiaryDao.createDiary(this.mCurrentTimeMillis - 2000000L, "나쁜 소식을 많이 들을수록 기뻐해야 한다.", "여러분이 지도자로서 가장 들을 필요가 있는 것이\n" +
                "바로 나쁜 소식이다.\n" +
                "좋은 소식은 내일도 좋은 것이지만\n" +
                "나쁜 소식은 내일이면 더 나빠질 것이다.\n" +
                "바로 이것이 비록 사실이 가슴 아프더라도\n" +
                "언제나 까다로운 질문을 하고 사실을 말하는 것이 안전한 이유다.\n" +
                "- 에릭 슈미트 구글 회장, ‘구글은 어떻게 일하는가?’에서");
    }

}
