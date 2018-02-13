package me.blog.korn123.easydiary.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TimePicker;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.simplemobiletools.commons.helpers.BaseConfig;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
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
import me.blog.korn123.commons.utils.EasyDiaryUtils;
import me.blog.korn123.commons.utils.FontUtils;
import me.blog.korn123.commons.utils.PermissionUtils;
import me.blog.korn123.easydiary.R;
import me.blog.korn123.easydiary.adapters.DiaryWeatherItemAdapter;
import me.blog.korn123.easydiary.extensions.ContextKt;
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper;
import me.blog.korn123.easydiary.models.DiaryDto;
import me.blog.korn123.easydiary.models.PhotoUriDto;

import static me.blog.korn123.commons.constants.Constants.THUMBNAIL_BACKGROUND_ALPHA;

/**
 * Created by CHO HANJOONG on 2017-03-16.
 */

public class DiaryInsertActivity extends EasyDiaryActivity {

    private Intent mRecognizerIntent;
    private long mCurrentTimeMillis;
    private int mCurrentCursor = 0;
    private RealmList<PhotoUriDto> mPhotoUris;
    private List<Integer> mRemoveIndexes = new ArrayList<>();
    private int mShowcaseIndex = 2;
    private ShowcaseView mShowcaseView;
    private AlertDialog mAlertDialog;
    private int mPrimaryColor = 0;

    @BindView(R.id.contents)
    EditText mContents;

    @BindView(R.id.title)
    EditText mTitle;

    @BindView(R.id.weatherSpinner)
    Spinner mWeatherSpinner;

    @BindView(R.id.photoContainer)
    ViewGroup mPhotoContainer;

    @BindView(R.id.saveContents)
    ImageView mSaveContents;

    @BindView(R.id.datePicker)
    ImageView mDatePicker;

    @BindView(R.id.timePicker)
    ImageView mTimePicker;

    @BindView(R.id.photoView)
    ImageView mPhotoView;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_insert);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.create_diary_title));
        setDateTime();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        mRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        bindEvent();
        initSpinner();
        initShowcase();
    }

    @Override
    public void onBackPressed() {
        DialogUtils.showAlertDialog(DiaryInsertActivity.this, getString(R.string.back_pressed_confirm),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        DiaryInsertActivity.super.onBackPressed();
                    }
                },
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }
        );
    }

    @OnClick({R.id.saveContents, R.id.photoView, R.id.datePicker, R.id.timePicker, R.id.secondsPicker, R.id.microphone})
    public void onClick(View view) {
        float fontSize = mContents.getTextSize();

        switch(view.getId()) {
            case R.id.saveContents:
                if (StringUtils.isEmpty(mContents.getText())) {
                    mContents.requestFocus();
                    DialogUtils.makeSnackBar(findViewById(android.R.id.content), getString(R.string.request_content_message));
                } else {
                    DiaryDto diaryDto = new DiaryDto(
                            -1,
                            mCurrentTimeMillis,
                            String.valueOf(DiaryInsertActivity.this.mTitle.getText()),
                            String.valueOf(DiaryInsertActivity.this.mContents.getText()),
                            mWeatherSpinner.getSelectedItemPosition()
                    );
                    applyRemoveIndex();
                    diaryDto.setPhotoUris(mPhotoUris);
                    EasyDiaryDbHelper.insertDiary(diaryDto);
                    CommonUtils.saveIntPreference(DiaryInsertActivity.this, Constants.PREVIOUS_ACTIVITY, Constants.PREVIOUS_ACTIVITY_CREATE);
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
            case R.id.secondsPicker:
                AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        HashMap<String, String> itemMap = (HashMap<String, String>) parent.getAdapter().getItem(position);
                        mSecond = Integer.valueOf(itemMap.get("value"));
                        setDateTime();
                        mAlertDialog.cancel();
                    }
                };
                AlertDialog.Builder builder = EasyDiaryUtils.createSecondsPickerBuilder(DiaryInsertActivity.this, itemClickListener, mSecond);
                mAlertDialog = builder.create();
                mAlertDialog.show();
                break;
            case R.id.microphone:
                showSpeechDialog();
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

    @Override
    protected void onResume() {
        super.onResume();

        // set bottom thumbnail container
        mPrimaryColor = new BaseConfig(DiaryInsertActivity.this).getPrimaryColor();
        GradientDrawable drawable = (GradientDrawable) mPhotoView.getBackground();
        drawable.setColor(ColorUtils.setAlphaComponent(mPrimaryColor, THUMBNAIL_BACKGROUND_ALPHA));
        setFontsStyle();
    }

    private void setFontsStyle() {
        FontUtils.setFontsTypeface(getApplicationContext(), getAssets(), null, (ViewGroup) findViewById(android.R.id.content));
    }

    private void setFontsSize() {
        ContextKt.initTextSize(this, getMRootView(), this);
    }

    public void initSpinner() {
        String[]  weatherArr = getResources().getStringArray(R.array.weather_item_array);
        ArrayAdapter arrayAdapter = new DiaryWeatherItemAdapter(DiaryInsertActivity.this, R.layout.item_weather, Arrays.asList(weatherArr));
        mWeatherSpinner.setAdapter(arrayAdapter);
    }

    private void initShowcase() {
        int margin = ((Number) (getResources().getDisplayMetrics().density * 12)).intValue();

        final RelativeLayout.LayoutParams centerParams =
                new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        centerParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        centerParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        centerParams.setMargins(0, 0, 0, margin);

        View.OnClickListener showcaseViewOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (mShowcaseIndex) {
                    case 2:
                        mShowcaseView.setButtonPosition(centerParams);
                        mShowcaseView.setShowcase(new ViewTarget(mTitle), true);
                        mShowcaseView.setContentTitle(getString(R.string.create_diary_showcase_title_2));
                        mShowcaseView.setContentText(getString(R.string.create_diary_showcase_message_2));
                        break;
                    case 3:
                        mShowcaseView.setButtonPosition(centerParams);
                        mShowcaseView.setShowcase(new ViewTarget(mContents), true);
                        mShowcaseView.setContentTitle(getString(R.string.create_diary_showcase_title_3));
                        mShowcaseView.setContentText(getString(R.string.create_diary_showcase_message_3));
                        break;
                    case 4:
                        mShowcaseView.setButtonPosition(centerParams);
                        mShowcaseView.setShowcase(new ViewTarget(mPhotoView), true);
                        mShowcaseView.setContentTitle(getString(R.string.create_diary_showcase_title_4));
                        mShowcaseView.setContentText(getString(R.string.create_diary_showcase_message_4));
                        break;
                    
                    case 5:
                        mShowcaseView.setButtonPosition(centerParams);
                        mShowcaseView.setShowcase(new ViewTarget(mDatePicker), true);
                        mShowcaseView.setContentTitle(getString(R.string.create_diary_showcase_title_7));
                        mShowcaseView.setContentText(getString(R.string.create_diary_showcase_message_7));
                        break;
                    case 6:
                        mShowcaseView.setButtonPosition(centerParams);
                        mShowcaseView.setShowcase(new ViewTarget(mTimePicker), true);
                        mShowcaseView.setContentTitle(getString(R.string.create_diary_showcase_title_8));
                        mShowcaseView.setContentText(getString(R.string.create_diary_showcase_message_8));
                        break;
                    case 7:
                        mShowcaseView.setButtonPosition(centerParams);
                        mShowcaseView.setShowcase(new ViewTarget(mSaveContents), true);
                        mShowcaseView.setContentTitle(getString(R.string.create_diary_showcase_title_9));
                        mShowcaseView.setContentText(getString(R.string.create_diary_showcase_message_9));
                        mShowcaseView.setButtonText(getString(R.string.create_diary_showcase_button_2));
                        break;
                    case 8:
                        mShowcaseView.hide();
                        break;
                }
                mShowcaseIndex++;
            }
        };

        mShowcaseView = new ShowcaseView.Builder(this)
                .withMaterialShowcase()
                .setTarget(new ViewTarget(mWeatherSpinner))
                .setContentTitle(getString(R.string.create_diary_showcase_title_1))
                .setContentText(getString(R.string.create_diary_showcase_message_1))
                .setStyle(R.style.ShowcaseTheme)
                .singleShot(Constants.SHOWCASE_SINGLE_SHOT_CREATE_DIARY_NUMBER)
                .setOnClickListener(showcaseViewOnClickListener)
                .build();
        mShowcaseView.setButtonText(getString(R.string.create_diary_showcase_button_1));
        mShowcaseView.setButtonPosition(centerParams);
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

    private DatePickerDialog mDatePickerDialog;
    private TimePickerDialog mTimePickerDialog;
    private int mYear = Integer.valueOf(DateUtils.getCurrentDateAsString(DateUtils.YEAR_PATTERN));
    private int mMonth = Integer.valueOf(DateUtils.getCurrentDateAsString(DateUtils.MONTH_PATTERN));
    private int mDayOfMonth = Integer.valueOf(DateUtils.getCurrentDateAsString(DateUtils.DAY_PATTERN));
    private int mHourOfDay = Integer.valueOf(DateUtils.getCurrentDateAsString("HH"));
    private int mMinute = Integer.valueOf(DateUtils.getCurrentDateAsString("mm"));
    private int mSecond = Integer.valueOf(DateUtils.getCurrentDateAsString("ss"));

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

    private void setDateTime() {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
            String dateTimeString = String.format(
                    "%d%s%s%s%s%s",
                    mYear,
                    StringUtils.leftPad(String.valueOf(mMonth), 2, "0"),
                    StringUtils.leftPad(String.valueOf(mDayOfMonth), 2, "0"),
                    StringUtils.leftPad(String.valueOf(mHourOfDay), 2, "0"),
                    StringUtils.leftPad(String.valueOf(mMinute), 2, "0"),
                    StringUtils.leftPad(String.valueOf(mSecond), 2, "0")
            );
            Date parsedDate = format.parse(dateTimeString);
            mCurrentTimeMillis = parsedDate.getTime();
            getSupportActionBar().setSubtitle(DateUtils.getFullPatternDateWithTimeAndSeconds(mCurrentTimeMillis));
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
//                pickIntent.setType("image/*");
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
            startActivityForResult(mRecognizerIntent, Constants.REQUEST_CODE_SPEECH_INPUT);
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
        CommonUtils.saveLongPreference(DiaryInsertActivity.this, Constants.SETTING_PAUSE_MILLIS, System.currentTimeMillis()); // clear screen lock policy
        switch (requestCode) {
            case Constants.REQUEST_CODE_SPEECH_INPUT:
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
//                String path = CommonUtils.uriToPath(getContentResolver(), data.getData());
                try {
                    if (resultCode == RESULT_OK && (data != null)) {
                        if (mPhotoUris == null) mPhotoUris = new RealmList<>();
                        mPhotoUris.add(new PhotoUriDto(data.getData().toString()));
                        Bitmap bitmap = BitmapUtils.decodeUri(this, data.getData(), CommonUtils.dpToPixel(this, 70, 1), CommonUtils.dpToPixel(this, 65, 1), CommonUtils.dpToPixel(this, 45, 1));
                        ImageView imageView = new ImageView(this);
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(CommonUtils.dpToPixel(this, 70, 1), CommonUtils.dpToPixel(this, 50, 1));
                        layoutParams.setMargins(0, 0, CommonUtils.dpToPixel(this, 3, 1), 0);
                        imageView.setLayoutParams(layoutParams);
                        Drawable drawable = getResources().getDrawable(R.drawable.bg_card_thumbnail);
                        GradientDrawable gradient = (GradientDrawable) drawable;
                        gradient.setColor(ColorUtils.setAlphaComponent(mPrimaryColor, THUMBNAIL_BACKGROUND_ALPHA));
                        imageView.setBackground(gradient);
                        imageView.setImageBitmap(bitmap);
                        imageView.setScaleType(ImageView.ScaleType.CENTER);
//                    imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
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
            default:
                break;
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
//                finish();
//                this.overridePendingTransition(R.anim.anim_left_to_center, R.anim.anim_center_to_right);
                super.onBackPressed();
                break;
//            case R.id.action_settings:
//                Intent settingIntent = new Intent(DiaryInsertActivity.this, SettingsActivity.class);
//                startActivity(settingIntent);
//                break;
        }
        return true;
//        return super.onOptionsItemSelected(item);
    }

//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.diary_common, menu);
//        return true;
//    }

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
                    DiaryInsertActivity.this,
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
