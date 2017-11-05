package me.blog.korn123.easydiary.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

import me.blog.korn123.commons.constants.Constants;
import me.blog.korn123.commons.utils.CommonUtils;
import me.blog.korn123.commons.utils.EasyDiaryUtils;
import me.blog.korn123.commons.utils.FontUtils;
import me.blog.korn123.easydiary.R;
import me.blog.korn123.easydiary.models.DiaryDto;

/**
 * Created by CHO HANJOONG on 2017-03-16.
 */

public class FontItemAdapter extends ArrayAdapter<Map<String, String>> {
    private final Context context;
    private final List<Map<String, String>> list;
    private final int layoutResourceId;

    public FontItemAdapter(Context context, int layoutResourceId, List<Map<String, String>> list) {
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
            holder.textView = ((TextView)row.findViewById(R.id.textView));
            row.setTag(holder);
        } else {
            holder = (ViewHolder)row.getTag();
        }

        holder.textView.setText(list.get(position).get("disPlayFontName"));
        holder.textView.setTypeface(FontUtils.getTypeface(context, context.getAssets(), list.get(position).get("fontName")));

        return row;
    }

//    private void setFontsSize(ViewHolder holder) {
//        float commonSize = CommonUtils.loadFloatPreference(context, Constants.SETTING_FONT_SIZE, holder.textView1.getTextSize());
//        FontUtils.setFontsSize(commonSize, -1, holder.textView1);
//    }

    private static class ViewHolder {
        TextView textView;
    }

}
