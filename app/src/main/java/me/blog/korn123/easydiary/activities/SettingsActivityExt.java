package me.blog.korn123.easydiary.activities;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.blog.korn123.commons.constants.Path;
import me.blog.korn123.commons.utils.DialogUtils;
import me.blog.korn123.commons.utils.FontUtils;
import me.blog.korn123.easydiary.R;
import me.blog.korn123.easydiary.adapters.FontItemAdapter;

/**
 * Created by CHO HANJOONG on 2017-11-04.
 */

public class SettingsActivityExt extends EasyDiaryActivity {


    @BindView(R.id.pref2)
    LinearLayout pref2;

    @BindView(R.id.pref2Switcher)
    Switch pref2Switcher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);

        setSupportActionBar((Toolbar)findViewById(R.id.toolbar));
        getSupportActionBar().setTitle("Setting");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setFontsTypeface();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setFontsTypeface() {
        FontUtils.setFontsTypeface(SettingsActivityExt.this, getAssets(), null, (ViewGroup) findViewById(android.R.id.content));
    }

    @OnClick({ R.id.pref1, R.id.pref2 })
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.pref1:
//                DialogUtils.makeSnackBar(view, "OK");
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivityExt.this);
                builder.setPositiveButton("OK", null);
                builder.setNegativeButton("CANCEL", null);
                LayoutInflater inflater = (LayoutInflater) this.getSystemService(this.LAYOUT_INFLATER_SERVICE);
                View fontView = inflater.inflate(R.layout.dialog_fonts, null);
                ListView listView = (ListView) fontView.findViewById(R.id.listFont);


                String[] fontNameArray = getResources().getStringArray(R.array.pref_list_fonts_title);
                String[] fontPathArray = getResources().getStringArray(R.array.pref_list_fonts_values);
                List<Map<String, String>> listFont = new ArrayList<>();
                for (int i = 0; i < fontNameArray.length; i++) {
                    Map<String, String> map = new HashMap<>();
                    map.put("fontName" , fontNameArray[i]);
                    map.put("fontPath" , fontPathArray[i]);
                    listFont.add(map);
                }

                File fontDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + Path.USER_CUSTOM_FONTS_DIRECTORY);
                if (fontDir.list() != null && fontDir.list().length > 0) {
                    for (String fontName : fontDir.list()) {
                        if (FilenameUtils.getExtension(fontName).equalsIgnoreCase("ttf")) {
                            Map<String, String> map = new HashMap<>();
                            map.put("fontName" , FilenameUtils.getBaseName(fontName));
                            map.put("fontPath" , fontName);
                            listFont.add(map);
                        }
                    }
                }

                ArrayAdapter arrayAdapter = new FontItemAdapter(SettingsActivityExt.this, R.layout.item_font, listFont);
                listView.setAdapter(arrayAdapter);
                builder.setView(fontView);
                builder.create().show();
                break;
            case R.id.pref2:
                pref2Switcher.toggle();
                break;
        }
    }

}
