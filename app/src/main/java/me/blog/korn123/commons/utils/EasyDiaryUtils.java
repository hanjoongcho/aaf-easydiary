package me.blog.korn123.commons.utils;

import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

import me.blog.korn123.commons.constants.Constants;
import me.blog.korn123.easydiary.R;
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper;

/**
 * Created by hanjoong on 2017-04-30.
 */

public class EasyDiaryUtils {

    public static void initWeatherView(ImageView imageView, int weatherFlag) {
        initWeatherView(imageView, weatherFlag, false);
    }

    public static void initWorkingDirectory(String path) {
        File workingDirectory = new File(path);
        if (!workingDirectory.exists()) workingDirectory.mkdirs();
    }

    public static void initWeatherView(ImageView imageView, int weatherFlag, boolean isShowEmptyWeatherView) {

        if (!isShowEmptyWeatherView && weatherFlag < 1) {
            imageView.setVisibility(View.GONE);
        } else {
            imageView.setVisibility(View.VISIBLE);
        }

        switch (weatherFlag) {
            case 0:
                imageView.setImageResource(0);
                break;
            case Constants.WEATHER_SUNNY:
                imageView.setImageResource(R.drawable.ic_sunny);
                break;
            case Constants.WEATHER_CLOUD_AND_SUN:
                imageView.setImageResource(R.drawable.ic_clouds_and_sun);
                break;
            case Constants.WEATHER_RAIN_DROPS:
                imageView.setImageResource(R.drawable.ic_raindrops);
                break;
            case Constants.WEATHER_BOLT:
                imageView.setImageResource(R.drawable.ic_bolt);
                break;
            case Constants.WEATHER_SNOWING:
                imageView.setImageResource(R.drawable.ic_snowing);
                break;
            case Constants.WEATHER_RAINBOW:
                imageView.setImageResource(R.drawable.ic_rainbow);
                break;
            case Constants.WEATHER_UMBRELLA:
                imageView.setImageResource(R.drawable.ic_umbrella_1);
                break;
            case Constants.WEATHER_STARS:
                imageView.setImageResource(R.drawable.ic_stars_2);
                break;
            case Constants.WEATHER_MOON:
                imageView.setImageResource(R.drawable.ic_moon_9);
                break;
            case Constants.WEATHER_NIGHT_RAIN:
                imageView.setImageResource(R.drawable.ic_night_rain);
                break;
        }
    }

    public static String getEasyDiaryMimeType() {
        String easyDiaryMimeType = "text/aaf_v" + EasyDiaryDbHelper.getRealmInstance().getVersion();
        return  easyDiaryMimeType;
    }

    public static String[] getEasyDiaryMimeTypeAll() {
        int currentVersion = (int)EasyDiaryDbHelper.getRealmInstance().getVersion();
        String[] easyDiaryMimeType = new String[currentVersion];
        for (int i = 0; i < currentVersion; i++) {
            easyDiaryMimeType[i] = "text/aaf_v" + (i + 1);
        }
        return  easyDiaryMimeType;
    }

    public static void highlightString(TextView textView, String input) {
        //Get the text from text view and create a spannable string
        SpannableString spannableString = new SpannableString(textView.getText());

        //Get the previous spans and remove them
        BackgroundColorSpan[] backgroundSpans = spannableString.getSpans(0, spannableString.length(), BackgroundColorSpan.class);

        for (BackgroundColorSpan span: backgroundSpans) {
            spannableString.removeSpan(span);
        }

        //Search for all occurrences of the keyword in the string
        int indexOfKeyword = spannableString.toString().indexOf(input);

        while (indexOfKeyword >= 0) {
            //Create a background color span on the keyword
            spannableString.setSpan(new BackgroundColorSpan(Color.YELLOW), indexOfKeyword, indexOfKeyword + input.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            //Get the next index of the keyword
            indexOfKeyword = spannableString.toString().indexOf(input, indexOfKeyword + input.length());
        }

        //Set the final text on TextView
        textView.setText(spannableString);
    }

}
