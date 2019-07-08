package me.blog.korn123.easydiary.viewholders

import android.app.Activity
import android.graphics.Bitmap
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.RequestOptions.bitmapTransform
import com.bumptech.glide.request.RequestOptions.overrideOf
import io.github.aafactory.commons.utils.CommonUtils
import jp.wasabeef.glide.transformations.*
import jp.wasabeef.glide.transformations.gpu.ToonFilterTransformation
import kotlinx.android.synthetic.main.activity_post_card.*
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.actionBarHeight
import me.blog.korn123.easydiary.extensions.statusBarHeight

class PhotoViewHolder(
        itemView: View, 
        val activity: Activity,
        private val itemCount: Int 
) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
    private val imageView: ImageView = itemView.findViewById(R.id.photo)
    
    internal fun bindTo(photoPath: String, position: Int, glideOption: Int = GLIDE_CROP_TOP, forceSinglePhotoPosition: Int = -1) {
        val point =  CommonUtils.getDefaultDisplay(activity)
        val height = point.y - activity.actionBarHeight() - activity.statusBarHeight() - activity.seekBarContainer.height
        val size = if (point.x > point.y) height else point.x

        if (forceSinglePhotoPosition > -1) {
            imageView.visibility = View.VISIBLE
            imageView.layoutParams.width = size
            imageView.layoutParams.height = size
        } else {
            when (itemCount) {
                1 -> {
                    imageView.layoutParams.width = size
                    imageView.layoutParams.height = size

                }
                3, 5, 6 -> {
                    if (position < 1) {
                        imageView.layoutParams.width = (size * 0.8).toInt()
                        imageView.layoutParams.height = size
                    } else {
                        imageView.layoutParams.width = (size * 0.2).toInt()
                        imageView.layoutParams.height = (size * 0.2).toInt()
                    }
                }
                2, 4 -> {
                    imageView.layoutParams.width = size / 2
                    imageView.layoutParams.height = size / 2
                }
                else -> {
                    imageView.layoutParams.width = (size / Math.round(Math.sqrt(itemCount.toDouble())).toInt())
                    imageView.layoutParams.height = (size / Math.round(Math.sqrt(itemCount.toDouble())).toInt())
                }
            }
        }


        when (glideOption) {
            GLIDE_CROP_TOP -> Glide
                    .with(imageView.context)
                    .load(photoPath)
                    .apply(bitmapTransform(CropTransformation(imageView.layoutParams.width, imageView.layoutParams.height, CropTransformation.CropType.TOP)))
                    .into(imageView)
            GLIDE_CROP_CENTER -> Glide
                    .with(imageView.context)
                    .load(photoPath)
                    .apply(bitmapTransform(CropTransformation(imageView.layoutParams.width, imageView.layoutParams.height, CropTransformation.CropType.CENTER)))
                    .into(imageView)
            GLIDE_CROP_BOTTOM -> Glide
                    .with(imageView.context)
                    .load(photoPath)
                    .apply(bitmapTransform(CropTransformation(imageView.layoutParams.width, imageView.layoutParams.height, CropTransformation.CropType.BOTTOM)))
                    .into(imageView)
            GLIDE_CROP_TOP_GRAY_SCALE -> Glide
                    .with(imageView.context)
                    .load(photoPath)
                    .apply(bitmapTransform(MultiTransformation<Bitmap>(
                            CropTransformation(imageView.layoutParams.width, imageView.layoutParams.height, CropTransformation.CropType.TOP),
                            GrayscaleTransformation()
                    )))
                    .into(imageView)
            GLIDE_CROP_CENTER_GRAY_SCALE -> Glide
                    .with(imageView.context)
                    .load(photoPath)
                    .apply(bitmapTransform(MultiTransformation<Bitmap>(
                            CropTransformation(imageView.layoutParams.width, imageView.layoutParams.height, CropTransformation.CropType.CENTER),
                            GrayscaleTransformation()
                    )))
                    .into(imageView)
            GLIDE_CROP_BOTTOM_GRAY_SCALE -> Glide
                    .with(imageView.context)
                    .load(photoPath)
                    .apply(bitmapTransform(MultiTransformation<Bitmap>(
                            CropTransformation(imageView.layoutParams.width, imageView.layoutParams.height, CropTransformation.CropType.BOTTOM),
                            GrayscaleTransformation()
                    )))
                    .into(imageView)
            GLIDE_CROP_TOP_CARTOON -> Glide
                    .with(imageView.context)
                    .load(photoPath)
                    .apply(bitmapTransform(MultiTransformation<Bitmap>(
                            CropTransformation(imageView.layoutParams.width, imageView.layoutParams.height, CropTransformation.CropType.TOP),
                            ToonFilterTransformation()
                    )))
                    .into(imageView)
            GLIDE_CROP_CENTER_CARTOON -> Glide
                    .with(imageView.context)
                    .load(photoPath)
                    .apply(bitmapTransform(MultiTransformation<Bitmap>(
                            CropTransformation(imageView.layoutParams.width, imageView.layoutParams.height, CropTransformation.CropType.CENTER),
                            ToonFilterTransformation()
                    )))
                    .into(imageView)
            GLIDE_CROP_BOTTOM_CARTOON -> Glide
                    .with(imageView.context)
                    .load(photoPath)
                    .apply(bitmapTransform(MultiTransformation<Bitmap>(
                            CropTransformation(imageView.layoutParams.width, imageView.layoutParams.height, CropTransformation.CropType.BOTTOM),
                            ToonFilterTransformation()
                    )))
                    .into(imageView)
        }

//        Glide.with(imageView.context)
//                .load(photoPath)
////                .apply(RequestOptions().placeholder(R.drawable.ic_aaf_photos).fitCenter())
//                .apply(RequestOptions().transforms(bitmapTransformation))
////                .apply(RequestOptions().transforms(CenterCrop(), RoundedCorners(CommonUtils.dpToPixel(imageView.context, roundCornerDpUnit))))
//                .into(imageView)
    }

    fun getStatusBarHeight(): Int {
        var result = 0
        val resourceId = activity.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = activity.resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

    companion object {
        const val GLIDE_CROP_TOP = 0
        const val GLIDE_CROP_CENTER = 1
        const val GLIDE_CROP_BOTTOM = 2
        const val GLIDE_CROP_TOP_GRAY_SCALE = 3
        const val GLIDE_CROP_CENTER_GRAY_SCALE = 4
        const val GLIDE_CROP_BOTTOM_GRAY_SCALE = 5
        const val GLIDE_CROP_TOP_CARTOON = 6
        const val GLIDE_CROP_CENTER_CARTOON = 7
        const val GLIDE_CROP_BOTTOM_CARTOON = 8
    }
}
