package me.blog.korn123.easydiary.activities;

import android.os.Bundle;
import android.webkit.WebView;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.blog.korn123.easydiary.R;
import me.blog.korn123.easydiary.activities.EasyDiaryActivity;

/**
 * Created by CHO HANJOONG on 2017-02-11.
 */

public class LicensesActivity extends EasyDiaryActivity {

    @BindView(R.id.licenses) WebView webView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_licenses);
        ButterKnife.bind(this);
        webView.loadUrl("https://github.com/hanjoongcho/aaf-easydiary/blob/master/LICENSE.md");
    }

}
