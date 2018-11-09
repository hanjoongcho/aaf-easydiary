package me.blog.korn123.easydiary.activities

import android.content.DialogInterface
import android.graphics.drawable.GradientDrawable
import android.os.Environment
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.ColorUtils
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import io.github.aafactory.commons.utils.BitmapUtils
import io.github.aafactory.commons.utils.CommonUtils
import io.realm.RealmList
import kotlinx.android.synthetic.main.layout_bottom_toolbar.*
import kotlinx.android.synthetic.main.layout_edit_photo_container.*
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.showAlertDialog
import me.blog.korn123.easydiary.helper.DIARY_PHOTO_DIRECTORY
import me.blog.korn123.easydiary.helper.FILE_URI_PREFIX
import me.blog.korn123.easydiary.helper.THUMBNAIL_BACKGROUND_ALPHA
import me.blog.korn123.easydiary.models.PhotoUriDto
import java.io.File
import java.util.*

abstract class EditActivity : EasyDiaryActivity() {
    protected lateinit var mPhotoUris: RealmList<PhotoUriDto>
    protected var mPrimaryColor = 0
    protected val mRemoveIndexes = ArrayList<Int>()
    
    fun attachPhotos(selectPaths: ArrayList<String>) {
        setVisiblePhotoProgress(true)
        Thread(Runnable {
            selectPaths.map { item ->
                val photoPath = Environment.getExternalStorageDirectory().absolutePath + DIARY_PHOTO_DIRECTORY + UUID.randomUUID().toString()
                try {
                    EasyDiaryUtils.downSamplingImage(this, File(item), File(photoPath))
                    mPhotoUris.add(PhotoUriDto(FILE_URI_PREFIX + photoPath))
                    val thumbnailSize = config.settingThumbnailSize
                    val bitmap = BitmapUtils.decodeFile(photoPath, CommonUtils.dpToPixel(applicationContext, thumbnailSize - 5), CommonUtils.dpToPixel(applicationContext, thumbnailSize - 5))
                    val imageView = ImageView(applicationContext)
                    val layoutParams = LinearLayout.LayoutParams(CommonUtils.dpToPixel(applicationContext, thumbnailSize), CommonUtils.dpToPixel(applicationContext, thumbnailSize))
                    layoutParams.setMargins(0, 0, CommonUtils.dpToPixel(applicationContext, 3F), 0)
                    imageView.layoutParams = layoutParams
                    val drawable = ContextCompat.getDrawable(this, R.drawable.bg_card_thumbnail)
                    val gradient = drawable as GradientDrawable
                    gradient.setColor(ColorUtils.setAlphaComponent(mPrimaryColor, THUMBNAIL_BACKGROUND_ALPHA))
                    imageView.background = gradient
                    imageView.setImageBitmap(bitmap)
                    imageView.scaleType = ImageView.ScaleType.CENTER
                    val currentIndex = mPhotoUris.size - 1
                    imageView.setOnClickListener(PhotoClickListener(currentIndex))
                    runOnUiThread {
                        photoContainer.addView(imageView, photoContainer.childCount - 1)
                        initBottomToolbar()

                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            runOnUiThread {
                photoContainer.postDelayed({ (photoContainer.parent as HorizontalScrollView).fullScroll(HorizontalScrollView.FOCUS_RIGHT) }, 100L)
                setVisiblePhotoProgress(false)
            }
        }).start()
    }

    protected fun initBottomToolbar() {
        bottomTitle.text = String.format(getString(R.string.attached_photo_count), photoContainer.childCount -1)
    }
    
    abstract fun setVisiblePhotoProgress(isVisible: Boolean)
    
    internal inner class PhotoClickListener(var index: Int) : View.OnClickListener {
        override fun onClick(v: View) {
            val targetIndex = index
            showAlertDialog(
                    getString(R.string.delete_photo_confirm_message),
                    DialogInterface.OnClickListener { dialog, which ->
                        mRemoveIndexes.add(targetIndex)
                        photoContainer.removeView(v)
                        initBottomToolbar()
                    },
                    DialogInterface.OnClickListener { dialog, which -> }
            )
        }
    }
}