package me.blog.korn123.easydiary.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.chrisbanes.photoview.PhotoView;

import butterknife.ButterKnife;
import butterknife.OnClick;
import me.blog.korn123.commons.constants.Constants;
import me.blog.korn123.commons.utils.CommonUtils;
import me.blog.korn123.commons.utils.FontUtils;
import me.blog.korn123.easydiary.R;
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper;
import me.blog.korn123.easydiary.models.DiaryDto;
import me.blog.korn123.easydiary.viewpagers.HackyViewPager;

/**
 * Created by hanjoong on 2017-06-08.
 */

public class PhotoViewPagerActivity extends EasyDiaryActivity {

    ViewPager mViewPager;
    private TextView mPageInfo;
    private int mPhotoCount;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_view_pager);
        ButterKnife.bind(this);
        mViewPager = (HackyViewPager) findViewById(R.id.view_pager);
        mPageInfo = (TextView)findViewById(R.id.pageInfo);
        setFontsStyle();

        Intent intent = getIntent();
        int sequence = intent.getIntExtra(Constants.DIARY_SEQUENCE, 0);
        DiaryDto diaryDto = EasyDiaryDbHelper.readDiaryBy(sequence);
        mPhotoCount = diaryDto.getPhotoUris().size();
        mPageInfo.setText("1 / " + mPhotoCount);

        mViewPager.setAdapter(new SamplePagerAdapter(diaryDto));
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                mPageInfo.setText((position + 1) + " / " + mPhotoCount);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    @OnClick({R.id.close})
    void onClick(View view) {
        switch(view.getId()) {
            case R.id.close:
                finish();
                break;
        }
    }

    private void setFontsStyle() {
        FontUtils.setFontsTypeface(getApplicationContext(), getAssets(), null, (ViewGroup) findViewById(android.R.id.content));
    }

    static class SamplePagerAdapter extends PagerAdapter {

        DiaryDto diaryDto;

        SamplePagerAdapter(DiaryDto diaryDto) {
            this.diaryDto = diaryDto;
        }

        @Override
        public int getCount() {
            return diaryDto.getPhotoUris().size();
        }

        @Override
        public View instantiateItem(ViewGroup container, int position) {
            PhotoView photoView = new PhotoView(container.getContext());
//            photoView.setImageResource(sDrawables[position]);
            Uri uri = Uri.parse(diaryDto.getPhotoUris().get(position).getPhotoUri());
            photoView.setImageURI(uri);
            if (photoView.getDrawable() == null) {
                TextView textView = new TextView(container.getContext());
                textView.setGravity(Gravity.CENTER);
                int padding = CommonUtils.dpToPixel(container.getContext(), 10, 1);
                textView.setPadding(padding, padding, padding, padding);
                FontUtils.setTypefaceDefault(textView);
                textView.setText(container.getContext().getString(R.string.photo_view_error_info));
                container.addView(textView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                return textView;
            } else {
                // Now just add PhotoView to ViewPager and return it
                container.addView(photoView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                return photoView;
            }
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }


    }

}
