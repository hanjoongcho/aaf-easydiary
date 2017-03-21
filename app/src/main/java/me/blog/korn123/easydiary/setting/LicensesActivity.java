package me.blog.korn123.easydiary.setting;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.blog.korn123.easydiary.R;

/**
 * Created by CHO HANJOONG on 2017-02-11.
 */

public class LicensesActivity extends AppCompatActivity {

    @BindView(R.id.licenses) WebView webView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_licenses);
        ButterKnife.bind(this);
        webView.loadUrl("file:///android_asset/licenses.html");
    }

}
