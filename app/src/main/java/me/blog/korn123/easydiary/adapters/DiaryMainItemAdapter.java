package me.blog.korn123.easydiary.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import me.blog.korn123.easydiary.extensions.ContextKt;
import me.blog.korn123.easydiary.extensions.ContextKt.*;
import io.github.hanjoongcho.commons.helpers.BaseConfig;
import me.blog.korn123.commons.constants.Constants;
import me.blog.korn123.commons.utils.CommonUtils;
import me.blog.korn123.commons.utils.DateUtils;
import me.blog.korn123.commons.utils.EasyDiaryUtils;
import me.blog.korn123.commons.utils.FontUtils;
import me.blog.korn123.easydiary.R;
import me.blog.korn123.easydiary.models.DiaryDto;

/**
 * Created by CHO HANJOONG on 2017-03-16.
 */

public class DiaryMainItemAdapter extends ArrayAdapter<DiaryDto> {
    private final Context context;
    private final List<DiaryDto> list;
    private final int layoutResourceId;
    private String query;

    public void setCurrentQuery(String query) {
        this.query = query;
    }

    public String getCurrentQuery() {
        return this.query;
    }

    public DiaryMainItemAdapter(Context context, int layoutResourceId, List<DiaryDto> list) {
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
            holder.textView2 = row.findViewById(R.id.text2);
            holder.textView3 = row.findViewById(R.id.text3);
            holder.imageView = row.findViewById(R.id.weather);
            holder.item_holder = row.findViewById(R.id.item_holder);
            row.setTag(holder);
        } else {
            holder = (ViewHolder)row.getTag();
        }

        setFontsTypeface(holder);
        setFontsSize(holder);

        DiaryDto diaryDto = (DiaryDto)this.list.get(position);
        if (StringUtils.isEmpty(diaryDto.getTitle())) {
            holder.textView1.setVisibility(View.GONE);
        } else {
            holder.textView1.setVisibility(View.VISIBLE);
        }
        holder.textView1.setText(diaryDto.getTitle());
        holder.textView2.setText(diaryDto.getContents());

        // highlight current query
        if (StringUtils.isNotEmpty(query)) {
            if (CommonUtils.loadBooleanPreference(getContext(), Constants.DIARY_SEARCH_QUERY_CASE_SENSITIVE)) {
                EasyDiaryUtils.highlightString(holder.textView1, query);
                EasyDiaryUtils.highlightString(holder.textView2, query);
            } else {
                EasyDiaryUtils.highlightStringIgnoreCase(holder.textView1, query);
                EasyDiaryUtils.highlightStringIgnoreCase(holder.textView2, query);
            }

        }
        holder.textView3.setText(DateUtils.getFullPatternDateWithTime(diaryDto.getCurrentTimeMillis()));
        EasyDiaryUtils.initWeatherView(holder.imageView, diaryDto.getWeather());

        GradientDrawable drawable = (GradientDrawable) holder.item_holder.getBackground();
        drawable.setColor(new BaseConfig(context).getCustomBackgroundColor());
        ContextKt.updateTextColors(context, holder.item_holder, 0, 0);
        return row;
    }

    private void setFontsTypeface(ViewHolder holder) {
        FontUtils.setFontsTypeface(context, context.getAssets(), null, holder.textView1, holder.textView2, holder.textView3);
    }

    private void setFontsSize(ViewHolder holder) {
        float commonSize = CommonUtils.loadFloatPreference(context, Constants.SETTING_FONT_SIZE, holder.textView1.getTextSize());
        FontUtils.setFontsSize(commonSize, -1, holder.textView1, holder.textView2, holder.textView3);
    }

    private static class ViewHolder {
        TextView textView1;
        TextView textView2;
        TextView textView3;
        ImageView imageView;
        LinearLayout item_holder;
    }
}
