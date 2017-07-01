package me.blog.korn123.easydiary.colorpicker;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorChangedListener;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.slider.LightnessSlider;

import org.apache.commons.lang3.StringUtils;

import java.util.logging.Handler;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.blog.korn123.commons.constants.Constants;
import me.blog.korn123.easydiary.R;
import me.blog.korn123.easydiary.helper.EasyDiaryActivity;

/**
 * Created by hanjoong on 2017-07-01.
 */

public class ColorPickerActivity extends EasyDiaryActivity {

    private String mSelectedColor;

    @BindView(R.id.ok)
    FloatingActionButton mFloatingActionButton;

    ColorPickerView mColorPickerView;
    LightnessSlider mLightnessSlider;
    String mHexStringColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_picker);
        ButterKnife.bind(this);
        mColorPickerView = (ColorPickerView) findViewById(R.id.color_picker_view);
        mLightnessSlider= (LightnessSlider) findViewById(R.id.v_lightness_slider);
        mColorPickerView.addOnColorChangedListener(new OnColorChangedListener() {
            @Override public void onColorChanged(int selectedColor) {
                // Handle on color change
                Log.d("change", "onColorChanged: 0x" + Integer.toHexString(selectedColor));
            }
        });

        mColorPickerView.addOnColorSelectedListener(new OnColorSelectedListener() {
            @Override
            public void onColorSelected(int selectedColor) {
                Log.d("select", "onColorChanged: 0x" + Integer.toHexString(selectedColor));
                mSelectedColor = Integer.toHexString(selectedColor).toUpperCase();
//                Toast.makeText(
//                        ColorPickerActivity.this,
//                        "selectedColor: " + mSelectedColor,
//                        Toast.LENGTH_SHORT).show();
            }
        });

        if (StringUtils.isNotEmpty(getIntent().getStringExtra("hexStringColor"))) {
            mHexStringColor = getIntent().getStringExtra("hexStringColor");
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        Thread.sleep(500);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    new android.os.Handler(Looper.getMainLooper()).post(new Runnable() {
//                        @Override
//                        public void run() {
//                            mColorPickerView.setInitialColor(Color.parseColor(mHexStringColor), false);
//                        }
//                    });
//                }
//            }).start();
        }

    }

    @OnClick({R.id.ok, R.id.test})
    public void onClick(View view){

        switch (view.getId()) {
            case R.id.ok:
                Intent intent = new Intent();
                intent.putExtra("color", Integer.toHexString(mColorPickerView.getSelectedColor()));
                setResult(RESULT_OK, intent);
                finish();
                break;
            case R.id.test:
                mColorPickerView.setInitialColor(0xff5f00ff, false);
                Log.d("select", "onColorChanged: 0x" + mHexStringColor);
                break;
        }
    }

}
