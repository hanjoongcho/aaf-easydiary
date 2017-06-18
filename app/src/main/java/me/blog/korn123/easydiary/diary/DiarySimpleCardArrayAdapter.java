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

import java.util.List;

import me.blog.korn123.commons.constants.Constants;
import me.blog.korn123.commons.utils.CommonUtils;
import me.blog.korn123.commons.utils.EasyDiaryUtils;
import me.blog.korn123.commons.utils.FontUtils;
import me.blog.korn123.easydiary.R;

/**
 * Created by CHO HANJOONG on 2017-03-16.
 */

public class DiarySimpleCardArrayAdapter extends ArrayAdapter<DiaryDto> {
    private final Context context;
    private final List<DiaryDto> list;
    private final int layoutResourceId;

    public DiarySimpleCardArrayAdapter(Context context, int layoutResourceId, List<DiaryDto> list) {
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
            holder.imageView = ((ImageView) row.findViewById(R.id.weather));
            row.setTag(holder);
        } else {
            holder = (ViewHolder)row.getTag();
        }

        initFontStyle(holder);
        float fontSize = CommonUtils.loadFloatPreference(context, "font_size", 0);
        if (fontSize > 0) {
            holder.textView1.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize);
        }

        DiaryDto diaryDto = (DiaryDto)this.list.get(position);
        holder.textView1.setText(diaryDto.getTitle());
        EasyDiaryUtils.initWeatherView(holder.imageView, diaryDto.getWeather());

        return row;
    }

    private void initFontStyle(ViewHolder holder) {
        FontUtils.setTypeface(context, context.getAssets(), holder.textView1);
    }

    private static class ViewHolder {
        TextView textView1;
        ImageView imageView;
    }
}
