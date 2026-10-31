package me.blog.korn123.easydiary.activities

import android.os.Bundle
import android.os.Looper
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.github.chrisbanes.photoview.PhotoView
import kotlinx.coroutines.launch
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.databinding.ActivityPhotoViewPagerBinding
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.dpToPixel
import me.blog.korn123.easydiary.extensions.shareFile
import me.blog.korn123.easydiary.helper.*
import me.blog.korn123.easydiary.utils.EasyDiaryUtils
import me.blog.korn123.easydiary.utils.FontUtils
import java.io.File
import me.blog.korn123.easydiary.domain.model.Diary as DiaryDomain

/**
 * Created by hanjoong on 2017-06-08.
 */

class PhotoViewPagerActivity : EasyDiaryActivity() {
    private lateinit var binding: ActivityPhotoViewPagerBinding
    private var photoCount: Int = 0

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhotoViewPagerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val intent = intent
        val sequence = intent.getLongExtra(DIARY_SEQUENCE, 0)
        val photoIndex = intent.getIntExtra(DIARY_ATTACH_PHOTO_INDEX, 0)
        lifecycleScope.launch {
            diaryViewModel.findDiaryById(sequence)?.let { diary ->
                photoCount = diary.photoUris.size

                supportActionBar?.run {
                    setDisplayHomeAsUpEnabled(true)
                    setHomeAsUpIndicator(R.drawable.ic_cross)
                    title = "1 / $photoCount"
                }

                binding.run {
                    viewPager.adapter = PhotoPagerAdapter(diary)
                    viewPager.addOnPageChangeListener(
                        object : androidx.viewpager.widget.ViewPager.OnPageChangeListener {
                            override fun onPageScrolled(
                                position: Int,
                                positionOffset: Float,
                                positionOffsetPixels: Int,
                            ) {}

                            override fun onPageSelected(position: Int) {
                                toolbar.title = "${position + 1} / $photoCount"
                            }

                            override fun onPageScrollStateChanged(state: Int) {}
                        },
                    )
                    if (photoIndex > 0) android.os.Handler(Looper.getMainLooper()).post { viewPager.setCurrentItem(photoIndex, false) }
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        binding.run {
            when (item.itemId) {
                android.R.id.home -> {
                    TransitionHelper.finishActivityWithTransition(this@PhotoViewPagerActivity, TransitionConstants.TOP_TO_BOTTOM)
                }

                R.id.planner -> {
                    (viewPager.findViewWithTag<LinearLayout>("view_${viewPager.currentItem}")).getChildAt(0).run {
                        if (this is PhotoView) setRotationBy(90F)
                    }
                }

                R.id.share -> {
                    val diary = (viewPager.adapter as PhotoPagerAdapter).diary
                    if (diary.photoUris.isNotEmpty() && viewPager.currentItem in diary.photoUris.indices) {
                        val photoUri = diary.photoUris[viewPager.currentItem]
                        when (diary.isEncrypt) {
                            true -> {}

                            false -> {
                                val filePath = EasyDiaryUtils.getApplicationDataDirectory(this@PhotoViewPagerActivity) + photoUri.getFilePath()
                                shareFile(File(filePath), photoUri.mimeType ?: MIME_TYPE_JPEG)
                            }
                        }
                    }
                }

                else -> {}
            }
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_photo_view_pager, menu)
        return true
    }

    internal class PhotoPagerAdapter(
        var diary: DiaryDomain,
    ) : androidx.viewpager.widget.PagerAdapter() {
        override fun getCount(): Int = diary.photoUris.size

        override fun instantiateItem(
            container: ViewGroup,
            position: Int,
        ): View {
            val viewHolder = LinearLayout(container.context).apply { tag = "view_$position" }
            val photoView = PhotoView(container.context)
            val imageFilePath = EasyDiaryUtils.getApplicationDataDirectory(container.context) + diary.photoUrisWithEncryptionPolicy()!![position].getFilePath()
            when {
                File(imageFilePath).isFile -> {
                    val options =
                        RequestOptions()
                            .error(R.drawable.ic_error_7)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .priority(Priority.HIGH)
                    Glide
                        .with(container.context)
                        .load(imageFilePath)
                        .apply(options)
                        .into(photoView)
                    viewHolder.addView(photoView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    container.addView(viewHolder, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    return viewHolder
                }

                else -> {
                    val textView = TextView(container.context)
                    textView.gravity = Gravity.CENTER
                    val padding = container.context.dpToPixel(10F)
                    textView.setPadding(padding, padding, padding, padding)
                    textView.typeface = FontUtils.getCommonTypeface(container.context)
                    textView.text = if (diary.isEncrypt) "The diary is encrypted. You will need to decrypt the diary to see the attached photos." else container.context.getString(R.string.photo_view_error_info)
                    textView.setTextColor(container.context.config.textColor)
                    viewHolder.addView(textView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    container.addView(viewHolder, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    return viewHolder
                }
            }
        }

        override fun destroyItem(
            container: ViewGroup,
            position: Int,
            `object`: Any,
        ) {
            container.removeView(`object` as View)
        }

        override fun isViewFromObject(
            view: View,
            `object`: Any,
        ): Boolean = view === `object`
    }
}
