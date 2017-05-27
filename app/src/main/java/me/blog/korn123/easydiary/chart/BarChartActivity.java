package me.blog.korn123.easydiary.chart;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.blog.korn123.commons.utils.DateUtils;
import me.blog.korn123.commons.utils.FontUtils;
import me.blog.korn123.easydiary.R;
import me.blog.korn123.easydiary.diary.DiaryDao;
import me.blog.korn123.easydiary.diary.DiaryDto;

/**
 * Created by CHO HANJOONG on 2017-03-16.
 */

public class BarChartActivity extends ChartBase {

    protected BarChart mChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_barchart);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.bar_chart_title));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mChart = (BarChart) findViewById(R.id.chart1);

        FontUtils.setToolbarTypeface(toolbar, Typeface.DEFAULT);

        mChart.setDrawBarShadow(false);
        mChart.setDrawValueAboveBar(true);

        mChart.getDescription().setEnabled(false);

        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        mChart.setMaxVisibleValueCount(60);

        // scaling can now only be done on x- and y-axis separately
        mChart.setPinchZoom(false);

        mChart.setDrawGridBackground(false);
        // mChart.setDrawYLabels(false);

        IAxisValueFormatter xAxisFormatter = new DayAxisValueFormatter(this, mChart);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTypeface(mTfLight);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f); // only intervals of 1 day
        xAxis.setLabelCount(7);
        xAxis.setValueFormatter(xAxisFormatter);

        IAxisValueFormatter custom = new MyAxisValueFormatter(this);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setTypeface(mTfLight);
        leftAxis.setLabelCount(8, false);
        leftAxis.setValueFormatter(custom);
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setSpaceTop(15f);
        leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setTypeface(mTfLight);
        rightAxis.setLabelCount(8, false);
        rightAxis.setValueFormatter(custom);
        rightAxis.setSpaceTop(15f);
        rightAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        Legend l = mChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setForm(Legend.LegendForm.SQUARE);
        l.setFormSize(9f);
        l.setTextSize(11f);
        l.setXEntrySpace(4f);
        // l.setExtra(ColorTemplate.VORDIPLOM_COLORS, new String[] { "abc",
        // "def", "ghj", "ikl", "mno" });
        // l.setCustom(ColorTemplate.VORDIPLOM_COLORS, new String[] { "abc",
        // "def", "ghj", "ikl", "mno" });

        XYMarkerView mv = new XYMarkerView(this, xAxisFormatter);
        mv.setChartView(mChart); // For bounds control
        mChart.setMarker(mv); // Set the marker to the chart

        setData(6, 20);
    }

    private void setData(int count, float range) {

        List<DiaryDto> listDiary = DiaryDao.readDiary(null);
        Map<Integer, Integer> map = new HashMap<>();
        for (DiaryDto diaryDto : listDiary) {
            String writeHour = DateUtils.timeMillisToHour(diaryDto.getCurrentTimeMillis());
            int itemNumber = hourToItemNumber(Integer.parseInt(writeHour));
            if (map.get(itemNumber) == null) {
                map.put(itemNumber, 1);
            } else {
                map.put(itemNumber, map.get(itemNumber) + 1);
            }
        }

        ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();
        for (int i = 1; i <= count; i++) {
            int total = 0;
            if (map.get(i) != null) total = map.get(i);
            yVals1.add(new BarEntry(i, total));
        }

        BarDataSet set1;

        set1 = new BarDataSet(yVals1, getString(R.string.bar_chart_status));
        IValueFormatter iValueFormatter = new IValueFormatterExt(this);
        set1.setValueFormatter(iValueFormatter);
//            set1.setDrawIcons(false);

        set1.setColors(ColorTemplate.MATERIAL_COLORS);

        ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
        dataSets.add(set1);

        BarData data = new BarData(dataSets);
        data.setValueTextSize(10f);
        data.setValueTypeface(mTfLight);
        data.setBarWidth(0.9f);

        mChart.setData(data);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public String itemNumberToRange(int itemNumber) {
        String hourRange = null;
        switch (itemNumber) {
            case 1:
                hourRange = getString(R.string.range_a);
                break;
            case 2:
                hourRange = getString(R.string.range_b);
                break;
            case 3:
                hourRange = getString(R.string.range_c);
                break;
            case 4:
                hourRange = getString(R.string.range_d);
                break;
            case 5:
                hourRange = getString(R.string.range_e);
                break;
            case 6:
                hourRange = getString(R.string.range_f);
                break;
            default:
                hourRange = getString(R.string.range_g);
                break;

        }
        return hourRange;
    }

    public static int hourToItemNumber(int hour) {
        int itemNumber = 0;
        if (hour >= 0 && hour < 4) {
            itemNumber = 1;
        } else if (hour >= 4 && hour < 8) {
            itemNumber = 2;
        } else if (hour >= 8 && hour < 12) {
            itemNumber = 3;
        } else if (hour >= 12 && hour < 16) {
            itemNumber = 4;
        } else if (hour >= 16 && hour < 20) {
            itemNumber = 5;
        } else if (hour >= 20 && hour < 24) {
            itemNumber = 6;
        }
        return itemNumber;
    }

}
