package me.blog.korn123.commons.utils;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.text.SpannedString;
import android.text.style.BackgroundColorSpan;
import android.widget.TextView;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Administrator on 2017-11-02.
 */

public class EasyDiaryUtilsTest {

    private static Context sContext;

    @BeforeClass
    public static void init() {
        sContext = InstrumentationRegistry.getTargetContext();
    }


    @Test
    public void test_01() {
        TextView textView = new TextView(sContext);
        textView.setText("apple banana pineapple");
        EasyDiaryUtils.highlightString(textView, "APPLE");

        SpannedString spannedString = (SpannedString) textView.getText();
        BackgroundColorSpan[] backgroundSpans = spannedString.getSpans(0, spannedString.length(), BackgroundColorSpan.class);
        assertFalse(backgroundSpans.length == 2);
    }

    @Test
    public void test_02() {
        TextView textView = new TextView(sContext);
        textView.setText("apple banana pineapple");
        EasyDiaryUtils.highlightStringIgnoreCase(textView, "APPLE");

        SpannedString spannedString = (SpannedString) textView.getText();
        BackgroundColorSpan[] backgroundSpans = spannedString.getSpans(0, spannedString.length(), BackgroundColorSpan.class);
        assertTrue(backgroundSpans.length == 2);
    }

}
