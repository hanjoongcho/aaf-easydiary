package me.blog.korn123.easydiary.calendar;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.blog.korn123.commons.utils.DateUtils;
import me.blog.korn123.commons.utils.FontUtils;
import me.blog.korn123.easydiary.R;
import me.blog.korn123.easydiary.diary.DiaryDao;
import me.blog.korn123.easydiary.diary.DiaryDto;
import me.blog.korn123.easydiary.diary.DiarySimpleCardArrayAdapter;
import me.blog.korn123.easydiary.diary.ReadDiaryDetailActivity;
import me.blog.korn123.easydiary.helper.EasyDiaryActivity;

/**
 * Created by hanjoong on 2017-03-28.
 */

public class CalendarActivity extends EasyDiaryActivity {
    private CaldroidFragment caldroidFragment;

    @BindView(R.id.selectedList)
    ListView mSelectedListView;

    @BindView(R.id.emptyInfo)
    TextView mEmptyInfo;

    private ArrayAdapter<DiaryDto> mArrayAdapterDiary;
    private List<DiaryDto> mDiaryList;
    private Date mCurrentDate;

    private Date getCurrentDate() {
        if (mCurrentDate == null) mCurrentDate = Calendar.getInstance().getTime();
        return mCurrentDate;
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshList(getCurrentDate());
        caldroidFragment.refreshView();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.calendar_title));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        FontUtils.setToolbarTypeface(toolbar, Typeface.DEFAULT);
        FontUtils.setTypefaceDefault(mEmptyInfo);

        Calendar cal = Calendar.getInstance();
        Date currentDate = cal.getTime();
        refreshList(currentDate);
        mArrayAdapterDiary = new DiarySimpleCardArrayAdapter(this, R.layout.list_item_diary_simple_card_array_adapter , this.mDiaryList);
        mSelectedListView.setAdapter(mArrayAdapterDiary);
        mSelectedListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                DiaryDto diaryDto = (DiaryDto)adapterView.getAdapter().getItem(i);
                Intent detailIntent = new Intent(CalendarActivity.this, ReadDiaryDetailActivity.class);
                detailIntent.putExtra("sequence", diaryDto.getSequence());
                detailIntent.putExtra("title", diaryDto.getTitle());
                detailIntent.putExtra("contents", diaryDto.getContents());
                detailIntent.putExtra("date", DateUtils.timeMillisToDateTime(diaryDto.getCurrentTimeMillis()));
                detailIntent.putExtra("current_time_millis", diaryDto.getCurrentTimeMillis());
                detailIntent.putExtra("weather", diaryDto.getWeather());
                startActivity(detailIntent);
            }
        });

        // Setup caldroid fragment
        // **** If you want normal CaldroidFragment, use below line ****
//        caldroidFragment = new CaldroidFragment();

        // //////////////////////////////////////////////////////////////////////
        // **** This is to show customized fragment. If you want customized
        // version, uncomment below line ****
        caldroidFragment = new CaldroidCustomFragment();

        // Setup arguments

        // If Activity is created after rotation
        if (savedInstanceState != null) {
            caldroidFragment.restoreStatesFromKey(savedInstanceState,
                    "CALDROID_SAVED_STATE");
        }
        // If activity is created from fresh
        else {
            Bundle args = new Bundle();
            args.putInt(CaldroidFragment.MONTH, cal.get(Calendar.MONTH) + 1);
            args.putInt(CaldroidFragment.YEAR, cal.get(Calendar.YEAR));
            args.putBoolean(CaldroidFragment.ENABLE_SWIPE, true);
            args.putBoolean(CaldroidFragment.SIX_WEEKS_IN_CALENDAR, true);

            // Uncomment this to customize startDayOfWeek
            // args.putInt(CaldroidFragment.START_DAY_OF_WEEK,
            // CaldroidFragment.TUESDAY); // Tuesday

            // Uncomment this line to use Caldroid in compact mode
            // args.putBoolean(CaldroidFragment.SQUARE_TEXT_VIEW_CELL, false);

            // Uncomment this line to use dark theme
//            args.putInt(CaldroidFragment.THEME_RESOURCE, com.caldroid.R.style.CaldroidDefaultDark);

            caldroidFragment.setArguments(args);
        }

        caldroidFragment.setSelectedDate(currentDate);

//        setCustomResourceForDates();

        // Attach to the activity
        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.replace(R.id.calendar1, caldroidFragment);
        t.commit();

        // Setup listener
        final CaldroidListener listener = new CaldroidListener() {

            @Override
            public void onSelectDate(Date date, View view) {
//                Toast.makeText(getApplicationContext(), formatter.format(date),
//                        Toast.LENGTH_SHORT).show();
//                ColorDrawable green = new ColorDrawable(Color.GREEN);
//                caldroidFragment.setBackgroundDrawableForDate(green, date);
                caldroidFragment.clearSelectedDates();
                caldroidFragment.setSelectedDate(date);
                caldroidFragment.refreshView();
                mCurrentDate = date;
                refreshList(date);
            }

            @Override
            public void onChangeMonth(int month, int year) {
                String text = "month: " + month + " year: " + year;
//                Toast.makeText(getApplicationContext(), text,
//                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLongClickDate(Date date, View view) {
//                Toast.makeText(getApplicationContext(),
//                        "Long click " + formatter.format(date),
//                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCaldroidViewCreated() {
                if (caldroidFragment.getLeftArrowButton() != null) {
//                    Toast.makeText(getApplicationContext(),
//                            "Caldroid view is created", Toast.LENGTH_SHORT)
//                            .show();
                }
            }

        };

        // Setup Caldroid
        caldroidFragment.setCaldroidListener(listener);
    }

    /**
     * Save current states of the Caldroid here
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // TODO Auto-generated method stub
        super.onSaveInstanceState(outState);

        if (caldroidFragment != null) {
            caldroidFragment.saveStatesToKey(outState, "CALDROID_SAVED_STATE");
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void refreshList(Date date) {
        final SimpleDateFormat formatter = new SimpleDateFormat(DateUtils.DATE_PATTERN_DASH);

        if (mDiaryList == null) {
            mDiaryList = DiaryDao.readDiaryByDateString(formatter.format(date));
        } else if (mDiaryList != null) {
            mDiaryList.clear();
            mDiaryList.addAll(DiaryDao.readDiaryByDateString(formatter.format(date)));
            mArrayAdapterDiary.notifyDataSetChanged();
        }

        if (mDiaryList.size() > 0) {
            mSelectedListView.setVisibility(View.VISIBLE);
            mEmptyInfo.setVisibility(View.GONE);
        } else {
            mSelectedListView.setVisibility(View.GONE);
            mEmptyInfo.setVisibility(View.VISIBLE);
        }
    }

    //    private void setCustomResourceForDates() {
//        Calendar cal = Calendar.getInstance();
//
//        Date currentDate = cal.getTime();
//
//        // Min date is last 7 days
//        cal.add(Calendar.DATE, -7);
//        Date blueDate = cal.getTime();
//
//        // Max date is next 7 days
//        cal = Calendar.getInstance();
//        cal.add(Calendar.DATE, 7);
//        Date greenDate = cal.getTime();
//
//        if (caldroidFragment != null) {
//            ColorDrawable blue = new ColorDrawable(getResources().getColor(R.color.blue));
//            ColorDrawable green = new ColorDrawable(Color.GREEN);
//            caldroidFragment.setBackgroundDrawableForDate(blue, blueDate);
//            caldroidFragment.setBackgroundDrawableForDate(green, greenDate);
//            caldroidFragment.setTextColorForDate(R.color.white, blueDate);
//            caldroidFragment.setTextColorForDate(R.color.white, greenDate);
//        }
//    }

}
