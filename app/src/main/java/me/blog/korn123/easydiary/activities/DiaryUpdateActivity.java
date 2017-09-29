package me.blog.korn123.easydiary.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TimePicker;

import org.apache.commons.lang3.StringUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.RealmList;
import me.blog.korn123.commons.constants.Constants;
import me.blog.korn123.commons.utils.BitmapUtils;
import me.blog.korn123.commons.utils.CommonUtils;
import me.blog.korn123.commons.utils.DateUtils;
import me.blog.korn123.commons.utils.DialogUtils;
import me.blog.korn123.commons.utils.FontUtils;
import me.blog.korn123.commons.utils.PermissionUtils;
import me.blog.korn123.easydiary.R;
import me.blog.korn123.easydiary.adapters.DiaryWeatherItemAdapter;
import me.blog.korn123.easydiary.models.PhotoUriDto;
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper;
import me.blog.korn123.easydiary.models.DiaryDto;

/**
 * Created by CHO HANJOONG on 2017-03-16.
 */

public class DiaryUpdateActivity extends EasyDiaryActivity {

    private final int REQUEST_CODE_SPEECH_INPUT = 100;
    private Intent mRecognizerIntent;
    private long mCurrentTimeMillis;
    private int mSequence;
    private int mWeather;
    private int mCurrentCursor = 1;
    private RealmList<PhotoUriDto> mPhotoUris;
    private List<Integer> mRemoveIndexes = new ArrayList<>();

    @BindView(R.id.contents)
    EditText mContents;

    @BindView(R.id.title)
    EditText mTitle;

    @BindView(R.id.saveContents)
    ImageView mSaveContents;

    @BindView(R.id.toggleSwitch)
    Switch mToggleSwitch;

    @BindView(R.id.toggleMicOn)
    ImageView mToggleMicOn;

    @BindView(R.id.toggleMicOff)
    ImageView mToggleMicOff;

    @BindView(R.id.weatherSpinner)
    Spinner mWeatherSpinner;

    @BindView(R.id.photoContainer)
    ViewGroup mPhotoContainer;

//    @BindView(R.id.subToolbar)
//    ViewGroup mSubToolbar;

    @BindView(R.id.photoContainerScrollView)
    HorizontalScrollView mHorizontalScrollView;

    @BindView(R.id.speechButton)
    FloatingActionButton mSpeechButton;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_update);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.update_diary_title));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        mRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        FontUtils.setToolbarTypeface(toolbar, Typeface.DEFAULT);
        FontUtils.setTypeface(this, getAssets(), this.mContents);
        FontUtils.setTypeface(this, getAssets(), this.mTitle);

        bindView();
        bindEvent();
        initFontStyle();
        initData();
        initDateTime();
        setDateTime();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    public void initSpinner() {
        String[]  weatherArr = getResources().getStringArray(R.array.weather_item_array);
        ArrayAdapter arrayAdapter = new DiaryWeatherItemAdapter(DiaryUpdateActivity.this, R.layout.item_weather, Arrays.asList(weatherArr));
        mWeatherSpinner.setAdapter(arrayAdapter);
        mWeatherSpinner.setSelection(mWeather);
    }

    public void initData() {
        Intent intent = getIntent();
        mSequence = intent.getIntExtra("sequence", 0);
        DiaryDto diaryDto = EasyDiaryDbHelper.readDiaryBy(mSequence);
        mWeather = diaryDto.getWeather();

        mTitle.setText(diaryDto.getTitle());
        getSupportActionBar().setSubtitle(DateUtils.getFullPatternDateWithTime(diaryDto.getCurrentTimeMillis()));
        mContents.setText(diaryDto.getContents());
        mCurrentTimeMillis = diaryDto.getCurrentTimeMillis();
        mContents.requestFocus();

        // TODO fixme elegance
        mPhotoUris = new RealmList<>();
        mPhotoUris.addAll(diaryDto.getPhotoUris());
        if (mPhotoUris != null && mPhotoUris.size() > 0) {
            int currentIndex = 0;
            for (PhotoUriDto dto : mPhotoUris) {
                Uri uri = Uri.parse(dto.getPhotoUri());
                Bitmap bitmap = null;
                try {
                    bitmap = BitmapUtils.decodeUri(this, uri, CommonUtils.dpToPixel(this, 70, 1), CommonUtils.dpToPixel(this, 60, 1), CommonUtils.dpToPixel(this, 40, 1));

                } catch (FileNotFoundException e) {
                    bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.question_mark_4);
                    e.printStackTrace();
                }

                ImageView imageView = new ImageView(this);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(CommonUtils.dpToPixel(this, 70, 1), CommonUtils.dpToPixel(this, 50, 1));
                layoutParams.setMargins(0, 0, CommonUtils.dpToPixel(this, 3, 1), 0);
                imageView.setLayoutParams(layoutParams);
                imageView.setBackgroundResource(R.drawable.bg_card_01);
                imageView.setImageBitmap(bitmap);
                imageView.setScaleType(ImageView.ScaleType.CENTER);
                imageView.setOnClickListener(new PhotoClickListener(currentIndex++));
                mPhotoContainer.addView(imageView, mPhotoContainer.getChildCount() - 1);
            }
        }
        initSpinner();
    }

    @Override
    public void onBackPressed() {
        DialogUtils.showAlertDialog(DiaryUpdateActivity.this, getString(R.string.back_pressed_confirm),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                },
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }
        );
    }

    public void initFontStyle() {
        float fontSize = CommonUtils.loadFloatPreference(DiaryUpdateActivity.this, "font_size", 0);
        if (fontSize > 0) {
            mContents.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize);
        }

        FontUtils.setTypeface(this, getAssets(), mTitle);
        FontUtils.setTypeface(this, getAssets(), mContents);
    }

    private void bindView() {
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

    @OnClick({R.id.speechButton, R.id.zoomIn, R.id.zoomOut, R.id.saveContents, R.id.photoView, R.id.datePicker, R.id.timePicker})
    public void onClick(View view) {
        float fontSize = mContents.getTextSize();

        switch(view.getId()) {
            case R.id.speechButton:
                showSpeechDialog();
                break;
            case R.id.zoomIn:
                CommonUtils.saveFloatPreference(DiaryUpdateActivity.this, Constants.SETTING_FONT_SIZE, fontSize + 5);
                setDiaryFontSize();
                break;
            case R.id.zoomOut:
                CommonUtils.saveFloatPreference(DiaryUpdateActivity.this, Constants.SETTING_FONT_SIZE, fontSize - 5);
                setDiaryFontSize();
                break;
            case R.id.saveContents:
                if (StringUtils.isEmpty(mContents.getText())) {
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
                    applyRemoveIndex();
                    diaryDto.setPhotoUris(mPhotoUris);
                    EasyDiaryDbHelper.updateDiary(diaryDto);
                    finish();
                }
                break;
            case R.id.photoView:
                if (PermissionUtils.checkPermission(this, Constants.EXTERNAL_STORAGE_PERMISSIONS)) {
                    // API Level 22 이하이거나 API Level 23 이상이면서 권한취득 한경우
                    callImagePicker();
                } else {
                    // API Level 23 이상이면서 권한취득 안한경우
                    PermissionUtils.confirmPermission(this, this, Constants.EXTERNAL_STORAGE_PERMISSIONS, Constants.REQUEST_CODE_EXTERNAL_STORAGE);
                }
                break;
            case R.id.datePicker:
                if (mDatePickerDialog == null) {
                    mDatePickerDialog = new DatePickerDialog(this, mStartDateListener, mYear, mMonth - 1, mDayOfMonth);
                }
                mDatePickerDialog.show();
                break;
            case R.id.timePicker:
                if (mTimePickerDialog == null) {
                    mTimePickerDialog = new TimePickerDialog(this, mTimeSetListener, mHourOfDay, mMinute, false);
                }
                mTimePickerDialog.show();
                break;
        }
    }

    private DatePickerDialog mDatePickerDialog;
    private TimePickerDialog mTimePickerDialog;
    private int mYear;
    private int mMonth;
    private int mDayOfMonth;
    private int mHourOfDay;
    private int mMinute;

    DatePickerDialog.OnDateSetListener mStartDateListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            mYear = year;
            mMonth = month + 1;
            mDayOfMonth = dayOfMonth;
            setDateTime();
        }
    };

    TimePickerDialog.OnTimeSetListener mTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            mHourOfDay = hourOfDay;
            mMinute = minute;
            setDateTime();
        }
    };

    private void initDateTime() {
        mYear = Integer.valueOf(DateUtils.timeMillisToDateTime(mCurrentTimeMillis, DateUtils.YEAR_PATTERN));
        mMonth = Integer.valueOf(DateUtils.timeMillisToDateTime(mCurrentTimeMillis, DateUtils.MONTH_PATTERN));
        mDayOfMonth = Integer.valueOf(DateUtils.timeMillisToDateTime(mCurrentTimeMillis, DateUtils.DAY_PATTERN));
        mHourOfDay = Integer.valueOf(DateUtils.timeMillisToDateTime(mCurrentTimeMillis,"HH"));
        mMinute = Integer.valueOf(DateUtils.timeMillisToDateTime(mCurrentTimeMillis,"mm"));
    }

    private void setDateTime() {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmm");
            String dateTimeString = String.format(
                    "%d%s%s%s%s",
                    mYear,
                    StringUtils.leftPad(String.valueOf(mMonth), 2, "0"),
                    StringUtils.leftPad(String.valueOf(mDayOfMonth), 2, "0"),
                    StringUtils.leftPad(String.valueOf(mHourOfDay), 2, "0"),
                    StringUtils.leftPad(String.valueOf(mMinute), 2, "0")
            );
            Date parsedDate = format.parse(dateTimeString);
            mCurrentTimeMillis = parsedDate.getTime();
            getSupportActionBar().setSubtitle(DateUtils.getFullPatternDateWithTime(mCurrentTimeMillis));
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void applyRemoveIndex() {
        Collections.sort(mRemoveIndexes, Collections.<Integer>reverseOrder());
        for (int index : mRemoveIndexes) {
            mPhotoUris.remove(index);
        }
        mRemoveIndexes.clear();
    }

    private void callImagePicker() {
        Intent pickImageIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        try {
            startActivityForResult(pickImageIntent, Constants.REQUEST_CODE_IMAGE_PICKER);
        } catch (ActivityNotFoundException e) {
            DialogUtils.showAlertDialog(this, getString(R.string.gallery_intent_not_found_message), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
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
        CommonUtils.saveLongPreference(DiaryUpdateActivity.this, Constants.SETTING_PAUSE_MILLIS, System.currentTimeMillis()); // clear screen lock policy
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
                break;
            case Constants.REQUEST_CODE_IMAGE_PICKER:
                try {
                    if (resultCode == RESULT_OK && (data != null)) {
                        if (mPhotoUris == null) mPhotoUris =new RealmList<PhotoUriDto>();
                        mPhotoUris.add(new PhotoUriDto(data.getData().toString()));
                        Bitmap bitmap = BitmapUtils.decodeUri(this, data.getData(), CommonUtils.dpToPixel(this, 70, 1), CommonUtils.dpToPixel(this, 60, 1), CommonUtils.dpToPixel(this, 40, 1));
                        ImageView imageView = new ImageView(this);
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(CommonUtils.dpToPixel(this, 70, 1), CommonUtils.dpToPixel(this, 50, 1));
                        layoutParams.setMargins(0, 0, CommonUtils.dpToPixel(this, 3, 1), 0);
                        imageView.setLayoutParams(layoutParams);
                        imageView.setBackgroundResource(R.drawable.bg_card_01);
                        imageView.setImageBitmap(bitmap);
                        imageView.setScaleType(ImageView.ScaleType.CENTER);
                        final int currentIndex = mPhotoUris.size() - 1;
                        imageView.setOnClickListener(new PhotoClickListener(currentIndex));
                        mPhotoContainer.addView(imageView, mPhotoContainer.getChildCount() - 1);
                        mPhotoContainer.postDelayed(new Runnable() {
                            public void run() {
                                ((HorizontalScrollView)mPhotoContainer.getParent()).fullScroll(HorizontalScrollView.FOCUS_RIGHT);
                            }
                        }, 100L);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Constants.REQUEST_CODE_EXTERNAL_STORAGE:
                if (PermissionUtils.checkPermission(this, Constants.EXTERNAL_STORAGE_PERMISSIONS)) {
                    // 권한이 있는경우
                    callImagePicker();
                } else {
                    // 권한이 없는경우
                    DialogUtils.makeSnackBar(findViewById(android.R.id.content), getString(R.string.guide_message_3));
                }
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
                Intent settingIntent = new Intent(DiaryUpdateActivity.this, SettingsActivity.class);
                startActivity(settingIntent);
                break;
//            case R.id.toolbarToggle:
//                if (mSubToolbar.getVisibility() == View.GONE) {
//                    mSubToolbar.setVisibility(View.VISIBLE);
//                } else if (mSubToolbar.getVisibility() == View.VISIBLE) {
//                    mSubToolbar.setVisibility(View.GONE);
//                }
//                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        FontUtils.setTypeface(this, getAssets(), this.mContents);
        FontUtils.setTypeface(this, getAssets(), this.mTitle);
        setDiaryFontSize();
    }

    private void setDiaryFontSize() {
        float fontSize = CommonUtils.loadFloatPreference(this, "font_size", 0);
        if (fontSize > 0) {
            mTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize);
            mContents.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize);
            mWeather = mWeatherSpinner.getSelectedItemPosition();
            initSpinner();
        }
    }

    class PhotoClickListener implements View.OnClickListener {

        int index;
        PhotoClickListener(int index) {
            this.index = index;
        }

        @Override
        public void onClick(View v) {
            final View targetView = v;
            final int targetIndex = index;
            DialogUtils.showAlertDialog(
                    DiaryUpdateActivity.this,
                    getString(R.string.delete_photo_confirm_message),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mRemoveIndexes.add(targetIndex);
                            mPhotoContainer.removeView(targetView);
                        }
                    },
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {}
                    }
            );
        }
    }

}
