package me.blog.korn123.easydiary.diary;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

import org.apache.commons.lang3.StringUtils;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.blog.korn123.commons.constants.Constants;
import me.blog.korn123.commons.utils.BitmapUtils;
import me.blog.korn123.commons.utils.CommonUtils;
import me.blog.korn123.commons.utils.DateUtils;
import me.blog.korn123.commons.utils.DialogUtils;
import me.blog.korn123.commons.utils.EasyDiaryUtils;
import me.blog.korn123.commons.utils.FontUtils;
import me.blog.korn123.easydiary.R;
import me.blog.korn123.easydiary.helper.EasyDiaryActivity;
import me.blog.korn123.easydiary.photo.PhotoViewPagerActivity;
import me.blog.korn123.easydiary.setting.SettingsActivity;

/**
 * Created by CHO HANJOONG on 2017-03-16.
 */

public class ReadDiaryDetailActivity extends EasyDiaryActivity {

    private TextToSpeech mTextToSpeech;
    private long mCurrentTimeMillis;

    @BindView(R.id.container)
    ViewPager mViewPager;

    @BindView(R.id.edit)
    ImageView mEdit;

    @BindView(R.id.speechOutButton)
    ImageView mSpeechOutButton;

    @BindView(R.id.postCard)
    ImageView mPostCard;

    @BindView(R.id.delete)
    ImageView mDelete;


    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    public void onCreate(Bundle savedInstanceState) {
        // fixme elegance start    =============================================================================
        // activity destroy 시 저장된 savedInstance 값이 전달되면 갱신된 fragment 접근이 안됨
        // super.onCreate(savedInstanceState);
        super.onCreate(null);

        final int startPageIndex;
        // init viewpager
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), getIntent().getStringExtra("query"));
        if (savedInstanceState == null) {
            startPageIndex = mSectionsPagerAdapter.sequenceToPageIndex(getIntent().getIntExtra("sequence", -1));
        } else {
            startPageIndex = mSectionsPagerAdapter.sequenceToPageIndex(savedInstanceState.getInt("sequence", -1));
        }
        // fixme elegance end      =============================================================================

        setContentView(R.layout.activity_read_diary_detail);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.read_diary_detail_title));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FontUtils.setToolbarTypeface(toolbar, Typeface.DEFAULT);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                PlaceholderFragment fragment = mSectionsPagerAdapter.getFragment(mViewPager.getCurrentItem());
//                Log.i("determine", String.valueOf(fragment.getActivity()));
                if (fragment.getActivity() != null) {
                    fragment.setDiaryTypeface();
                    fragment.setDiaryFontSize();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                mViewPager.setCurrentItem(startPageIndex, false);
            }
        });

        initShowcase();
    }

    private int showcaseIndex = 2;
    ShowcaseView mShowcaseView;

    private void initShowcase() {
        int margin = ((Number) (getResources().getDisplayMetrics().density * 12)).intValue();

        final RelativeLayout.LayoutParams centerParams =
                new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        centerParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        centerParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        centerParams.setMargins(0, 0, 0, margin);

        View.OnClickListener showcaseViewOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (showcaseIndex) {
                    case 2:
                        mShowcaseView.setButtonPosition(centerParams);
                        mShowcaseView.setShowcase(new ViewTarget(mEdit), true);
                        mShowcaseView.setContentTitle(getString(R.string.read_diary_detail_showcase_title_2));
                        mShowcaseView.setContentText(getString(R.string.read_diary_detail_showcase_message_2));
                        break;
                    case 3:
                        mShowcaseView.setButtonPosition(centerParams);
                        mShowcaseView.setShowcase(new ViewTarget(mSpeechOutButton), true);
                        mShowcaseView.setContentTitle(getString(R.string.read_diary_detail_showcase_title_3));
                        mShowcaseView.setContentText(getString(R.string.read_diary_detail_showcase_message_3));
                        break;
                    case 4:
                        mShowcaseView.setButtonPosition(centerParams);
                        mShowcaseView.setShowcase(new ViewTarget(mPostCard), true);
                        mShowcaseView.setContentTitle(getString(R.string.read_diary_detail_showcase_title_4));
                        mShowcaseView.setContentText(getString(R.string.read_diary_detail_showcase_message_4));
                        break;
                    case 5:
                        mShowcaseView.setButtonPosition(centerParams);
                        mShowcaseView.setShowcase(new ViewTarget(mDelete), true);
                        mShowcaseView.setContentTitle(getString(R.string.read_diary_detail_showcase_title_5));
                        mShowcaseView.setContentText(getString(R.string.read_diary_detail_showcase_message_5));
                        mShowcaseView.setButtonText(getString(R.string.read_diary_detail_showcase_button_2));
                        break;
                    case 6:
                        mShowcaseView.hide();
                        break;
                }
                showcaseIndex++;
            }
        };

        mShowcaseView = new ShowcaseView.Builder(this)
                .withMaterialShowcase()
                .setTarget(new ViewTarget(mViewPager))
                .setContentTitle(getString(R.string.read_diary_detail_showcase_title_1))
                .setContentText(getString(R.string.read_diary_detail_showcase_message_1))
                .setStyle(R.style.ShowcaseTheme)
//                .singleShot(Constants.SHOWCASE_SINGLE_SHOT_CREATE_DIARY_NUMBER)
                .setOnClickListener(showcaseViewOnClickListener)
                .build();
        mShowcaseView.setButtonText(getString(R.string.read_diary_detail_showcase_button_1));
        mShowcaseView.setButtonPosition(centerParams);
    }

    private void initModule() {
        mTextToSpeech = new TextToSpeech(ReadDiaryDetailActivity.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    mTextToSpeech.setLanguage(Locale.getDefault());
                    mTextToSpeech.setPitch(1.3f);
                    mTextToSpeech.setSpeechRate(1f);
                }
            }
        });
    }

    private void destroyModule() {
        if (mTextToSpeech != null) {
            mTextToSpeech.stop();
            mTextToSpeech.shutdown();
            mTextToSpeech = null;
        }
    }

    private void textToSpeech(String text) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ttsGreater21(String.valueOf(text));
        } else {
            ttsUnder20(String.valueOf(text));
        }
    }

    @SuppressWarnings("deprecation")
    private void ttsUnder20(String text) {
        HashMap<String, String> map = new HashMap<>();
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "MessageId");

        mTextToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onError(String utteranceId) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onDone(String utteranceId) {
                // TODO Auto-generated method stub
            }
        });

        mTextToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, map);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void ttsGreater21(String text) {
        String utteranceId = this.hashCode() + "";
        mTextToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
    }

    @Override
    protected void onPause() {
        super.onPause();
        destroyModule();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        PlaceholderFragment fragment = mSectionsPagerAdapter.getFragment(mViewPager.getCurrentItem());
        outState.putInt("sequence", fragment.mSequence);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initModule();
    }

    @OnClick({R.id.zoomIn, R.id.zoomOut, R.id.delete, R.id.edit, R.id.speechOutButton, R.id.postCard})
    public void onClick(View view) {

//        ViewGroup viewPagerRootView = (ViewGroup) mViewPager.getChildAt(0);
//        mViewPager.setCurrentItem(2);
//        float fontSize = ((TextView) viewPagerRootView.findViewById(R.id.title)).getTextSize();
        final PlaceholderFragment fragment = mSectionsPagerAdapter.getFragment(mViewPager.getCurrentItem());
        float fontSize = fragment.mTitle.getTextSize();

        switch(view.getId()) {
            case R.id.zoomIn:
                CommonUtils.saveFloatPreference(ReadDiaryDetailActivity.this, "font_size", fontSize + 5);
                fragment.setDiaryFontSize();
                break;
            case R.id.zoomOut:
                CommonUtils.saveFloatPreference(ReadDiaryDetailActivity.this, "font_size", fontSize - 5);
                fragment.setDiaryFontSize();
                break;
            case R.id.delete:
                DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        DiaryDao.deleteDiary(fragment.mSequence);
                        finish();
                    }
                };
                DialogInterface.OnClickListener negativeListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                };
                DialogUtils.showAlertDialog(ReadDiaryDetailActivity.this, getString(R.string.delete_confirm), positiveListener, negativeListener);
                break;
            case R.id.edit:
                Intent updateDiaryIntent = new Intent(ReadDiaryDetailActivity.this, UpdateDiaryActivity.class);
                updateDiaryIntent.putExtra("sequence", fragment.mSequence);
                startActivity(updateDiaryIntent);
                finish();
                break;
            case R.id.speechOutButton:
                textToSpeech(fragment.mContents.getText().toString());
                break;
            case R.id.postCard:
                Intent postCardIntent = new Intent(ReadDiaryDetailActivity.this, PostCardActivity.class);
                postCardIntent.putExtra("sequence", fragment.mSequence);
                startActivityForResult(postCardIntent, Constants.REQUEST_CODE_BACKGROUND_COLOR_PICKER);
                break;
        }
    }



    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case android.R.id.home:
                finish();
//                this.overridePendingTransition(R.anim.anim_left_to_center, R.anim.anim_center_to_right);
                break;
            case R.id.action_settings:
                Intent settingIntent = new Intent(ReadDiaryDetailActivity.this, SettingsActivity.class);
                startActivity(settingIntent);
                break;
//            case R.id.toolbarToggle:
//                if (mSubToolbar.getVisibility() == View.GONE) {
//                    mSubToolbar.setVisibility(View.VISIBLE);
//                } else if (mSubToolbar.getVisibility() == View.VISIBLE) {
//                    mSubToolbar.setVisibility(View.GONE);
//                }
//                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String DIARY_SEQUENCE = "diary_sequence";
        private static final String DIARY_SEARCH_QUERY = "diary_search_query";
        private int mSequence;

        @BindView(R.id.contents)
        TextView mContents;

        @BindView(R.id.title)
        TextView mTitle;

        @BindView(R.id.date)
        TextView mDate;

        @BindView(R.id.weather)
        ImageView mWeather;

        @BindView(R.id.photoContainer)
        ViewGroup mPhotoContainer;

        @BindView(R.id.photoContainerScrollView)
        HorizontalScrollView mHorizontalScrollView;

        public PlaceholderFragment() {}

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sequence, String query) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(DIARY_SEQUENCE, sequence);
            args.putString(DIARY_SEARCH_QUERY, query);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            // bind view
            View rootView = inflater.inflate(R.layout.fragment_read_diary_detail, container, false);
            mContents = (TextView) rootView.findViewById(R.id.contents);
            mTitle = (TextView) rootView.findViewById(R.id.title);
            mDate = (TextView) rootView.findViewById(R.id.date);
            mWeather = (ImageView) rootView.findViewById(R.id.weather);
            ViewGroup mPhotoContainer = (ViewGroup) rootView.findViewById(R.id.photoContainer);
            HorizontalScrollView mHorizontalScrollView = (HorizontalScrollView) rootView.findViewById(R.id.photoContainerScrollView);

            mSequence = getArguments().getInt(DIARY_SEQUENCE);
            DiaryDto diaryDto = DiaryDao.readDiaryBy(mSequence);
            if (StringUtils.isEmpty(diaryDto.getTitle())) {
                mTitle.setVisibility(View.GONE);
            }
            mTitle.setText(diaryDto.getTitle());
            mContents.setText(diaryDto.getContents());
            mDate.setText(DateUtils.getFullPatternDateWithTime(diaryDto.getCurrentTimeMillis()));

            String query = getArguments().getString(DIARY_SEARCH_QUERY);
            if (StringUtils.isNotEmpty(query)) {
                EasyDiaryUtils.highlightString(mTitle, query);
                EasyDiaryUtils.highlightString(mContents, query);
            }

            int weather = diaryDto.getWeather();
            EasyDiaryUtils.initWeatherView(mWeather, weather);

            // TODO fixme elegance
            if (diaryDto.getPhotoUris() != null && diaryDto.getPhotoUris().size() > 0) {
                View.OnClickListener onClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent photoViewPager = new Intent(getContext(), PhotoViewPagerActivity.class);
                        photoViewPager.putExtra("sequence", mSequence);
                        startActivity(photoViewPager);
                    }
                };

                for (PhotoUriDto dto : diaryDto.getPhotoUris()) {
                    Uri uri = Uri.parse(dto.getPhotoUri());
                    Bitmap bitmap = null;
                    try {
                        bitmap = BitmapUtils.decodeUri(getContext(), uri, CommonUtils.dpToPixel(getContext(), 70, 1), CommonUtils.dpToPixel(getContext(), 60, 1), CommonUtils.dpToPixel(getContext(), 40, 1));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.question_mark_4);
                    }
                    ImageView imageView = new ImageView(getContext());
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(CommonUtils.dpToPixel(getContext(), 70, 1), CommonUtils.dpToPixel(getContext(), 50, 1));
                    layoutParams.setMargins(0, 0, CommonUtils.dpToPixel(getContext(), 3, 1), 0);
                    imageView.setLayoutParams(layoutParams);
                    imageView.setBackgroundResource(R.drawable.bg_card_01);
                    imageView.setImageBitmap(bitmap);
                    imageView.setScaleType(ImageView.ScaleType.CENTER);
                    mPhotoContainer.addView(imageView);
                    imageView.setOnClickListener(onClickListener);
                }
            } else {
                mHorizontalScrollView.setVisibility(View.GONE);
            }
            return rootView;
        }

        @Override
        public void onResume() {
            super.onResume();
            setDiaryTypeface();
            setDiaryFontSize();
        }

        private void setDiaryTypeface() {
            FontUtils.setTypeface(getContext(), getActivity().getAssets(), mTitle);
            FontUtils.setTypeface(getContext(), getActivity().getAssets(), mDate);
            FontUtils.setTypeface(getContext(), getActivity().getAssets(), mContents);
        }

        private void setDiaryFontSize() {
            float fontSize = CommonUtils.loadFloatPreference(getContext(), "font_size", 0);
            if (fontSize > 0) {
                mTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize);
                mDate.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize);
                mContents.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize);
            }
        }

    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private List<DiaryDto> mDiaryList;
        private List<PlaceholderFragment> mFragmentList = new ArrayList<>();

        public SectionsPagerAdapter(FragmentManager fm, String query) {
            super(fm);
            this.mDiaryList = DiaryDao.readDiary(query);
            for (DiaryDto diaryDto : mDiaryList) {
                mFragmentList.add(PlaceholderFragment.newInstance(diaryDto.getSequence(), query));
            }
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return mFragmentList.get(position);
        }

        public PlaceholderFragment getFragment(int position) {
            return mFragmentList.get(position);
        }

        public int sequenceToPageIndex(int sequence) {
            int pageIndex = 0;
            if (sequence > -1) {
                for (int i = 0; i < mDiaryList.size(); i++) {
                    if (mDiaryList.get(i).getSequence() == sequence) {
                        pageIndex = i;
                        break;
                    }
                }
            }
            return pageIndex;
        }

        @Override
        public int getCount() {
            return mDiaryList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return null;
        }
    }

}
