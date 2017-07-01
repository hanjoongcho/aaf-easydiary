package me.blog.korn123.easydiary.diary;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.blog.korn123.commons.constants.Constants;
import me.blog.korn123.commons.constants.Path;
import me.blog.korn123.commons.utils.BitmapUtils;
import me.blog.korn123.commons.utils.CommonUtils;
import me.blog.korn123.commons.utils.DialogUtils;
import me.blog.korn123.commons.utils.EasyDiaryUtils;
import me.blog.korn123.commons.utils.FontUtils;
import me.blog.korn123.commons.utils.PermissionUtils;
import me.blog.korn123.easydiary.R;
import me.blog.korn123.easydiary.colorpicker.ColorPickerActivity;
import me.blog.korn123.easydiary.helper.EasyDiaryActivity;

/**
 * Created by hanjoong on 2017-07-01.
 */

public class PostCardActivity extends EasyDiaryActivity {

    private int mSequence;
    private String mBgHexColor;
    private String mTextHexColor;

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

        Intent intent = getIntent();
        mSequence = intent.getIntExtra("sequence", 0);
        DiaryDto diaryDto = DiaryDao.readDiaryBy(mSequence);

        EasyDiaryUtils.initWeatherView(mWeather, diaryDto.getWeather());
        mTitle.setText(diaryDto.getTitle());
        mContents.setText(diaryDto.getContents());
        mDate.setText(diaryDto.getDateString());
    }

    @OnClick({R.id.bgColor, R.id.textColor, R.id.close, R.id.save})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bgColor:
                Intent bgColorPickerIntent = new Intent(PostCardActivity.this, ColorPickerActivity.class);
                if (StringUtils.isNotEmpty(mBgHexColor)) {
                    bgColorPickerIntent.putExtra("hexStringColor", mBgHexColor);
                }
                startActivityForResult(bgColorPickerIntent, Constants.REQUEST_CODE_BACKGROUND_COLOR_PICKER);
                break;
            case R.id.textColor:
                Intent textColorPickerIntent = new Intent(PostCardActivity.this, ColorPickerActivity.class);
                if (StringUtils.isNotEmpty(mTextHexColor)) {
                    textColorPickerIntent.putExtra("hexStringColor", mTextHexColor);
                }
                startActivityForResult(textColorPickerIntent, Constants.REQUEST_CODE_TEXT_COLOR_PICKER);
                break;
            case R.id.close:
                finish();
                break;
            case R.id.save:
                if (PermissionUtils.checkPermission(this, Constants.EXTERNAL_STORAGE_PERMISSIONS)) {
                    // API Level 22 이하이거나 API Level 23 이상이면서 권한취득 한경우
                    exportDiaryCard();
                } else {
                    // API Level 23 이상이면서 권한취득 안한경우
                    PermissionUtils.confirmPermission(this, this, Constants.EXTERNAL_STORAGE_PERMISSIONS, Constants.REQUEST_CODE_EXTERNAL_STORAGE);
                }
                break;
        }
    }

    private void exportDiaryCard() {
        Bitmap bitmap = BitmapUtils.viewToBitmap(mPostContainer);
        EasyDiaryUtils.initWorkingDirectory();
        String diaryCardPath = Path.WORKING_DIRECTORY + mSequence + "_" + UUID.randomUUID().toString() + ".jpg";
        BitmapUtils.saveBitmapToFileCache(bitmap, Environment.getExternalStorageDirectory().getAbsolutePath() + diaryCardPath);
        DialogUtils.showAlertDialog(PostCardActivity.this, getString(R.string.diary_card_export_info) , diaryCardPath, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Constants.REQUEST_CODE_EXTERNAL_STORAGE:
                if (PermissionUtils.checkPermission(this, Constants.EXTERNAL_STORAGE_PERMISSIONS)) {
                    // 권한이 있는경우
                    exportDiaryCard();
                } else {
                    // 권한이 없는경우
                    DialogUtils.makeSnackBar(findViewById(android.R.id.content), getString(R.string.guide_message_3));
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
                    mBgHexColor = hexStringColor;
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
                    mTextHexColor = hexStringColor;
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

        FontUtils.setTypeface(this, getAssets(), mTitle);
        FontUtils.setTypeface(this, getAssets(), mDate);
        FontUtils.setTypeface(this, getAssets(), mContents);
        setDiaryFontSize();
    }

    private void setDiaryFontSize() {
        float fontSize = CommonUtils.loadFloatPreference(this, "font_size", 0);
        if (fontSize > 0) {
            mTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize);
            mDate.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize);
            mContents.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize);
        }
    }

}
