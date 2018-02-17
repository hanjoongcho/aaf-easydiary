package me.blog.korn123.easydiary.activities

import android.net.Uri
import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.github.chrisbanes.photoview.PhotoView
import kotlinx.android.synthetic.main.activity_photo_view_pager.*
import me.blog.korn123.commons.constants.Constants
import me.blog.korn123.commons.utils.CommonUtils
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import me.blog.korn123.easydiary.models.DiaryDto

/**
 * Created by hanjoong on 2017-06-08.
 */

class PhotoViewPagerActivity : EasyDiaryActivity() {
    private var mPhotoCount: Int = 0

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_view_pager)

        val intent = intent
        val sequence = intent.getIntExtra(Constants.DIARY_SEQUENCE, 0)
        val diaryDto = EasyDiaryDbHelper.readDiaryBy(sequence)
        mPhotoCount = diaryDto.photoUris.size
        pageInfo.text = "1 / $mPhotoCount"

        view_pager.adapter = SamplePagerAdapter(diaryDto)
        view_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                pageInfo.text = "${position + 1} / $mPhotoCount" 
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })

//        val closeIcon = ContextCompat.getDrawable(this, R.drawable.x_mark_3)
//        closeIcon?.let {
//            it.setColorFilter(this.config.primaryColor, PorterDuff.Mode.SRC_IN)
//            close.setImageDrawable(closeIcon)   
//        }

        close.setOnClickListener { finish() }
    }

    internal class SamplePagerAdapter(var diaryDto: DiaryDto) : PagerAdapter() {
        override fun getCount(): Int {
            return diaryDto.photoUris.size
        }

        override fun instantiateItem(container: ViewGroup, position: Int): View {
            val photoView = PhotoView(container.context)
            //            photoView.setImageResource(sDrawables[position]);
            val uri = Uri.parse(diaryDto.photoUris[position].photoUri)
            photoView.setImageURI(uri)
            when (photoView.drawable == null) {
                true -> {
                    val textView = TextView(container.context)
                    textView.gravity = Gravity.CENTER
                    val padding = CommonUtils.dpToPixel(container.context, 10, 1)
                    textView.setPadding(padding, padding, padding, padding)
                    FontUtils.setTypefaceDefault(textView)
                    textView.text = container.context.getString(R.string.photo_view_error_info)
                    container.addView(textView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    return textView    
                }
                false -> {
                    // Now just add PhotoView to ViewPager and return it
                    container.addView(photoView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    return photoView    
                }
            }
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view === `object`
        }
    }
}
