package me.blog.korn123.easydiary.activities

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.*
import android.widget.TextView
import com.github.chrisbanes.photoview.PhotoView
import io.github.aafactory.commons.utils.CommonUtils
import kotlinx.android.synthetic.main.activity_photo_view_pager.*
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.shareFile
import me.blog.korn123.easydiary.helper.DIARY_POSTCARD_DIRECTORY
import me.blog.korn123.easydiary.helper.MIME_TYPE_JPEG
import me.blog.korn123.easydiary.helper.POSTCARD_SEQUENCE
import java.io.File

/**
 * Created by hanjoong on 2017-06-08.
 */

class PostcardViewPagerActivity : EasyDiaryActivity() {
    private var mPostcardCount: Int = 0
    private lateinit var mListPostcard: List<File>

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_view_pager)
        setSupportActionBar(toolbar)

        val intent = intent
        val sequence = intent.getIntExtra(POSTCARD_SEQUENCE, 0)

        mListPostcard = File(EasyDiaryUtils.getApplicationDataDirectory(this) + DIARY_POSTCARD_DIRECTORY)
                .listFiles()
                .filter { it.extension.equals("jpg", true)}
                .sortedDescending()
        mPostcardCount = mListPostcard.size

        supportActionBar?.run {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_cross)
            title = "1 / $mPostcardCount"
        }

        view_pager.adapter = PostcardPagerAdapter(mListPostcard)
        view_pager.addOnPageChangeListener(object : androidx.viewpager.widget.ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                toolbar.title = "${position + 1} / $mPostcardCount"
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })

//        val closeIcon = ContextCompat.getDrawable(this, R.drawable.x_mark_3)
//        closeIcon?.let {
//            it.setColorFilter(this.config.primaryColor, PorterDuff.Mode.SRC_IN)
//            close.setImageDrawable(closeIcon)   
//        }

        view_pager.setCurrentItem(sequence, false)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.diary_post_card_view_pager, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.share -> shareFile(mListPostcard[view_pager.currentItem], MIME_TYPE_JPEG)
        }
        return super.onOptionsItemSelected(item)
    }

    internal class PostcardPagerAdapter(var listPostcard: List<File>) : androidx.viewpager.widget.PagerAdapter() {
        override fun getCount(): Int {
            return listPostcard.size
        }

        override fun instantiateItem(container: ViewGroup, position: Int): View {
            val photoView = PhotoView(container.context)
            val bitmap = BitmapFactory.decodeFile(listPostcard[position].path)
            when (bitmap == null) {
                true -> {
                    val textView = TextView(container.context)
                    textView.gravity = Gravity.CENTER
                    val padding = CommonUtils.dpToPixel(container.context, 10F)
                    textView.setPadding(padding, padding, padding, padding)
                    FontUtils.setTypefaceDefault(textView)
                    textView.text = container.context.getString(R.string.photo_view_error_info)
                    container.addView(textView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    return textView    
                }
                false -> {
                    // Now just add PhotoView to ViewPager and return it
                    photoView.setImageBitmap(bitmap)
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
