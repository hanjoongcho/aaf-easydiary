package me.blog.korn123.easydiary.activities

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.github.chrisbanes.photoview.PhotoView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.adapters.GalleryAdapter
import me.blog.korn123.easydiary.databinding.ActivityPhotoViewPagerBinding
import me.blog.korn123.easydiary.extensions.dpToPixel
import me.blog.korn123.easydiary.extensions.makeToast
import me.blog.korn123.easydiary.extensions.shareFile
import me.blog.korn123.easydiary.helper.DIARY_SEQUENCE
import me.blog.korn123.easydiary.helper.MIME_TYPE_JPEG
import me.blog.korn123.easydiary.helper.POSTCARD_SEQUENCE
import me.blog.korn123.easydiary.helper.TransitionHelper
import me.blog.korn123.easydiary.utils.FontUtils

/**
 * Created by hanjoong on 2017-06-08.
 */

class GalleryViewPagerActivity : EasyDiaryActivity() {
    private lateinit var binding: ActivityPhotoViewPagerBinding
    private var attachedPhotoCount: Int = 0
    private val attachedPhotos: ArrayList<GalleryAdapter.AttachedPhoto> = arrayListOf()

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhotoViewPagerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val sequence = intent.getIntExtra(POSTCARD_SEQUENCE, 0)
        lifecycleScope.launch(Dispatchers.IO) {
            val fetchedPhotos = diaryViewModel.getAttachedPhotos(this@GalleryViewPagerActivity)
            withContext(Dispatchers.Main) {
                attachedPhotos.clear()
                fetchedPhotos?.let { attachedPhotos.addAll(it) }

                attachedPhotoCount = attachedPhotos.size

                supportActionBar?.run {
                    setDisplayHomeAsUpEnabled(true)
                    setHomeAsUpIndicator(R.drawable.ic_cross)
                    title = "1 / $attachedPhotoCount"
                }

                binding.run {
                    viewPager.adapter = GalleryPagerAdapter()
                    viewPager.addOnPageChangeListener(
                        object : androidx.viewpager.widget.ViewPager.OnPageChangeListener {
                            override fun onPageScrolled(
                                position: Int,
                                positionOffset: Float,
                                positionOffsetPixels: Int,
                            ) {}

                            override fun onPageSelected(position: Int) {
                                toolbar.title = "${position + 1} / $attachedPhotoCount"
                            }

                            override fun onPageScrollStateChanged(state: Int) {}
                        },
                    )
                    viewPager.setCurrentItem(sequence, false)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_gallery_view_pager, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        binding.run {
            val attachedPhoto = attachedPhotos[viewPager.currentItem]
            when (item.itemId) {
                R.id.share -> {
                    if (attachedPhoto.diary == null) {
                        shareFile(attachedPhoto.file, MIME_TYPE_JPEG)
                    } else {
                        if (attachedPhoto.diary.isEncrypt) {
                            makeToast("Encrypted files cannot be shared.")
                        } else {
                            shareFile(
                                attachedPhoto.file,
                                MIME_TYPE_JPEG,
                            )
                        }
                    }
                }

                R.id.diary -> {
                    attachedPhoto.diary?.let { diary ->
                        TransitionHelper.startActivityWithTransition(
                            this@GalleryViewPagerActivity,
                            Intent(
                                this@GalleryViewPagerActivity,
                                DiaryReadingActivity::class.java,
                            ).apply {
                                putExtra(DIARY_SEQUENCE, diary.diaryId)
                            },
                        )
                    } ?: run { makeToast("There is no linked diary information.") }
                }

                else -> {}
            }
        }
        return super.onOptionsItemSelected(item)
    }

    inner class GalleryPagerAdapter : androidx.viewpager.widget.PagerAdapter() {
        override fun getCount(): Int = attachedPhotos.size

        override fun instantiateItem(
            container: ViewGroup,
            position: Int,
        ): View {
            val photoView = PhotoView(container.context)
            val attachedPhoto = attachedPhotos[position]
            val bitmap = BitmapFactory.decodeFile(if (attachedPhoto.diary?.isEncrypt == true) "" else attachedPhoto.file.path)

            return if (bitmap == null) {
                val textView = TextView(container.context).apply {
                    gravity = Gravity.CENTER
                    val padding = container.context.dpToPixel(10F)
                    setPadding(padding, padding, padding, padding)
                    typeface = FontUtils.getCommonTypeface(container.context)
                    text = if (attachedPhoto.diary?.isEncrypt == true) {
                        "The diary is encrypted. You will need to decrypt the diary to see the attached photos."
                    } else {
                        container.context.getString(R.string.photo_view_error_info)
                    }
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
