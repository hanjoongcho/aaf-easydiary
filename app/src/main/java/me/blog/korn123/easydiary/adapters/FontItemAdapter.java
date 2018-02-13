package me.blog.korn123.easydiary.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;
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
import me.blog.korn123.commons.utils.FontUtils;
import me.blog.korn123.easydiary.R;

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
            holder.textView = (TextView) row.findViewById(R.id.textView);
            holder.imageView = (ImageView) row.findViewById(R.id.checkIcon);
            row.setTag(holder);
        } else {
            holder = (ViewHolder)row.getTag();
        }

        if (StringUtils.equals(CommonUtils.loadStringPreference(context, Constants.SETTING_FONT_NAME, Constants.CUSTOM_FONTS_SUPPORTED_LANGUAGE_DEFAULT), list.get(position).get("fontName"))) {
            holder.imageView.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.check_mark));
        } else {
            holder.imageView.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.check_mark_off));
        }
        holder.textView.setText(list.get(position).get("disPlayFontName"));
        holder.textView.setTypeface(FontUtils.getTypeface(context, context.getAssets(), list.get(position).get("fontName")));

        return row;
    }

    private static class ViewHolder {
        TextView textView;
        ImageView imageView;
    }
}
