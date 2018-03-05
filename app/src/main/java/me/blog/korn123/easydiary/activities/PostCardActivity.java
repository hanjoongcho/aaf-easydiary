package me.blog.korn123.easydiary.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.blog.korn123.commons.constants.Constants;
import me.blog.korn123.commons.constants.Path;
import me.blog.korn123.commons.utils.BitmapUtils;
import me.blog.korn123.commons.utils.CommonUtils;
import me.blog.korn123.commons.utils.DateUtils;
import me.blog.korn123.commons.utils.DialogUtils;
import me.blog.korn123.commons.utils.EasyDiaryUtils;
import me.blog.korn123.commons.utils.FontUtils;
import me.blog.korn123.commons.utils.PermissionUtils;
import me.blog.korn123.easydiary.R;
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper;
import me.blog.korn123.easydiary.models.DiaryDto;

/**
 * Created by hanjoong on 2017-07-01.
 */

public class PostCardActivity extends EasyDiaryActivity {

    private String mSavedDiaryCardPath;
    private int mSequence;
    private int mBgColor = 0xffffffff;
    private int mTextColor = 0xff4A4A4C;

    @BindView(R.id.contents)
    TextView mContents;

    @BindView(R.id.title)
    TextView mTitle;

    @BindView(R.id.date)
    TextView mDate;

    @BindView(R.id.weather)
    ImageView mWeather;

    @BindView(R.id.contentsContainer)
    ViewGroup mContentsContainer;

    @BindView(R.id.postContainer)
    ViewGroup mPostContainer;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_card);
        ButterKnife.bind(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_cross);

        Intent intent = getIntent();
        mSequence = intent.getIntExtra(Constants.DIARY_SEQUENCE, 0);
        DiaryDto diaryDto = EasyDiaryDbHelper.readDiaryBy(mSequence);

        EasyDiaryUtils.initWeatherView(mWeather, diaryDto.getWeather());
        mTitle.setText(diaryDto.getTitle());
        mContents.setText(diaryDto.getContents());
        mDate.setText(DateUtils.INSTANCE.getFullPatternDateWithTime(diaryDto.getCurrentTimeMillis()));
        initShowcase();
    }
    
    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            System.out.println(1);
        }
    };

    private int showcaseIndex = 1;
    ShowcaseView mShowcaseView;

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
                switch (showcaseIndex) {
                    case 1:
                        mShowcaseView.setButtonPosition(centerParams);
                        mShowcaseView.setTarget(new ViewTarget(R.id.textColor, PostCardActivity.this));
                        mShowcaseView.setContentTitle(getString(R.string.post_card_showcase_title_1));
                        mShowcaseView.setContentText(getString(R.string.post_card_showcase_message_1));
                        break;
                    case 2:
                        mShowcaseView.setButtonPosition(centerParams);
                        mShowcaseView.setTarget(new ViewTarget(R.id.bgColor, PostCardActivity.this));
                        mShowcaseView.setContentTitle(getString(R.string.post_card_showcase_title_2));
                        mShowcaseView.setContentText(getString(R.string.post_card_showcase_message_2));
                        break;
                    case 3:
                        mShowcaseView.setButtonPosition(centerParams);
                        mShowcaseView.setTarget(new ViewTarget(R.id.save, PostCardActivity.this));
                        mShowcaseView.setContentTitle(getString(R.string.post_card_showcase_title_3));
                        mShowcaseView.setContentText(getString(R.string.post_card_showcase_message_3));
                        break;
                    case 4:
                        mShowcaseView.setButtonPosition(centerParams);
                        mShowcaseView.setTarget(new ViewTarget(R.id.share, PostCardActivity.this));
                        mShowcaseView.setContentTitle(getString(R.string.post_card_showcase_title_4));
                        mShowcaseView.setContentText(getString(R.string.post_card_showcase_message_4));
                        break;
                    case 5:
                        mShowcaseView.hide();
                        break;
                }
                showcaseIndex++;
            }
        };

        mShowcaseView = new ShowcaseView.Builder(this)
                .withMaterialShowcase()
                .setContentTitle(getString(R.string.post_card_showcase_title_0))
                .setContentText(getString(R.string.post_card_showcase_message_0))
                .setStyle(R.style.ShowcaseTheme)
                .singleShot(Constants.SHOWCASE_SINGLE_SHOT_POST_CARD_NUMBER)
                .setOnClickListener(showcaseViewOnClickListener)
                .build();
        mShowcaseView.setButtonText(getString(R.string.post_card_showcase_button_1));
        mShowcaseView.setButtonPosition(centerParams);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.diary_post_card, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.bgColor:
                ColorPickerDialogBuilder
                        .with(PostCardActivity.this)
//                        .setTitle("Choose Color")
                        .initialColor(mBgColor)
                        .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                        .density(12)
                        .setPositiveButton("ok", new ColorPickerClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                                mBgColor = selectedColor;
                                mContentsContainer.setBackgroundColor(mBgColor);
                            }
                        })
                        .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
//						.showColorEdit(true)
//                        .setColorEditTextColor(ContextCompat.getColor(PostCardActivity.this, android.R.color.holo_blue_bright))
                        .build()
                        .show();
                break;
            case R.id.textColor:
                ColorPickerDialogBuilder
                        .with(PostCardActivity.this)
//                        .setTitle("Choose Color")
                        .initialColor(mTextColor)
                        .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                        .density(12)
                        .setPositiveButton("ok", new ColorPickerClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                                mTextColor = selectedColor;
                                mTitle.setTextColor(mTextColor);
                                mDate.setTextColor(mTextColor);
                                mContents.setTextColor(mTextColor);
                            }
                        })
                        .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
//						.showColorEdit(true)
//                        .setColorEditTextColor(ContextCompat.getColor(PostCardActivity.this, android.R.color.holo_blue_bright))
                        .build()
                        .show();
                break;
            case R.id.save:
                if (PermissionUtils.INSTANCE.checkPermission(this, Constants.INSTANCE.getEXTERNAL_STORAGE_PERMISSIONS())) {
                    // API Level 22 이하이거나 API Level 23 이상이면서 권한취득 한경우
                    exportDiaryCard(true);
                } else {
                    // API Level 23 이상이면서 권한취득 안한경우
                    PermissionUtils.INSTANCE.confirmPermission(this, this, Constants.INSTANCE.getEXTERNAL_STORAGE_PERMISSIONS(), Constants.REQUEST_CODE_EXTERNAL_STORAGE);
                }
                break;
            case R.id.share:
                if (PermissionUtils.INSTANCE.checkPermission(this, Constants.INSTANCE.getEXTERNAL_STORAGE_PERMISSIONS())) {
                    // API Level 22 이하이거나 API Level 23 이상이면서 권한취득 한경우
                    exportDiaryCard(false);
                } else {
                    // API Level 23 이상이면서 권한취득 안한경우
                    PermissionUtils.INSTANCE.confirmPermission(this, this, Constants.INSTANCE.getEXTERNAL_STORAGE_PERMISSIONS(), Constants.REQUEST_CODE_EXTERNAL_STORAGE_WITH_SHARE_DIARY_CARD);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

//    @OnClick({R.id.bgColor, R.id.textColor, R.id.close, R.id.save, R.id.share})
//    public void onClick(View view) {
//        switch (view.getId()) {
//            case R.id.bgColor:
////                Intent bgColorPickerIntent = new Intent(PostCardActivity.this, ColorPickerActivity.class);
////                if (StringUtils.isNotEmpty(mBgHexColor)) {
////                    bgColorPickerIntent.putExtra("hexStringColor", mBgHexColor);
////                }
////                startActivityForResult(bgColorPickerIntent, Constants.REQUEST_CODE_BACKGROUND_COLOR_PICKER);
//                ColorPickerDialogBuilder
//                        .with(PostCardActivity.this)
////                        .setTitle("Choose Color")
//                        .initialColor(mBgColor)
//                        .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
//                        .density(12)
//                        .setPositiveButton("ok", new ColorPickerClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
//                                mBgColor = selectedColor;
//                                mContentsContainer.setBackgroundColor(mBgColor);
//                            }
//                        })
//                        .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                            }
//                        })
////						.showColorEdit(true)
////                        .setColorEditTextColor(ContextCompat.getColor(PostCardActivity.this, android.R.color.holo_blue_bright))
//                        .build()
//                        .show();
//
//                break;
//            case R.id.textColor:
////                Intent textColorPickerIntent = new Intent(PostCardActivity.this, ColorPickerActivity.class);
////                if (StringUtils.isNotEmpty(mTextHexColor)) {
////                    textColorPickerIntent.putExtra("hexStringColor", mTextHexColor);
////                }
////                startActivityForResult(textColorPickerIntent, Constants.REQUEST_CODE_TEXT_COLOR_PICKER);
//                ColorPickerDialogBuilder
//                        .with(PostCardActivity.this)
////                        .setTitle("Choose Color")
//                        .initialColor(mTextColor)
//                        .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
//                        .density(12)
//                        .setPositiveButton("ok", new ColorPickerClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
//                                mTextColor = selectedColor;
//                                mTitle.setTextColor(mTextColor);
//                                mDate.setTextColor(mTextColor);
//                                mContents.setTextColor(mTextColor);
//                            }
//                        })
//                        .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                            }
//                        })
////						.showColorEdit(true)
////                        .setColorEditTextColor(ContextCompat.getColor(PostCardActivity.this, android.R.color.holo_blue_bright))
//                        .build()
//                        .show();
//                break;
//            case R.id.close:
//                finish();
//                break;
//            case R.id.save:
//                if (PermissionUtils.checkPermission(this, Constants.EXTERNAL_STORAGE_PERMISSIONS)) {
//                    // API Level 22 이하이거나 API Level 23 이상이면서 권한취득 한경우
//                    exportDiaryCard(true);
//                } else {
//                    // API Level 23 이상이면서 권한취득 안한경우
//                    PermissionUtils.confirmPermission(this, this, Constants.EXTERNAL_STORAGE_PERMISSIONS, Constants.REQUEST_CODE_EXTERNAL_STORAGE);
//                }
//                break;
//            case R.id.share:
//                if (PermissionUtils.checkPermission(this, Constants.EXTERNAL_STORAGE_PERMISSIONS)) {
//                    // API Level 22 이하이거나 API Level 23 이상이면서 권한취득 한경우
//                    exportDiaryCard(false);
//                } else {
//                    // API Level 23 이상이면서 권한취득 안한경우
//                    PermissionUtils.confirmPermission(this, this, Constants.EXTERNAL_STORAGE_PERMISSIONS, Constants.REQUEST_CODE_EXTERNAL_STORAGE_WITH_SHARE_DIARY_CARD);
//                }
//                break;
//        }
//    }

    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    private void exportDiaryCard(final boolean showInfoDialog) {
        // draw viewGroup on UI Thread
        final Bitmap bitmap = BitmapUtils.INSTANCE.diaryViewGroupToBitmap(mPostContainer);
        progressBar.setVisibility(View.VISIBLE);

        // generate postcard file another thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final String diaryCardPath = Path.INSTANCE.getWORKING_DIRECTORY() + mSequence + "_" + DateUtils.INSTANCE.getCurrentDateTime(DateUtils.INSTANCE.getDATE_TIME_PATTERN_WITHOUT_DELIMITER()) + ".jpg";
                    mSavedDiaryCardPath = Environment.getExternalStorageDirectory().getAbsolutePath() + diaryCardPath;
                    EasyDiaryUtils.initWorkingDirectory(Environment.getExternalStorageDirectory().getAbsolutePath() + Path.INSTANCE.getUSER_CUSTOM_FONTS_DIRECTORY());
                    BitmapUtils.INSTANCE.saveBitmapToFileCache(bitmap, mSavedDiaryCardPath);
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE);
                            if (showInfoDialog) {
                                DialogUtils.INSTANCE.showAlertDialog(PostCardActivity.this, getString(R.string.diary_card_export_info) , diaryCardPath, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                });
                            } else {
                                shareDiary();
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    final String errorMessage = e.getMessage();
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE);
                            String errorInfo = String.format("%s\n\n[ERROR: %s]", getString(R.string.diary_card_export_error_message), errorMessage);
                            DialogUtils.INSTANCE.showAlertDialog(PostCardActivity.this, errorInfo, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                        }
                    });
                }
            }
        }).start();
    }

    private void shareDiary() {
        File file = new File(mSavedDiaryCardPath);
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        shareIntent.setType("image/jpeg");
        startActivity(Intent.createChooser(shareIntent, getString(R.string.diary_card_share_info)));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Constants.REQUEST_CODE_EXTERNAL_STORAGE:
                if (PermissionUtils.INSTANCE.checkPermission(this, Constants.INSTANCE.getEXTERNAL_STORAGE_PERMISSIONS())) {
                    // 권한이 있는경우
                    exportDiaryCard(true);
                } else {
                    // 권한이 없는경우
                    DialogUtils.INSTANCE.makeSnackBar(findViewById(android.R.id.content), getString(R.string.guide_message_3));
                }
                break;
            case Constants.REQUEST_CODE_EXTERNAL_STORAGE_WITH_SHARE_DIARY_CARD:
                if (PermissionUtils.INSTANCE.checkPermission(this, Constants.INSTANCE.getEXTERNAL_STORAGE_PERMISSIONS())) {
                    // 권한이 있는경우
                    exportDiaryCard(false);
                } else {
                    // 권한이 없는경우
                    DialogUtils.INSTANCE.makeSnackBar(findViewById(android.R.id.content), getString(R.string.guide_message_3));
                }
                break;
            default:
                break;
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constants.REQUEST_CODE_BACKGROUND_COLOR_PICKER:
                if ((resultCode == RESULT_OK) && (data != null)) {
                    String hexStringColor = "#" + data.getStringExtra("color");
//                    mBgHexColor = hexStringColor;
//                    GradientDrawable shape =  new GradientDrawable();
//                    shape.setCornerRadius(CommonUtils.dpToPixel(PostCardActivity.this, 5));
//                    shape.setColor(Color.parseColor(hexStringColor));
//                    mContentsContainer.setBackgroundDrawable(shape);
                    mContentsContainer.setBackgroundColor(Color.parseColor(hexStringColor));
                }
                break;
            case Constants.REQUEST_CODE_TEXT_COLOR_PICKER:
                if ((resultCode == RESULT_OK) && (data != null)) {
                    String hexStringColor = "#" + data.getStringExtra("color");
//                    mTextHexColor = hexStringColor;
                    mTitle.setTextColor(Color.parseColor(hexStringColor));
                    mDate.setTextColor(Color.parseColor(hexStringColor));
                    mContents.setTextColor(Color.parseColor(hexStringColor));
                }
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setFontsStyle();
    }

    private void setFontsStyle() {
        FontUtils.setFontsTypeface(getApplicationContext(), getAssets(), null, (ViewGroup) findViewById(android.R.id.content));
    }

}
