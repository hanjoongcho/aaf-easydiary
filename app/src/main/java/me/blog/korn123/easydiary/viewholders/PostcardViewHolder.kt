/*
 * Copyright 2017 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.blog.korn123.easydiary.viewholders

import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.flexbox.FlexboxLayoutManager
import me.blog.korn123.commons.utils.CommonUtils
import me.blog.korn123.easydiary.R
import java.io.File

/**
 * ViewHolder that represents a cat image.
 */
internal class PostcardViewHolder(itemView: View, val activity: Activity) : RecyclerView.ViewHolder(itemView) {

    private val imageView: ImageView = itemView.findViewById(R.id.imageview)

    internal fun bindTo(file: File) {
        Log.i("imagepath", file.path)
        val point =  CommonUtils.getDefaultDisplay(activity)
        val targetX = Math.floor(point.x / 3.0)
        imageView.layoutParams.width = targetX.toInt()
        imageView.layoutParams.height = targetX.toInt()
        //        imageView.setImageBitmap(BitmapUtils.decodeFileMaxWidthHeight(file.path, 500))
        Glide.with(imageView.context)
                .load(file)
                .apply(RequestOptions().placeholder(R.drawable.ic_aaf_photos).centerCrop())
                .into(imageView)

        val lp = imageView.layoutParams
        if (lp is FlexboxLayoutManager.LayoutParams) {
            lp.flexGrow = 1f
        }
    }
}
