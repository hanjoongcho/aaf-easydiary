package me.blog.korn123.easydiary.extensions

import android.app.Activity
import me.blog.korn123.easydiary.databinding.ActivityBaseDevBinding
import me.blog.korn123.easydiary.helper.DEV_SYNC_MARKDOWN_ALL

fun Activity.pushMarkDown(path: String, contents: String) {}

fun Activity.syncMarkDown(mBinding: ActivityBaseDevBinding? = null, syncMode: String = DEV_SYNC_MARKDOWN_ALL, onComplete: () -> Unit = {}) {}