package me.blog.korn123.easydiary.adapters;

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
import me.blog.korn123.commons.utils.EasyDiaryUtils;
import me.blog.korn123.commons.utils.FontUtils;
import me.blog.korn123.easydiary.R;
import me.blog.korn123.easydiary.activities.PostCardActivity;
import me.blog.korn123.easydiary.models.DiaryDto;

/**
 * Created by CHO HANJOONG on 2017-03-16.
 */

public class DiaryCalendarItemAdapter extends ArrayAdapter<DiaryDto> {
    private final Context context;
    private final List<DiaryDto> list;
    private final int layoutResourceId;

    public DiaryCalendarItemAdapter(Context context, int layoutResourceId, List<DiaryDto> list) {
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

        setFontsTypeface(holder);
        setFontsSize(holder);

        DiaryDto diaryDto = (DiaryDto)this.list.get(position);
        if (StringUtils.isNotEmpty(diaryDto.getTitle())) {
            holder.textView1.setText(diaryDto.getTitle());
        } else {
            holder.textView1.setText(StringUtils.split(diaryDto.getContents(), "\n")[0]);
        }
        EasyDiaryUtils.initWeatherView(holder.imageView, diaryDto.getWeather());

        return row;
    }

    private void setFontsTypeface(ViewHolder holder) {
        FontUtils.setFontsTypeface(context, context.getAssets(), null, holder.textView1);
    }

    private void setFontsSize(ViewHolder holder) {
        float commonSize = CommonUtils.loadFloatPreference(context, Constants.SETTING_FONT_SIZE, holder.textView1.getTextSize());
        FontUtils.setFontsSize(commonSize, -1, holder.textView1);
    }

    private static class ViewHolder {
        TextView textView1;
        ImageView imageView;
    }
}
