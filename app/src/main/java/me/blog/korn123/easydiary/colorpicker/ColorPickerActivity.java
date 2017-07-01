package me.blog.korn123.easydiary.colorpicker;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorChangedListener;
import com.flask.colorpicker.OnColorSelectedListener;

import me.blog.korn123.commons.constants.Constants;
import me.blog.korn123.easydiary.R;
import me.blog.korn123.easydiary.helper.EasyDiaryActivity;

/**
 * Created by hanjoong on 2017-07-01.
 */

public class ColorPickerActivity extends EasyDiaryActivity {

    private String mSelectedColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_picker);

        ColorPickerView colorPickerView = (ColorPickerView) findViewById(R.id.color_picker_view);
        colorPickerView.addOnColorChangedListener(new OnColorChangedListener() {
            @Override public void onColorChanged(int selectedColor) {
                // Handle on color change
                Log.d("ColorPicker", "onColorChanged: 0x" + Integer.toHexString(selectedColor));
            }
        });

        colorPickerView.addOnColorSelectedListener(new OnColorSelectedListener() {
            @Override
            public void onColorSelected(int selectedColor) {
                mSelectedColor = Integer.toHexString(selectedColor).toUpperCase();
                Toast.makeText(
                        ColorPickerActivity.this,
                        "selectedColor: " + mSelectedColor,
                        Toast.LENGTH_SHORT).show();

                Intent intent = new Intent();
                intent.putExtra("color", mSelectedColor);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

}
