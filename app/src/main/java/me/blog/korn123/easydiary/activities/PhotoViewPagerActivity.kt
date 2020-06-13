package me.blog.korn123.easydiary.activities

import android.os.Bundle
import android.os.Handler
import android.view.*
import android.webkit.MimeTypeMap
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.github.chrisbanes.photoview.PhotoView
import io.github.aafactory.commons.utils.CommonUtils
import kotlinx.android.synthetic.main.activity_photo_view_pager.*
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.shareFile
import me.blog.korn123.easydiary.helper.*
import me.blog.korn123.easydiary.models.DiaryDto
import java.io.File

/**
 * Created by hanjoong on 2017-06-08.
 */

class PhotoViewPagerActivity : EasyDiaryActivity() {
    private var mPhotoCount: Int = 0

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_view_pager)
        setSupportActionBar(toolbar)

        val intent = intent
        val sequence = intent.getIntExtra(DIARY_SEQUENCE, 0)
        val photoIndex = intent.getIntExtra(DIARY_ATTACH_PHOTO_INDEX, 0)
        val diaryDto = EasyDiaryDbHelper.readDiaryBy(sequence)
        mPhotoCount = diaryDto.photoUris?.size ?: 0

        supportActionBar?.run {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_cross)
            title = "1 / $mPhotoCount"
        }

        view_pager.adapter = PhotoPagerAdapter(diaryDto)
        view_pager.addOnPageChangeListener(object : androidx.viewpager.widget.ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                toolbar.title = "${position + 1} / $mPhotoCount"
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })

//        val closeIcon = ContextCompat.getDrawable(this, R.drawable.x_mark_3)
//        closeIcon?.let {
//            it.setColorFilter(this.config.primaryColor, PorterDuff.Mode.SRC_IN)
//            close.setImageDrawable(closeIcon)   
//        }

        if (photoIndex > 0) Handler().post{ view_pager.setCurrentItem(photoIndex, false) }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> TransitionHelper.finishActivityWithTransition(this, TransitionHelper.TOP_TO_BOTTOM)
            R.id.planner -> {
                (view_pager.findViewWithTag<LinearLayout>("view_${view_pager.currentItem}")).getChildAt(0).run {
                    if (this is PhotoView) setRotationBy(90F)
                }
            }
            R.id.share -> {
                (view_pager.adapter as PhotoPagerAdapter).diaryDto.photoUris?.let {
                    it[view_pager.currentItem]?.let { photoUri ->
                        val filePath = EasyDiaryUtils.getApplicationDataDirectory(this) + photoUri.getFilePath()
                        shareFile(File(filePath), photoUri.mimeType ?: MIME_TYPE_JPEG)
                    }
                }
            }
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.diary_photo_view, menu)
        return true
    }

    internal class PhotoPagerAdapter(var diaryDto: DiaryDto) : androidx.viewpager.widget.PagerAdapter() {
        override fun getCount(): Int {
            return diaryDto.photoUris?.size ?: 0
        }

        override fun instantiateItem(container: ViewGroup, position: Int): View {
            val viewHolder = LinearLayout(container.context).apply { tag = "view_$position" }
            val photoView = PhotoView(container.context)
            val imageFilePath = EasyDiaryUtils.getApplicationDataDirectory(container.context) + diaryDto.photoUris!![position]!!.getFilePath()

            when (File(imageFilePath).exists()) {
                false -> {
                    val textView = TextView(container.context)
                    textView.gravity = Gravity.CENTER
                    val padding = CommonUtils.dpToPixel(container.context, 10F)
                    textView.setPadding(padding, padding, padding, padding)
                    textView.typeface = FontUtils.getCommonTypeface(container.context)
                    textView.text = container.context.getString(R.string.photo_view_error_info)
                    textView.setTextColor(container.context.config.textColor)
                    viewHolder.addView(textView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    container.addView(viewHolder, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    return viewHolder
                }
                true -> {
                    // Now just add PhotoView to ViewPager and return it
//                    photoView.setImageBitmap(bitmap)
                    diaryDto.photoUris?.let {
                        it[position]?.let { photoUriDto ->
                            val options = RequestOptions()
                                    .error(R.drawable.error_7)
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .priority(Priority.HIGH)
                            Glide.with(container.context)
                                    .load(imageFilePath)
                                    .apply(options)
                                    .into(photoView)
                        }
                    }
                    viewHolder.addView(photoView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    container.addView(viewHolder, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    return viewHolder
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
