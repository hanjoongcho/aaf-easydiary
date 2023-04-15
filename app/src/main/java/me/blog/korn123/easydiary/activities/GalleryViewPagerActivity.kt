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
import com.github.chrisbanes.photoview.PhotoView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.blog.korn123.commons.utils.FontUtils
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

/**
 * Created by hanjoong on 2017-06-08.
 */

class GalleryViewPagerActivity : EasyDiaryActivity() {
    private lateinit var mBinding: ActivityPhotoViewPagerBinding
    private var mAttachedPhotoCount: Int = 0
    private var mAttachedPhotos: ArrayList<GalleryAdapter.AttachedPhoto> = arrayListOf()

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityPhotoViewPagerBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        setSupportActionBar(mBinding.toolbar)

        val intent = intent
        val sequence = intent.getIntExtra(POSTCARD_SEQUENCE, 0)
        CoroutineScope(Dispatchers.IO).launch {
            val attachedPhotos = GalleryActivity.getAttachedPhotos(this@GalleryViewPagerActivity)
            withContext(Dispatchers.Main) {
                mAttachedPhotos.clear()
                attachedPhotos?.let { mAttachedPhotos.addAll(it) }

                mAttachedPhotoCount = mAttachedPhotos.size

                supportActionBar?.run {
                    setDisplayHomeAsUpEnabled(true)
                    setHomeAsUpIndicator(R.drawable.ic_cross)
                    title = "1 / $mAttachedPhotoCount"
                }

                mBinding.run {
                    viewPager.adapter = GalleryPagerAdapter()
                    viewPager.addOnPageChangeListener(object : androidx.viewpager.widget.ViewPager.OnPageChangeListener {
                        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

                        override fun onPageSelected(position: Int) {
                            toolbar.title = "${position + 1} / $mAttachedPhotoCount"
                        }

                        override fun onPageScrollStateChanged(state: Int) {}
                    })
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
        mBinding.run {
            val attachedPhoto = mAttachedPhotos[viewPager.currentItem]
            when (item.itemId) {
                R.id.share -> {
                    when (attachedPhoto.diary == null) {
                        true -> {
                            // FIXME: Check mimetype from PhotoUri Model
                            shareFile(attachedPhoto.file, MIME_TYPE_JPEG)
                        }

                        false -> {
                            if (attachedPhoto.diary.isEncrypt) makeToast("Encrypted files cannot be shared.") else shareFile(
                                attachedPhoto.file,
                                MIME_TYPE_JPEG
                            )
                        }
                    }
                }

                R.id.diary -> {
                    attachedPhoto.diary?.let {
                        TransitionHelper.startActivityWithTransition(
                            this@GalleryViewPagerActivity,
                            Intent(
                                this@GalleryViewPagerActivity,
                                DiaryReadingActivity::class.java
                            ).apply {
                                putExtra(DIARY_SEQUENCE, it.sequence)
                            }
                        )
                    } ?: run { makeToast("There is no linked diary information.") }
                }

                else -> {}
            }
        }
        return super.onOptionsItemSelected(item)
    }

    inner class GalleryPagerAdapter : androidx.viewpager.widget.PagerAdapter() {
        override fun getCount(): Int {
            return mAttachedPhotos.size
        }

        override fun instantiateItem(container: ViewGroup, position: Int): View {
            val photoView = PhotoView(container.context)
            val attachedPhoto = mAttachedPhotos[position]
            val bitmap = BitmapFactory.decodeFile(if (attachedPhoto.diary?.isEncrypt == true) "" else attachedPhoto.file.path)

            when (bitmap == null) {
                true -> {
                    val textView = TextView(container.context)
                    textView.gravity = Gravity.CENTER
                    val padding = container.context.dpToPixel(10F)
                    textView.setPadding(padding, padding, padding, padding)
                    textView.typeface = FontUtils.getCommonTypeface(container.context)
                    textView.text = if (attachedPhoto.diary?.isEncrypt == true) "The diary is encrypted. You will need to decrypt the diary to see the attached photos." else container.context.getString(R.string.photo_view_error_info)
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
