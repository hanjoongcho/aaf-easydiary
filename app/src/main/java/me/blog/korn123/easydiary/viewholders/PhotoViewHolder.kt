package me.blog.korn123.easydiary.viewholders

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.request.RequestOptions.bitmapTransform
import io.github.aafactory.commons.utils.CALCULATION
import io.github.aafactory.commons.utils.CommonUtils
import jp.wasabeef.glide.transformations.BitmapTransformation
import jp.wasabeef.glide.transformations.CropTransformation
import jp.wasabeef.glide.transformations.GrayscaleTransformation
import jp.wasabeef.glide.transformations.gpu.*
import kotlinx.android.synthetic.main.activity_post_card.*
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.PostCardActivity
import me.blog.korn123.easydiary.extensions.actionBarHeight
import me.blog.korn123.easydiary.extensions.statusBarHeight
import me.blog.korn123.easydiary.helper.AAF_TEST
import kotlin.math.ceil
import kotlin.math.sqrt

class PhotoViewHolder(
        itemView: View, 
        val activity: Activity,
        private val itemCount: Int 
) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
    private val imageView: ImageView = itemView.findViewById(R.id.photo)

    fun bindTo(postCardPhotoItem: PostCardPhotoItem) {
        val point =  CommonUtils.getDefaultDisplay(activity)
        val height = PostCardActivity.calcPhotoGridHeight(activity)
        val size = if (point.x > point.y) height else point.x

        Log.i(AAF_TEST, "$height ${activity.actionBarHeight()} ${activity.statusBarHeight()} ${activity.seekBarContainer.height} ${CommonUtils.dpToPixel(activity, 30F, CALCULATION.CEIL)}")

        if (postCardPhotoItem.forceSinglePhotoPosition) {
            imageView.layoutParams.width = size
            imageView.layoutParams.height = size
        } else {
            when (itemCount) {
                1 -> {
                    imageView.layoutParams.width = size
                    imageView.layoutParams.height = size
                }
                else -> {
                    size.div(ceil(sqrt(itemCount.toFloat()))).toInt().run {
                        imageView.layoutParams.width = this
                        imageView.layoutParams.height = this
                    }
                }
            }
        }

        applyOption(imageView.context, postCardPhotoItem.photoUri, postCardPhotoItem.viewMode, postCardPhotoItem.filterMode, imageView)
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
        fun getCropType(viewMode: Int): CropTransformation.CropType? = when (viewMode) {
            1 -> CropTransformation.CropType.TOP
            2 -> CropTransformation.CropType.CENTER
            3 -> CropTransformation.CropType.BOTTOM
            else -> null
        }

        private fun createBitmapTransformation(filterMode: Int) : BitmapTransformation {
            return when (filterMode) {
                1 -> ToonFilterTransformation()
                2 -> SepiaFilterTransformation()
                3 -> ContrastFilterTransformation()
                4 -> InvertFilterTransformation()
                5 -> PixelationFilterTransformation()
                6 -> SketchFilterTransformation()
                7 -> SwirlFilterTransformation()
                8 -> BrightnessFilterTransformation()
                9 -> KuwaharaFilterTransformation()
                10 -> VignetteFilterTransformation()
                else -> GrayscaleTransformation()
            }
        }

        fun applyOption(context: Context, photoUri: String, viewMode: Int, filterMode: Int, imageView: ImageView) {
            val rb = Glide.with(context).load(photoUri)
            when (viewMode) {
                0 -> {
                    if (filterMode == 0) {
                        rb.into(imageView)
                    } else {
                        rb.apply(bitmapTransform(createBitmapTransformation(filterMode))).into(imageView)
                    }

                }
                else -> {
                    if (filterMode == 0) {
                        rb.apply(bitmapTransform(CropTransformation(imageView.layoutParams.width, imageView.layoutParams.height, getCropType(viewMode)))).into(imageView)
                    } else {
                        rb.apply(bitmapTransform(MultiTransformation<Bitmap>(
                                CropTransformation(imageView.layoutParams.width, imageView.layoutParams.height, getCropType(viewMode)),
                                createBitmapTransformation(filterMode)
                        ))).into(imageView)
                    }
                }
            }
        }
    }

    data class PostCardPhotoItem(val photoUri: String, val position: Int, var viewMode: Int, var filterMode: Int, var forceSinglePhotoPosition: Boolean = false)
}
