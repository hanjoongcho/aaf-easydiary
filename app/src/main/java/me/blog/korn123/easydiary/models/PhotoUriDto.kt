package me.blog.korn123.easydiary.models

import android.os.Environment
import io.realm.RealmObject
import me.blog.korn123.commons.constants.Path
import me.blog.korn123.easydiary.helper.CONTENT_URI_PREFIX
import org.apache.commons.io.FilenameUtils
import org.apache.commons.lang3.StringUtils

/**
 * Created by hanjoong on 2017-06-08.
 */

open class PhotoUriDto : RealmObject {
    lateinit var photoUri: String

    constructor() 

    constructor(photoUri: String) {
        this.photoUri = photoUri
    }
    
    fun isContentUri(): Boolean = StringUtils.startsWith(photoUri, CONTENT_URI_PREFIX)
    
    fun getFilePath(): String {
        return "${Environment.getExternalStorageDirectory().absolutePath}${Path.DIARY_PHOTO_DIRECTORY}${FilenameUtils.getBaseName(photoUri)}"
    } 
}
