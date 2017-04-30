package me.blog.korn123.commons.utils;

import android.view.View;
import android.widget.ImageView;

import me.blog.korn123.commons.constants.Constants;
import me.blog.korn123.easydiary.R;

/**
 * Created by hanjoong on 2017-04-30.
 */

public class EasyDiaryUtils {

    public static void initWeatherView(ImageView imageView, int weatherFlag) {
        initWeatherView(imageView, weatherFlag, false);
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
            case Constants.WEATHER_FLAG_SUN:
                imageView.setImageResource(R.drawable.ic_sun);
                break;
            case Constants.WEATHER_FLAG_SUN_AND_CLOUD:
                imageView.setImageResource(R.drawable.ic_cloud);
                break;
            case Constants.WEATHER_FLAG_RAIN:
                imageView.setImageResource(R.drawable.ic_rain);
                break;
            case Constants.WEATHER_FLAG_THUNDER_BOLT:
                imageView.setImageResource(R.drawable.ic_storm);
                break;
            case Constants.WEATHER_FLAG_SNOW:
                imageView.setImageResource(R.drawable.ic_snow_2);
                break;
        }
    }

}
