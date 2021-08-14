package me.blog.korn123.easydiary.adapters

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.request.RequestOptions
import com.google.android.flexbox.FlexboxLayoutManager
import io.github.aafactory.commons.utils.CommonUtils
import jp.wasabeef.glide.transformations.BitmapTransformation
import jp.wasabeef.glide.transformations.CropTransformation
import jp.wasabeef.glide.transformations.GrayscaleTransformation
import jp.wasabeef.glide.transformations.gpu.*
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.PostcardActivity
import me.blog.korn123.easydiary.fragments.PhotoFlexItemOptionFragment
import kotlin.math.ceil
import kotlin.math.sqrt

class PhotoAdapter(
        val activity: AppCompatActivity,
        val postCardPhotoItems: List<PostCardPhotoItem>,
        private val dialogPositiveCallback: () -> Unit
) : RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {
    private val glideOptionMap = hashMapOf<Int, Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.viewholder_photo, parent, false)
        return PhotoViewHolder(view, activity, itemCount, this)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        postCardPhotoItems[position].let { postCardPhotoItem ->
            if (itemCount == 2) {
                holder.itemView.layoutParams = (holder.itemView.layoutParams as FlexboxLayoutManager.LayoutParams).apply {
                    if (position == 0) {
                        isWrapBefore = false
                        flexGrow = 1F
                    } else {
                        isWrapBefore = true
                        flexGrow = 0F
                    }
                }
            }
            holder.bindTo(postCardPhotoItem)
        }
    }

    override fun getItemCount() = postCardPhotoItems.size

    fun onItemHolderClick(itemHolder: PhotoViewHolder) {
        val postCardPhotoItem = postCardPhotoItems[itemHolder.adapterPosition]
        PhotoFlexItemOptionFragment.newInstance(postCardPhotoItem).apply {
            positiveCallback = { viewMode, filterMode, forceSinglePhotoPosition ->
                postCardPhotoItem.viewMode = viewMode
                postCardPhotoItem.filterMode = filterMode
                postCardPhotoItem.forceSinglePhotoPosition = forceSinglePhotoPosition
                itemHolder.bindTo(postCardPhotoItem)
                dialogPositiveCallback.invoke()
                notifyDataSetChanged()
            }
        }.show(activity.supportFragmentManager, "")
    }
    
//    fun getFlexDirection(): Int = when (activity.resources.configuration.orientation == ORIENTATION_PORTRAIT) {
//        true -> {
//            when (itemCount) {
//                3, 5, 6 -> FlexDirection.COLUMN
//                else -> FlexDirection.ROW
//            }
//        }
//        false -> FlexDirection.COLUMN
//    }

    class PhotoViewHolder(
            itemView: View,
            val activity: Activity,
            private val itemCount: Int,
            val adapter: PhotoAdapter
    ) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.photo)

        init {
            if (itemView is ViewGroup) itemView.setOnClickListener {
                adapter.onItemHolderClick(this)
            }
        }

        fun bindTo(postCardPhotoItem: PostCardPhotoItem) {
            val point =  CommonUtils.getDefaultDisplay(activity)
            val height = PostcardActivity.calcPhotoGridHeight(activity)
            val size = if (point.x > point.y) height else point.x

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
                            rb.apply(RequestOptions.bitmapTransform(createBitmapTransformation(filterMode))).into(imageView)
                        }

                    }
                    else -> {
                        if (filterMode == 0) {
                            rb.apply(RequestOptions.bitmapTransform(CropTransformation(imageView.layoutParams.width, imageView.layoutParams.height, getCropType(viewMode)))).into(imageView)
                        } else {
                            rb.apply(RequestOptions.bitmapTransform(MultiTransformation<Bitmap>(
                                    CropTransformation(imageView.layoutParams.width, imageView.layoutParams.height, getCropType(viewMode)),
                                    createBitmapTransformation(filterMode)
                            ))).into(imageView)
                        }
                    }
                }
            }
        }
    }

    data class PostCardPhotoItem(val photoUri: String, val position: Int, var viewMode: Int, var filterMode: Int, var forceSinglePhotoPosition: Boolean = false)
}
