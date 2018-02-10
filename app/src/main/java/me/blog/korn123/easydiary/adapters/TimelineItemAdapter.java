package me.blog.korn123.easydiary.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import io.github.hanjoongcho.commons.helpers.BaseConfig;
import me.blog.korn123.commons.constants.Constants;
import me.blog.korn123.commons.utils.CommonUtils;
import me.blog.korn123.commons.utils.DateUtils;
import me.blog.korn123.commons.utils.EasyDiaryUtils;
import me.blog.korn123.commons.utils.FontUtils;
import me.blog.korn123.easydiary.R;
import me.blog.korn123.easydiary.extensions.ContextKt;
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper;
import me.blog.korn123.easydiary.models.DiaryDto;

/**
 * Created by hanjoong on 2017-07-16.
 */

public class TimelineItemAdapter extends ArrayAdapter<DiaryDto> {

    private final Context context;
    private final List<DiaryDto> list;
    private final int layoutResourceId;
    private int mPrimaryColor = 0;

    public TimelineItemAdapter(@NonNull Context context, @LayoutRes int layoutResourceId, @NonNull List<DiaryDto> list) {
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
            holder.textView1 = row.findViewById(R.id.text1);
            holder.title = row.findViewById(R.id.title);
            holder.horizontalLine2 = row.findViewById(R.id.horizontalLine2);
            holder.titleContainer = row.findViewById(R.id.titleContainer);
            holder.weather = row.findViewById(R.id.weather);
            holder.circle = row.findViewById(R.id.circle);
            holder.topLine = row.findViewById(R.id.topLine);
            holder.item_holder = row.findViewById(R.id.item_holder);
            row.setTag(holder);
        } else {
            holder = (ViewHolder)row.getTag();
        }

        if (mPrimaryColor == 0) {
            mPrimaryColor = new BaseConfig(context).getPrimaryColor();
        }
        holder.titleContainer.setBackgroundColor(mPrimaryColor);
//        GradientDrawable drawable = (GradientDrawable) holder.circle.getDrawable();
//        drawable.setColor(mPrimaryColor);

        setFontsTypeface(holder);

        DiaryDto diaryDto = list.get(position);
        if (position > 0 && StringUtils.equals(diaryDto.getDateString(), list.get(position - 1).getDateString())) {
            holder.titleContainer.setVisibility(View.GONE);
            holder.topLine.setVisibility(View.GONE);
            holder.weather.setImageResource(0);
        } else {
//            holder.title.setText(diaryDto.getDateString() + " " + DateUtils.timeMillisToDateTime(diaryDto.getCurrentTimeMillis(), "EEEE"));
            holder.title.setText(DateUtils.getFullPatternDate(diaryDto.getCurrentTimeMillis()));
            holder.titleContainer.setVisibility(View.VISIBLE);
            holder.topLine.setVisibility(View.VISIBLE);
            // 현재 날짜의 목록을 조회
            List<DiaryDto> mDiaryList = EasyDiaryDbHelper.readDiaryByDateString(diaryDto.getDateString());
            boolean initWeather = false;
            if (mDiaryList.size() > 0) {
                for (DiaryDto temp : mDiaryList) {
                    if (temp.getWeather() > 0) {
                        initWeather = true;
                        EasyDiaryUtils.initWeatherView(holder.weather, temp.getWeather());
                        break;
                    }
                }
                if (!initWeather) {
                    holder.weather.setVisibility(View.GONE);
                    holder.weather.setImageResource(0);
                }
            } else {
                holder.weather.setVisibility(View.GONE);
                holder.weather.setImageResource(0);
            }
        }

        holder.textView1.setText(DateUtils.timeMillisToDateTime(diaryDto.getCurrentTimeMillis(), DateUtils.TIME_PATTERN_WITH_SECONDS) + "\n" + getSummary(diaryDto));
        ContextKt.updateTextColors(context, holder.item_holder, 0, 0);
        ContextKt.initTextSize(context, holder.item_holder, context);
        return row;
    }

    private String getSummary(DiaryDto diaryDto) {
        String summary = null;
        if (StringUtils.isNotEmpty(diaryDto.getTitle())) {
            summary = diaryDto.getTitle();
        } else {
            summary = StringUtils.abbreviate(diaryDto.getContents(), 10);
        }
        return summary;
    }
    
    private void setFontsTypeface(ViewHolder holder) {
        FontUtils.setFontsTypeface(context, context.getAssets(), null, holder.textView1, holder.title);
    }

    private static class ViewHolder {
        TextView textView1;
        TextView title;
        View horizontalLine2;
        ViewGroup titleContainer;
        ImageView weather;
        ImageView circle;
        TextView topLine;
        LinearLayout item_holder;
    }
}
