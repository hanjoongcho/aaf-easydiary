package me.blog.korn123.easydiary.activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.blog.korn123.commons.constants.Constants;
import me.blog.korn123.commons.constants.Path;
import me.blog.korn123.commons.utils.CommonUtils;
import me.blog.korn123.commons.utils.DialogUtils;
import me.blog.korn123.commons.utils.FontUtils;
import me.blog.korn123.commons.utils.PermissionUtils;
import me.blog.korn123.easydiary.R;
import me.blog.korn123.easydiary.adapters.FontItemAdapter;
import me.blog.korn123.easydiary.googledrive.GoogleDriveDownloader;
import me.blog.korn123.easydiary.googledrive.GoogleDriveUploader;

/**
 * Created by CHO HANJOONG on 2017-11-04.
 */

public class SettingsActivityExt extends EasyDiaryActivity {

    @BindView(R.id.prefSummary1)
    TextView prefSummary1;

    @BindView(R.id.pref2Switcher)
    Switch pref2Switcher;

    @BindView(R.id.pref4Switcher)
    Switch pref4Switcher;

    @BindView(R.id.prefSummary5)
    TextView prefSummary5;

    @BindView(R.id.prefSummary8)
    TextView prefSummary8;

    private AlertDialog mAlertDialog;

    private static int mTaskFlag = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);

        setSupportActionBar((Toolbar)findViewById(R.id.toolbar));
        getSupportActionBar().setTitle(R.string.setting_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initPreference();
    }

    private void initPreference() {
        prefSummary1.setText(CommonUtils.loadStringPreference(SettingsActivityExt.this, Constants.SETTING_FONT_NAME, Constants.CUSTOM_FONTS_SUPPORTED_LANGUAGE_DEFAULT));
        pref2Switcher.setChecked(CommonUtils.loadBooleanPreference(SettingsActivityExt.this, Constants.DIARY_SEARCH_QUERY_CASE_SENSITIVE));
        pref4Switcher.setChecked(CommonUtils.loadBooleanPreference(SettingsActivityExt.this, Constants.APP_LOCK_ENABLE));
        prefSummary5.setText(getString(R.string.lock_number) + " " + CommonUtils.loadStringPreference(SettingsActivityExt.this, Constants.APP_LOCK_SAVED_PASSWORD, "0000"));
        prefSummary8.setText(String.format("Easy Diary v %s", getPackageVersion()));
    }

    String getPackageVersion() {
        PackageInfo packageInfo = null;
        try {
            packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return packageInfo.versionName;
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

    @OnClick({ R.id.pref1, R.id.pref2, R.id.pref3, R.id.pref4, R.id.pref5, R.id.pref6, R.id.pref7, R.id.pref8, R.id.pref9 })
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
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Map<String, String> fontInfo = (HashMap) parent.getAdapter().getItem(position);
                        CommonUtils.saveStringPreference(SettingsActivityExt.this, Constants.SETTING_FONT_NAME, fontInfo.get("fontPath"));
                        FontUtils.setCommonTypeface(SettingsActivityExt.this, getAssets());
                        initPreference();
                        setFontsTypeface();
                        mAlertDialog.cancel();
                    }
                });

                builder.setView(fontView);
                mAlertDialog = builder.create();
                mAlertDialog.show();
                break;
            case R.id.pref2:
                pref2Switcher.toggle();
                CommonUtils.saveBooleanPreference(SettingsActivityExt.this, Constants.DIARY_SEARCH_QUERY_CASE_SENSITIVE, pref2Switcher.isChecked());
                break;
            case R.id.pref3:
                Intent intent = new Intent(SettingsActivityExt.this, WebViewActivity.class);
                intent.putExtra(Constants.OPEN_URL_INFO, getString(R.string.add_ttf_fonts_info_url));
                startActivity(intent);
                break;
            case R.id.pref4:
                pref4Switcher.toggle();
                CommonUtils.saveBooleanPreference(SettingsActivityExt.this, Constants.APP_LOCK_ENABLE, pref4Switcher.isChecked());
                break;
            case R.id.pref5:
                Intent lockSettingIntent = new Intent(SettingsActivityExt.this, LockSettingActivity.class);
                startActivityForResult(lockSettingIntent, Constants.REQUEST_CODE_LOCK_SETTING);
                break;
            case R.id.pref6:
                mTaskFlag = Constants.SETTING_FLAG_IMPORT_GOOGLE_DRIVE;
                if (PermissionUtils.checkPermission(SettingsActivityExt.this, Constants.EXTERNAL_STORAGE_PERMISSIONS)) {
                    // API Level 22 이하이거나 API Level 23 이상이면서 권한취득 한경우
                    Intent downloadIntent = new Intent(SettingsActivityExt.this, GoogleDriveDownloader.class);
                    startActivity(downloadIntent);
                } else {
                    // API Level 23 이상이면서 권한취득 안한경우
                    PermissionUtils.confirmPermission(SettingsActivityExt.this, SettingsActivityExt.this, Constants.EXTERNAL_STORAGE_PERMISSIONS, Constants.REQUEST_CODE_EXTERNAL_STORAGE);
                }
                break;
            case R.id.pref7:
                mTaskFlag = Constants.SETTING_FLAG_EXPORT_GOOGLE_DRIVE;
                if (PermissionUtils.checkPermission(SettingsActivityExt.this , Constants.EXTERNAL_STORAGE_PERMISSIONS)) {
                    // API Level 22 이하이거나 API Level 23 이상이면서 권한취득 한경우
                    Intent uploadIntent = new Intent(SettingsActivityExt.this, GoogleDriveUploader.class);
                    startActivity(uploadIntent);
                } else {
                    // API Level 23 이상이면서 권한취득 안한경우
                    PermissionUtils.confirmPermission(SettingsActivityExt.this, SettingsActivityExt.this, Constants.EXTERNAL_STORAGE_PERMISSIONS, Constants.REQUEST_CODE_EXTERNAL_STORAGE);
                }
                break;
            case R.id.pref8:
                Uri uri = Uri.parse("market://details?id=me.blog.korn123.easydiary");
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                // To count with Play market backstack, After pressing back button,
                // to taken back to our application, we need to add following flags to intent.
                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=me.blog.korn123.easydiary")));
                }
                break;
            case R.id.pref9:
                Intent webViewIntent = new Intent(SettingsActivityExt.this, WebViewActivity.class);
                webViewIntent.putExtra(Constants.OPEN_URL_INFO, "https://github.com/hanjoongcho/aaf-easydiary/blob/master/LICENSE.md");
                startActivity(webViewIntent);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            String password = data.getStringExtra(Constants.APP_LOCK_REQUEST_PASSWORD);
            CommonUtils.saveStringPreference(getApplicationContext(), Constants.APP_LOCK_SAVED_PASSWORD, password);
            prefSummary5.setText(getString(R.string.lock_number) + " " + password);
        }
        CommonUtils.saveLongPreference(getApplicationContext(), Constants.SETTING_PAUSE_MILLIS, System.currentTimeMillis());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Constants.REQUEST_CODE_EXTERNAL_STORAGE:
                if (PermissionUtils.checkPermission(getApplicationContext(), Constants.EXTERNAL_STORAGE_PERMISSIONS)) {
                    // 권한이 있는경우
                    if (mTaskFlag == Constants.SETTING_FLAG_EXPORT_GOOGLE_DRIVE) {
//                            FileUtils.copyFile(new File(EasyDiaryDbHelper.getRealmInstance().getPath()), new File(Path.WORKING_DIRECTORY + Path.DIARY_DB_NAME));
                        Intent uploadIntent = new Intent(getApplicationContext(), GoogleDriveUploader.class);
                        startActivity(uploadIntent);
                    } else if (mTaskFlag == Constants.SETTING_FLAG_IMPORT_GOOGLE_DRIVE) {
                        Intent downloadIntent = new Intent(getApplicationContext(), GoogleDriveDownloader.class);
                        startActivity(downloadIntent);
                    }
                } else {
                    // 권한이 없는경우
                    DialogUtils.makeSnackBar(findViewById(android.R.id.content), getString(R.string.guide_message_3));
                }
                break;
            default:
                break;
        }
    }

}
