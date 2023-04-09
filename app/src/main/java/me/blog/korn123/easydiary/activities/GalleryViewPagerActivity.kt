package me.blog.korn123.easydiary.activities

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.*
import android.widget.TextView
import com.github.chrisbanes.photoview.PhotoView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.adapters.GalleryAdapter
import me.blog.korn123.easydiary.databinding.ActivityPhotoViewPagerBinding
import me.blog.korn123.easydiary.extensions.dpToPixel
import me.blog.korn123.easydiary.helper.DIARY_PHOTO_DIRECTORY
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import me.blog.korn123.easydiary.helper.POSTCARD_SEQUENCE
import java.io.File

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
            val realm = EasyDiaryDbHelper.getTemporaryInstance()
            val listPostcard = File(EasyDiaryUtils.getApplicationDataDirectory(this@GalleryViewPagerActivity) + DIARY_PHOTO_DIRECTORY)
                    .listFiles()
                    .map { file ->
                        val diary = EasyDiaryDbHelper.findDiaryBy(file.name, realm)
                        GalleryAdapter.AttachedPhoto(file, false, if (diary != null) realm.copyFromRealm(diary) else null)
                    }.sortedByDescending { item -> item.diary?.currentTimeMillis ?: 0 }
            realm.close()
            withContext(Dispatchers.Main) {
                mAttachedPhotos.clear()
                mAttachedPhotos.addAll(listPostcard)

                mAttachedPhotoCount = mAttachedPhotos.size

                supportActionBar?.run {
                    setDisplayHomeAsUpEnabled(true)
                    setHomeAsUpIndicator(R.drawable.ic_cross)
                    title = "1 / $mAttachedPhotoCount"
                }

                mBinding.run {
                    viewPager.adapter = GalleryPagerAdapter(mAttachedPhotos)
                    viewPager.addOnPageChangeListener(object : androidx.viewpager.widget.ViewPager.OnPageChangeListener {
                        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

                        override fun onPageSelected(position: Int) {
                            toolbar.title = "${position + 1} / $mAttachedPhotoCount"
                        }

                        override fun onPageScrollStateChanged(state: Int) {}
                    })

//        val closeIcon = ContextCompat.getDrawable(this, R.drawable.x_mark_3)
//        closeIcon?.let {
//            it.setColorFilter(this.config.primaryColor, PorterDuff.Mode.SRC_IN)
//            close.setImageDrawable(closeIcon)
//        }

                    viewPager.setCurrentItem(sequence, false)
                }
            }
        }



    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_postcard_view_pager, menu)
        return true
    }

//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        when (item.itemId) {
//            R.id.share -> shareFile(mListPostcard[mBinding.viewPager.currentItem], MIME_TYPE_JPEG)
//        }
//        return super.onOptionsItemSelected(item)
//    }

    internal class GalleryPagerAdapter(private val attachedPhotos: List<GalleryAdapter.AttachedPhoto>) : androidx.viewpager.widget.PagerAdapter() {
        override fun getCount(): Int {
            return attachedPhotos.size
        }

        override fun instantiateItem(container: ViewGroup, position: Int): View {
            val photoView = PhotoView(container.context)
            val bitmap = BitmapFactory.decodeFile(attachedPhotos[position].file.path)
            when (bitmap == null) {
                true -> {
                    val textView = TextView(container.context)
                    textView.gravity = Gravity.CENTER
                    val padding = container.context.dpToPixel(10F)
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
