package me.blog.korn123.commons.utils;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.blog.korn123.easydiary.R;
import me.blog.korn123.easydiary.adapters.SecondItemAdapter;
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper;

import static me.blog.korn123.easydiary.helper.ConstantsKt.WEATHER_BOLT;
import static me.blog.korn123.easydiary.helper.ConstantsKt.WEATHER_CLOUD_AND_SUN;
import static me.blog.korn123.easydiary.helper.ConstantsKt.WEATHER_MOON;
import static me.blog.korn123.easydiary.helper.ConstantsKt.WEATHER_RAINBOW;
import static me.blog.korn123.easydiary.helper.ConstantsKt.WEATHER_RAIN_DROPS;
import static me.blog.korn123.easydiary.helper.ConstantsKt.WEATHER_SNOWING;
import static me.blog.korn123.easydiary.helper.ConstantsKt.WEATHER_STARS;
import static me.blog.korn123.easydiary.helper.ConstantsKt.WEATHER_SUNNY;
import static me.blog.korn123.easydiary.helper.ConstantsKt.WEATHER_UMBRELLA;
import static me.blog.korn123.easydiary.helper.ConstantsKt.WEATHER_NIGHT_RAIN;

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
            case WEATHER_SUNNY:
                imageView.setImageResource(R.drawable.ic_sunny);
                break;
            case WEATHER_CLOUD_AND_SUN:
                imageView.setImageResource(R.drawable.ic_clouds_and_sun);
                break;
            case WEATHER_RAIN_DROPS:
                imageView.setImageResource(R.drawable.ic_raindrops);
                break;
            case WEATHER_BOLT:
                imageView.setImageResource(R.drawable.ic_bolt);
                break;
            case WEATHER_SNOWING:
                imageView.setImageResource(R.drawable.ic_snowing);
                break;
            case WEATHER_RAINBOW:
                imageView.setImageResource(R.drawable.ic_rainbow);
                break;
            case WEATHER_UMBRELLA:
                imageView.setImageResource(R.drawable.ic_umbrella_1);
                break;
            case WEATHER_STARS:
                imageView.setImageResource(R.drawable.ic_stars_2);
                break;
            case WEATHER_MOON:
                imageView.setImageResource(R.drawable.ic_moon_9);
                break;
            case WEATHER_NIGHT_RAIN:
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

    public static void highlightStringIgnoreCase(TextView textView, String input) {
        String inputLower = input.toLowerCase();
        String contentsLower = textView.getText().toString().toLowerCase();
        SpannableString spannableString = new SpannableString(textView.getText());

        BackgroundColorSpan[] backgroundSpans = spannableString.getSpans(0, spannableString.length(), BackgroundColorSpan.class);

        for (BackgroundColorSpan span: backgroundSpans) {
            spannableString.removeSpan(span);
        }

        int indexOfKeyword = contentsLower.indexOf(inputLower);

        while (indexOfKeyword >= 0) {
            spannableString.setSpan(new BackgroundColorSpan(Color.YELLOW), indexOfKeyword, indexOfKeyword + inputLower.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            indexOfKeyword = contentsLower.indexOf(inputLower, indexOfKeyword + inputLower.length());
        }

        textView.setText(spannableString);
    }

    public static AlertDialog.Builder createSecondsPickerBuilder(Context context, AdapterView.OnItemClickListener itemClickListener, int second) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setNegativeButton("CANCEL", null);
        builder.setTitle(context.getString(R.string.common_create_seconds));
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View secondsPicker = inflater.inflate(R.layout.dialog_seconds_picker, null);
        ListView listView = secondsPicker.findViewById(R.id.seconds);
        List<Map<String, String>> listSecond = new ArrayList<>();
        for (int i = 0; i < 60; i++) {
            HashMap<String, String> map = new HashMap();
            map.put("label", String.valueOf(i) + "s");
            map.put("value", String.valueOf(i));
            listSecond.add(map);
        }
        ArrayAdapter adapter = new SecondItemAdapter(context, R.layout.item_second, listSecond, second);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(itemClickListener);
        builder.setView(secondsPicker);
        return builder;
    }
}
