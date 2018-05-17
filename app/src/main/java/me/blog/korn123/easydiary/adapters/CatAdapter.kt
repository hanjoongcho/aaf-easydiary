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

package me.blog.korn123.easydiary.adapters

import android.os.Environment
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.helper.WORKING_DIRECTORY
import me.blog.korn123.easydiary.viewholders.CatViewHolder
import java.io.File

/**
 * Adapter class that handles the data set with the {@link RecyclerView.LayoutManager}
 */
internal class CatAdapter : RecyclerView.Adapter<CatViewHolder>() {

    companion object {
        private val POST_CARDS = File(Environment.getExternalStorageDirectory().absolutePath + WORKING_DIRECTORY).listFiles().filter { it.extension.equals("jpg", true)}
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CatViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.viewholder_post_card, parent, false)
        return CatViewHolder(view)
    }

    override fun onBindViewHolder(holder: CatViewHolder, position: Int) {
//        val pos = position % POST_CARDS.size
        holder.bindTo(POST_CARDS[position])
        holder.itemView.setOnClickListener { 
            
        }
    }

    override fun getItemCount() = POST_CARDS.size
}
