package me.blog.korn123.easydiary.helper

import android.content.Context
import io.github.hanjoongcho.commons.helpers.BaseConfig
import me.blog.korn123.commons.utils.CommonUtils

/**
 * Created by CHO HANJOONG on 2017-12-24.
 * This code based 'Simple-Commons' package
 * You can see original 'Simple-Commons' from below link.
 * https://github.com/SimpleMobileTools/Simple-Commons
 */

class Config(context: Context) : BaseConfig(context) {
    companion object {
        fun newInstance(context: Context) = Config(context)
    }
}