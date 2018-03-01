package me.blog.korn123.easydiary.models

import io.realm.RealmObject
import me.blog.korn123.easydiary.helper.CONTENT_URI_PREFIX
import org.apache.commons.lang3.StringUtils

/**
 * Created by hanjoong on 2017-06-08.
 */

open class PhotoUriDto : RealmObject {
    var photoUri: String? = null

    constructor() 

    constructor(photoUri: String) {
        this.photoUri = photoUri
    }
    
    fun isContentUri(): Boolean = StringUtils.startsWith(photoUri, CONTENT_URI_PREFIX)
    
    fun getFilePath(): String = StringUtils.substring(photoUri, 6)
}
