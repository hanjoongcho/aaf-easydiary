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

import java.util.List;

import me.blog.korn123.commons.constants.Constants;
import me.blog.korn123.commons.utils.CommonUtils;
import me.blog.korn123.commons.utils.EasyDiaryUtils;
import me.blog.korn123.commons.utils.FontUtils;
import me.blog.korn123.easydiary.R;
import me.blog.korn123.easydiary.activities.DiaryInsertActivity;
import me.blog.korn123.easydiary.extensions.ContextKt;

/**
 * Created by CHO HANJOONG on 2017-03-16.
 */

public class DiaryWeatherItemAdapter extends ArrayAdapter<String> {
    private final Context mContext;
    private final List<String> mList;
    private final int mLayoutResourceId;

    public DiaryWeatherItemAdapter(Context context, int layoutResourceId, List<String> list) {
        super(context, layoutResourceId, list);
        this.mContext = context;
        this.mList = list;
        this.mLayoutResourceId = layoutResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return initRow(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return initRow(position, convertView, parent);
    }

    private View initRow(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder = null;
        if (row == null) {
            LayoutInflater inflater = ((Activity)this.mContext).getLayoutInflater();
            row = inflater.inflate(this.mLayoutResourceId, parent, false);
            holder = new ViewHolder();
            holder.textView1 = ((TextView)row.findViewById(R.id.text1));
            holder.imageView1 = ((ImageView) row.findViewById(R.id.imageView1));
            row.setTag(holder);
        } else {
            holder = (ViewHolder)row.getTag();
        }

        setFontsTypeface(holder);
        EasyDiaryUtils.initWeatherView(holder.imageView1, position, true);

        holder.textView1.setText(mList.get(position));
        if (position == 0) {
            holder.imageView1.setVisibility(View.GONE);
        } else {
            holder.imageView1.setVisibility(View.VISIBLE);
        }

        ContextKt.initTextSize(mContext, holder.textView1, mContext);
        return row;
    }

    private void setFontsTypeface(ViewHolder holder) {
        FontUtils.setFontsTypeface(mContext, mContext.getAssets(), null, holder.textView1);
    }

    private static class ViewHolder {
        TextView textView1;
        ImageView imageView1;
    }

}
