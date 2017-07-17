package me.blog.korn123.easydiary.timeline;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import me.blog.korn123.commons.utils.CommonUtils;
import me.blog.korn123.commons.utils.DateUtils;
import me.blog.korn123.commons.utils.EasyDiaryUtils;
import me.blog.korn123.commons.utils.FontUtils;
import me.blog.korn123.easydiary.R;
import me.blog.korn123.easydiary.diary.DiaryDto;

/**
 * Created by hanjoong on 2017-07-16.
 */

public class TimelineArrayAdapter extends ArrayAdapter<DiaryDto> {

    private final Context context;
    private final List<DiaryDto> list;
    private final int layoutResourceId;

    public TimelineArrayAdapter(@NonNull Context context, @LayoutRes int layoutResourceId, @NonNull List<DiaryDto> list) {
        super(context, layoutResourceId, list);
        this.context = context;
        this.list = list;
        this.layoutResourceId = layoutResourceId;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder = null;
        if (row == null) {
            LayoutInflater inflater = ((Activity)this.context).getLayoutInflater();
            row = inflater.inflate(this.layoutResourceId, parent, false);
            holder = new ViewHolder();
            holder.textView1 = (TextView) row.findViewById(R.id.text1);
            holder.textView2 = (TextView) row.findViewById(R.id.text2);
            holder.title = (TextView) row.findViewById(R.id.title);
            holder.horizontalLine1 = row.findViewById(R.id.horizontalLine1);
            holder.horizontalLine2 = row.findViewById(R.id.horizontalLine2);
            holder.titleContainer = (ViewGroup) row.findViewById(R.id.titleContainer);
            holder.weather = (ImageView) row.findViewById(R.id.weather);
            row.setTag(holder);
        } else {
            holder = (ViewHolder)row.getTag();
        }

        initFontStyle(holder);
        float fontSize = CommonUtils.loadFloatPreference(context, "font_size", 0);
        if (fontSize > 0) {
            holder.textView1.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize);
            holder.textView2.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize);
            holder.title.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize);
        }

        DiaryDto diaryDto = list.get(position);
        if (position > 0 && StringUtils.equals(diaryDto.getDateString(), list.get(position - 1).getDateString())) {
            holder.titleContainer.setVisibility(View.GONE);
            holder.weather.setImageResource(0);
        } else {
            holder.titleContainer.setVisibility(View.VISIBLE);
//            holder.title.setText(diaryDto.getDateString() + " " + DateUtils.timeMillisToDateTime(diaryDto.getCurrentTimeMillis(), "EEEE"));
            holder.title.setText(DateUtils.getFullPatternDate(diaryDto.getCurrentTimeMillis()));
            EasyDiaryUtils.initWeatherView(holder.weather, diaryDto.getWeather());
        }

        if (position % 2 == 0) {
            holder.textView1.setVisibility(View.VISIBLE);
            holder.textView2.setVisibility(View.INVISIBLE);
            holder.horizontalLine1.setVisibility(View.VISIBLE);
            holder.horizontalLine2.setVisibility(View.INVISIBLE);
            holder.textView1.setText(DateUtils.timeMillisToDateTime(diaryDto.getCurrentTimeMillis(), DateUtils.TIME_HMS_PATTERN_COLONE) + "\n" + diaryDto.getTitle());
//            holder.textView1.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        } else {
            holder.textView1.setVisibility(View.INVISIBLE);
            holder.textView2.setVisibility(View.VISIBLE);
            holder.horizontalLine1.setVisibility(View.INVISIBLE);
            holder.horizontalLine2.setVisibility(View.VISIBLE);
            holder.textView2.setText(DateUtils.timeMillisToDateTime(diaryDto.getCurrentTimeMillis(), DateUtils.TIME_HMS_PATTERN_COLONE) + "\n" + diaryDto.getTitle());
//            holder.textView2.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }


        return row;
    }

    private void initFontStyle(ViewHolder holder) {
        FontUtils.setTypeface(context, context.getAssets(), holder.textView1);
        FontUtils.setTypeface(context, context.getAssets(), holder.textView2);
        FontUtils.setTypeface(context, context.getAssets(), holder.title);
    }

    private static class ViewHolder {
        TextView textView1;
        TextView textView2;
        TextView title;
        View horizontalLine1;
        View horizontalLine2;
        ViewGroup titleContainer;
        ImageView weather;
    }
}
