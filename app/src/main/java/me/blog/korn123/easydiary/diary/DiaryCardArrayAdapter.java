package me.blog.korn123.easydiary.diary;

import android.app.Activity;
import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import me.blog.korn123.commons.constants.Constants;
import me.blog.korn123.commons.utils.CommonUtils;
import me.blog.korn123.commons.utils.DateUtils;
import me.blog.korn123.commons.utils.EasyDiaryUtils;
import me.blog.korn123.commons.utils.FontUtils;
import me.blog.korn123.easydiary.R;

/**
 * Created by CHO HANJOONG on 2017-03-16.
 */

public class DiaryCardArrayAdapter extends ArrayAdapter<DiaryDto> {
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

    public DiaryCardArrayAdapter(Context context, int layoutResourceId, List<DiaryDto> list) {
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
            holder.textView1 = ((TextView)row.findViewById(R.id.text1));
            holder.textView2 = ((TextView)row.findViewById(R.id.text2));
            holder.textView3 = ((TextView)row.findViewById(R.id.text3));
            holder.imageView = ((ImageView) row.findViewById(R.id.weather));
            row.setTag(holder);
        } else {
            holder = (ViewHolder)row.getTag();
        }

        initFontStyle(holder);
        float fontSize = CommonUtils.loadFloatPreference(context, "font_size", 0);
        if (fontSize > 0) {
            holder.textView1.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize);
            holder.textView2.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize);
            holder.textView3.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize);
        }

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
            EasyDiaryUtils.highlightString(holder.textView1, query);
            EasyDiaryUtils.highlightString(holder.textView2, query);
        }
        holder.textView3.setText(DateUtils.getFullPatternDateWithTime(diaryDto.getCurrentTimeMillis()));
        EasyDiaryUtils.initWeatherView(holder.imageView, diaryDto.getWeather());

        return row;
    }

    private void initFontStyle(ViewHolder holder) {
        FontUtils.setTypeface(context, context.getAssets(), holder.textView1);
        FontUtils.setTypeface(context, context.getAssets(), holder.textView2);
        FontUtils.setTypeface(context, context.getAssets(), holder.textView3);
    }

    private static class ViewHolder {
        TextView textView1;
        TextView textView2;
        TextView textView3;
        ImageView imageView;
    }
}
