package me.blog.korn123.easydiary.colorpicker;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorChangedListener;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.slider.LightnessSlider;

import org.apache.commons.lang3.StringUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_picker);
        ButterKnife.bind(this);
        mColorPickerView = (ColorPickerView) findViewById(R.id.color_picker_view);
        mColorPickerView.addOnColorChangedListener(new OnColorChangedListener() {
            @Override public void onColorChanged(int selectedColor) {
                // Handle on color change
//                Log.d("ColorPicker", "onColorChanged: 0x" + Integer.toHexString(selectedColor));
            }
        });

        mColorPickerView.addOnColorSelectedListener(new OnColorSelectedListener() {
            @Override
            public void onColorSelected(int selectedColor) {
                mSelectedColor = Integer.toHexString(selectedColor).toUpperCase();
//                Toast.makeText(
//                        ColorPickerActivity.this,
//                        "selectedColor: " + mSelectedColor,
//                        Toast.LENGTH_SHORT).show();
            }
        });

        if (StringUtils.isNotEmpty(getIntent().getStringExtra("hexStringColor"))) {
            String hexStringColor = getIntent().getStringExtra("hexStringColor");
            mColorPickerView.setInitialColor(Color.parseColor(hexStringColor), false);
        }

        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("color", Integer.toHexString(mColorPickerView.getSelectedColor()));
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

}
