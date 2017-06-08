package me.blog.korn123.easydiary.diary;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
import me.blog.korn123.easydiary.googledrive.GoogleDriveDownloader;
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
    private RealmList<PhotoUriDto> mPhotoUris;

    @BindView(R.id.contents)
    EditText mContents;

    @BindView(R.id.title)
    EditText mTitle;

    @BindView(R.id.saveContents)
    ImageView mSaveContents;

    @BindView(R.id.weatherSpinner)
    Spinner mWeatherSpinner;

    @BindView(R.id.photoContainer)
    ViewGroup mPhotoContainer;

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
        String[]  weatherArr = getResources().getStringArray(R.array.weather_item_array);
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

    @OnClick({R.id.speechButton, R.id.zoomIn, R.id.zoomOut, R.id.saveContents, R.id.photoView})
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
                    diaryDto.setPhotoUris(mPhotoUris);
                    DiaryDao.createDiary(diaryDto);
                    CommonUtils.saveIntPreference(CreateDiaryActivity.this, Constants.PREVIOUS_ACTIVITY, Constants.PREVIOUS_ACTIVITY_CREATE);
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
        }
    }

    private void callImagePicker() {
        Intent pickImageIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                pickIntent.setType("image/*");
        startActivityForResult(pickImageIntent, Constants.REQUEST_CODE_IMAGE_PICKER);
    }

    private void showSpeechDialog() {
        startActivityForResult(this.mRecognizerIntent, REQUEST_CODE_SPEECH_INPUT);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        CommonUtils.saveLongPreference(CreateDiaryActivity.this, Constants.PAUSE_MILLIS, System.currentTimeMillis()); // clear screen lock policy
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
                break;
            case Constants.REQUEST_CODE_IMAGE_PICKER:
//                String path = CommonUtils.uriToPath(getContentResolver(), data.getData());
                try {
                    if (data.getData() != null) {
                        if (mPhotoUris == null) mPhotoUris = new RealmList<>();
                        mPhotoUris.add(new PhotoUriDto(data.getData().toString()));
                        Bitmap bitmap = BitmapUtils.decodeUri(this, data.getData(), CommonUtils.dpToPixel(this, 70, 1), CommonUtils.dpToPixel(this, 60, 1), CommonUtils.dpToPixel(this, 40, 1));
                        ImageView imageView = new ImageView(this);
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(CommonUtils.dpToPixel(this, 70, 1), CommonUtils.dpToPixel(this, 50, 1));
                        layoutParams.setMargins(0, 0, CommonUtils.dpToPixel(this, 3, 1), 0);
                        imageView.setLayoutParams(layoutParams);
                        imageView.setBackgroundResource(R.drawable.bg_card_01);
                        imageView.setImageBitmap(bitmap);
                        imageView.setScaleType(ImageView.ScaleType.CENTER);
//                    imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        final int currentIndex = mPhotoUris.size() - 1;
                        imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                DialogUtils.showAlertDialog(
                                        CreateDiaryActivity.this,
                                        getString(R.string.delete_photo_confirm_message),
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                mPhotoUris.remove(currentIndex);
                                                mPhotoContainer.removeViewAt(currentIndex);
                                            }
                                        },
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {}
                                        }
                                );
                            }
                        });
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
            case Constants.REQUEST_CODE_EXTERNAL_STORAGE:
                callImagePicker();
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
