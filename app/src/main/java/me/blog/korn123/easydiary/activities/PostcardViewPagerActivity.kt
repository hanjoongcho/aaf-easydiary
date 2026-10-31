package me.blog.korn123.easydiary.activities

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.github.chrisbanes.photoview.PhotoView
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.databinding.ActivityPhotoViewPagerBinding
import me.blog.korn123.easydiary.extensions.dpToPixel
import me.blog.korn123.easydiary.extensions.shareFile
import me.blog.korn123.easydiary.helper.DIARY_POSTCARD_DIRECTORY
import me.blog.korn123.easydiary.helper.MIME_TYPE_JPEG
import me.blog.korn123.easydiary.helper.POSTCARD_SEQUENCE
import me.blog.korn123.easydiary.utils.EasyDiaryUtils
import me.blog.korn123.easydiary.utils.FontUtils
import java.io.File

class PostcardViewPagerActivity : EasyDiaryActivity() {
    private lateinit var binding: ActivityPhotoViewPagerBinding
    private var postcardCount: Int = 0
    private lateinit var listPostcard: List<File>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhotoViewPagerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val sequence = intent.getIntExtra(POSTCARD_SEQUENCE, 0)

        listPostcard =
            File(EasyDiaryUtils.getApplicationDataDirectory(this) + DIARY_POSTCARD_DIRECTORY)
                .listFiles()
                ?.filter { it.extension.equals("jpg", true) }
                ?.sortedDescending()
                ?: emptyList()
        postcardCount = listPostcard.size

        supportActionBar?.run {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_cross)
            title = "1 / $postcardCount"
        }

        binding.run {
            viewPager.adapter = PostcardPagerAdapter(listPostcard)
            viewPager.addOnPageChangeListener(
                object : androidx.viewpager.widget.ViewPager.OnPageChangeListener {
                    override fun onPageScrolled(
                        position: Int,
                        positionOffset: Float,
                        positionOffsetPixels: Int,
                    ) {}

                    override fun onPageSelected(position: Int) {
                        toolbar.title = "${position + 1} / $postcardCount"
                    }

                    override fun onPageScrollStateChanged(state: Int) {}
                },
            )

            viewPager.setCurrentItem(sequence, false)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_postcard_view_pager, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.share -> shareFile(listPostcard[binding.viewPager.currentItem], MIME_TYPE_JPEG)
        }
        return super.onOptionsItemSelected(item)
    }

    internal class PostcardPagerAdapter(
        private var listPostcard: List<File>,
    ) : androidx.viewpager.widget.PagerAdapter() {
        override fun getCount(): Int = listPostcard.size

        override fun instantiateItem(
            container: ViewGroup,
            position: Int,
        ): Any {
            val photoView = PhotoView(container.context)
            val bitmap = BitmapFactory.decodeFile(listPostcard[position].path)
            return if (bitmap == null) {
                val textView = TextView(container.context).apply {
                    gravity = Gravity.CENTER
                    val padding = container.context.dpToPixel(10F)
                    setPadding(padding, padding, padding, padding)
                    FontUtils.setTypefaceDefault(this)
                    text = container.context.getString(R.string.photo_view_error_info)
                }
                container.addView(textView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                textView
            } else {
                photoView.setImageBitmap(bitmap)
                container.addView(photoView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                photoView
            }
        }

        override fun destroyItem(
            container: ViewGroup,
            position: Int,
            objectName: Any,
        ) {
            container.removeView(objectName as View)
        }

        override fun isViewFromObject(
            view: View,
            objectName: Any,
        ): Boolean = view === objectName
    }
}
