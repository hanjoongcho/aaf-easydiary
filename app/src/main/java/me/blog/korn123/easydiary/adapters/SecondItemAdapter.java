package me.blog.korn123.easydiary.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

import me.blog.korn123.easydiary.R;

/**
 * Created by CHO HANJOONG on 2017-03-16.
 */

public class SecondItemAdapter extends ArrayAdapter<Map<String, String>> {
    private final Context context;
    private final List<Map<String, String>> list;
    private final int layoutResourceId;
    private int mSecond;

    public SecondItemAdapter(Context context, int layoutResourceId, List<Map<String, String>> list, int second) {
        super(context, layoutResourceId, list);
        this.context = context;
        this.list = list;
        this.layoutResourceId = layoutResourceId;
        this.mSecond = second;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder = null;
        if (row == null) {
            LayoutInflater inflater = ((Activity)this.context).getLayoutInflater();
            row = inflater.inflate(this.layoutResourceId, parent, false);
            holder = new ViewHolder();
            holder.textView = (TextView) row.findViewById(R.id.textView);
            row.setTag(holder);
        } else {
            holder = (ViewHolder)row.getTag();
        }

        holder.textView.setText(list.get(position).get("label"));
        if (position == mSecond) {
            holder.textView.setTextColor(context.getResources().getColor(R.color.colorPrimary));
        } else {
            holder.textView.setTextColor(context.getResources().getColor(R.color.summaryText));
        }
        return row;
    }

    private static class ViewHolder {
        TextView textView;
    }

}
