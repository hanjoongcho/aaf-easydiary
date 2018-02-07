package me.blog.korn123.easydiary.activities;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.graphics.ColorUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
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
import com.simplemobiletools.commons.helpers.BaseConfig;

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
import me.blog.korn123.easydiary.helper.TransitionHelper;
import me.blog.korn123.easydiary.models.PhotoUriDto;
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper;
import me.blog.korn123.easydiary.models.DiaryDto;

import static me.blog.korn123.commons.constants.Constants.THUMBNAIL_BACKGROUND_ALPHA;

/**
 * Created by CHO HANJOONG on 2017-03-16.
 */

public class DiaryReadActivity extends EasyDiaryActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    private TextToSpeech mTextToSpeech;

    private ShowcaseView mShowcaseView;

    @BindView(R.id.container)
    ViewPager mViewPager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // fixme elegance start    =============================================================================
        // activity destroy 시 저장된 savedInstance 값이 전달되면 갱신된 fragment 접근이 안됨
        // super.onCreate(savedInstanceState);
        super.onCreate(null);

        final int startPageIndex;
        // init viewpager
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), getIntent().getStringExtra(Constants.DIARY_SEARCH_QUERY));
        if (savedInstanceState == null) {
            startPageIndex = mSectionsPagerAdapter.sequenceToPageIndex(getIntent().getIntExtra(Constants.DIARY_SEQUENCE, -1));
        } else {
            startPageIndex = mSectionsPagerAdapter.sequenceToPageIndex(savedInstanceState.getInt(Constants.DIARY_SEQUENCE, -1));
        }
        // fixme elegance end      =============================================================================

        setContentView(R.layout.activity_diary_read);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
//        getSupportActionBar().setTitle(getString(R.string.read_diary_detail_title));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
                    fragment.setFontsTypeface();
                    fragment.setFontsSize();
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

    @Override
    protected void onPause() {
        super.onPause();
        destroyModule();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        PlaceholderFragment fragment = mSectionsPagerAdapter.getFragment(mViewPager.getCurrentItem());
        outState.putInt(Constants.DIARY_SEQUENCE, fragment.mSequence);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setFontsStyle();
        initModule();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final PlaceholderFragment fragment = mSectionsPagerAdapter.getFragment(mViewPager.getCurrentItem());
        float fontSize = fragment.mTitle.getTextSize();
        
        switch (item.getItemId()) {
            case android.R.id.home:
//                finish();
//                this.overridePendingTransition(R.anim.anim_left_to_center, R.anim.anim_center_to_right);
                this.onBackPressed();
                break;
            case R.id.zoomIn:
                CommonUtils.saveFloatPreference(DiaryReadActivity.this, Constants.SETTING_FONT_SIZE, fontSize + 5);
                setFontsStyle();
                fragment.setFontsSize();
                break;
            case R.id.zoomOut:
                CommonUtils.saveFloatPreference(DiaryReadActivity.this, Constants.SETTING_FONT_SIZE, fontSize - 5);
                setFontsStyle();
                fragment.setFontsSize();
                break;
            case R.id.delete:
                DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        EasyDiaryDbHelper.deleteDiary(fragment.mSequence);
                        finish();
                    }
                };
                DialogInterface.OnClickListener negativeListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                };
                DialogUtils.showAlertDialog(DiaryReadActivity.this, getString(R.string.delete_confirm), positiveListener, negativeListener);
                break;
            case R.id.edit:
                Intent updateDiaryIntent = new Intent(DiaryReadActivity.this, DiaryUpdateActivity.class);
                updateDiaryIntent.putExtra(Constants.DIARY_SEQUENCE, fragment.mSequence);
//                startActivity(updateDiaryIntent);
                TransitionHelper.startActivityWithTransition(DiaryReadActivity.this, updateDiaryIntent);
//                finish();
                break;
            case R.id.speechOutButton:
                textToSpeech(fragment.mContents.getText().toString());
                break;
            case R.id.postCard:
                Intent postCardIntent = new Intent(DiaryReadActivity.this, PostCardActivity.class);
                postCardIntent.putExtra(Constants.DIARY_SEQUENCE, fragment.mSequence);
//                startActivityForResult(postCardIntent, Constants.REQUEST_CODE_BACKGROUND_COLOR_PICKER);
                TransitionHelper.startActivityWithTransition(DiaryReadActivity.this, postCardIntent);
                break;
//            case R.id.action_settings:
//                Intent settingIntent = new Intent(DiaryReadActivity.this, SettingsActivity.class);
//                startActivity(settingIntent);
//                break;
//            case R.id.toolbarToggle:
//                if (mSubToolbar.getVisibility() == View.GONE) {
//                    mSubToolbar.setVisibility(View.VISIBLE);
//                } else if (mSubToolbar.getVisibility() == View.VISIBLE) {
//                    mSubToolbar.setVisibility(View.GONE);
//                }
//                break;
        }
//        return super.onOptionsItemSelected(item);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.diary_read, menu);
        return true;
    }

    private void setFontsStyle() {
        FontUtils.setFontsTypeface(getApplicationContext(), getAssets(), null, (ViewGroup) findViewById(android.R.id.content));
        float fontSize = CommonUtils.loadFloatPreference(this, Constants.SETTING_FONT_SIZE, -1);
        if (fontSize > 0) FontUtils.setFontsSize(fontSize, (ViewGroup) findViewById(android.R.id.content));
    }

    private int mShowcaseIndex = 1;
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
                switch (mShowcaseIndex) {
                    case 1:
                        mShowcaseView.setButtonPosition(centerParams);
                        mShowcaseView.setShowcase(new ViewTarget(mViewPager), false);
                        mShowcaseView.setContentTitle(getString(R.string.read_diary_detail_showcase_title_1));
                        mShowcaseView.setContentText(getString(R.string.read_diary_detail_showcase_message_1));
                        break;
                    case 2:
                        mShowcaseView.setButtonPosition(centerParams);
                        mShowcaseView.setTarget(new ViewTarget(R.id.edit, DiaryReadActivity.this));
                        mShowcaseView.setContentTitle(getString(R.string.read_diary_detail_showcase_title_2));
                        mShowcaseView.setContentText(getString(R.string.read_diary_detail_showcase_message_2));
                        break;
                    case 3:
                        mShowcaseView.setButtonPosition(centerParams);
                        mShowcaseView.setTarget(new ViewTarget(R.id.speechOutButton, DiaryReadActivity.this));
                        mShowcaseView.setContentTitle(getString(R.string.read_diary_detail_showcase_title_3));
                        mShowcaseView.setContentText(getString(R.string.read_diary_detail_showcase_message_3));
                        break;
                    case 4:
                        mShowcaseView.setButtonPosition(centerParams);
                        mShowcaseView.setTarget(new ViewTarget(R.id.postCard, DiaryReadActivity.this));
                        mShowcaseView.setContentTitle(getString(R.string.read_diary_detail_showcase_title_4));
                        mShowcaseView.setContentText(getString(R.string.read_diary_detail_showcase_message_4));
                        break;
                    case 5:
                        mShowcaseView.hide();
                        break;
                }
                mShowcaseIndex++;
            }
        };

        mShowcaseView = new ShowcaseView.Builder(this)
                .withMaterialShowcase()
                .setContentTitle(getString(R.string.read_diary_detail_showcase_title_0))
                .setContentText(getString(R.string.read_diary_detail_showcase_message_0))
                .setStyle(R.style.ShowcaseTheme)
                .singleShot(Constants.SHOWCASE_SINGLE_SHOT_READ_DIARY_DETAIL_NUMBER)
                .setOnClickListener(showcaseViewOnClickListener)
                .build();
        mShowcaseView.setButtonText(getString(R.string.read_diary_detail_showcase_button_1));
        mShowcaseView.setButtonPosition(centerParams);
    }

    private void initModule() {
        mTextToSpeech = new TextToSpeech(DiaryReadActivity.this, new TextToSpeech.OnInitListener() {
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

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        private int mSequence;
        private int mPrimaryColor = 0;

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
            args.putInt(Constants.DIARY_SEQUENCE, sequence);
            args.putString(Constants.DIARY_SEARCH_QUERY, query);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            // bind view
            View rootView = inflater.inflate(R.layout.fragment_diary_read, container, false);
            mContents = (TextView) rootView.findViewById(R.id.contents);
            mTitle = (TextView) rootView.findViewById(R.id.title);
            mDate = (TextView) rootView.findViewById(R.id.date);
            mWeather = (ImageView) rootView.findViewById(R.id.weather);
            mPhotoContainer = (ViewGroup) rootView.findViewById(R.id.photoContainer);
            mHorizontalScrollView = (HorizontalScrollView) rootView.findViewById(R.id.photoContainerScrollView);
            mSequence = getArguments().getInt(Constants.DIARY_SEQUENCE);
            return rootView;
        }

        @Override
        public void onResume() {
            super.onResume();

            initContents();
            initBottomContainer();
            setFontsTypeface();
            setFontsSize();
        }
        
        private void initContents() {
            DiaryDto diaryDto = EasyDiaryDbHelper.readDiaryBy(mSequence);
            if (StringUtils.isEmpty(diaryDto.getTitle())) {
                mTitle.setVisibility(View.GONE);
            }
            mTitle.setText(diaryDto.getTitle());
            mContents.setText(diaryDto.getContents());
            mDate.setText(DateUtils.getFullPatternDateWithTime(diaryDto.getCurrentTimeMillis()));
            initBottomContainer();

            String query = getArguments().getString(Constants.DIARY_SEARCH_QUERY);
            if (StringUtils.isNotEmpty(query)) {
                if (CommonUtils.loadBooleanPreference(getContext(), Constants.DIARY_SEARCH_QUERY_CASE_SENSITIVE)) {
                    EasyDiaryUtils.highlightString(mTitle, query);
                    EasyDiaryUtils.highlightString(mContents, query);
                } else {
                    EasyDiaryUtils.highlightStringIgnoreCase(mTitle, query);
                    EasyDiaryUtils.highlightStringIgnoreCase(mContents, query);
                }
            }

            int weather = diaryDto.getWeather();
            EasyDiaryUtils.initWeatherView(mWeather, weather);

            // TODO fixme elegance
            if (diaryDto.getPhotoUris() != null && diaryDto.getPhotoUris().size() > 0) {
                mHorizontalScrollView.setVisibility(View.VISIBLE);
                View.OnClickListener onClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent photoViewPager = new Intent(getContext(), PhotoViewPagerActivity.class);
                        photoViewPager.putExtra(Constants.DIARY_SEQUENCE, mSequence);
                        startActivity(photoViewPager);
                    }
                };

                if (mPhotoContainer.getChildCount() > 0) mPhotoContainer.removeAllViews();
                for (PhotoUriDto dto : diaryDto.getPhotoUris()) {
                    Uri uri = Uri.parse(dto.getPhotoUri());
                    Bitmap bitmap = null;
                    try {
                        bitmap = BitmapUtils.decodeUri(getContext(), uri, CommonUtils.dpToPixel(getContext(), 70, 1), CommonUtils.dpToPixel(getContext(), 65, 1), CommonUtils.dpToPixel(getContext(), 45, 1));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.question_shield);
//                        DialogUtils.makeSnackBar(mContents, e.getMessage());
                    } catch (SecurityException se) {
                        se.printStackTrace();
//                        DialogUtils.makeSnackBar(mContents, se.getMessage());
                        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.question_shield);
                    }
                    ImageView imageView = new ImageView(getContext());
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(CommonUtils.dpToPixel(getContext(), 70, 1), CommonUtils.dpToPixel(getContext(), 50, 1));
                    layoutParams.setMargins(0, 0, CommonUtils.dpToPixel(getContext(), 3, 1), 0);
                    imageView.setLayoutParams(layoutParams);
                    imageView.setBackgroundResource(R.drawable.bg_card_thumbnail);
                    Drawable drawable = getResources().getDrawable(R.drawable.bg_card_thumbnail);
                    GradientDrawable gradient = (GradientDrawable) drawable;
                    gradient.setColor(ColorUtils.setAlphaComponent(mPrimaryColor, THUMBNAIL_BACKGROUND_ALPHA));
                    imageView.setBackground(gradient);
                    imageView.setImageBitmap(bitmap);
                    imageView.setScaleType(ImageView.ScaleType.CENTER);
                    mPhotoContainer.addView(imageView);
                    imageView.setOnClickListener(onClickListener);
                }
            } else {
                mHorizontalScrollView.setVisibility(View.GONE);
            }
        }

        private void initBottomContainer() {
            // set bottom thumbnail container
            mPrimaryColor = new BaseConfig(getContext()).getPrimaryColor();
        }

        private void setFontsTypeface() {
            FontUtils.setFontsTypeface(getContext(), getActivity().getAssets(), null, mTitle, mDate, mContents);
        }

        private void setFontsSize() {
            float fontSize = CommonUtils.loadFloatPreference(getContext(), Constants.SETTING_FONT_SIZE, -1);
            if (fontSize > 0) FontUtils.setFontsSize(fontSize, -1, mTitle, mDate, mContents);
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
            this.mDiaryList = EasyDiaryDbHelper.readDiary(query, CommonUtils.loadBooleanPreference(getApplicationContext(), Constants.DIARY_SEARCH_QUERY_CASE_SENSITIVE));
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
