package me.blog.korn123.easydiary.models

import io.realm.RealmObject

/**
 * Created by hanjoong on 2017-06-08.
 */

open class PhotoUriDto : RealmObject {
    var photoUri: String? = null

    constructor() 

    constructor(photoUri: String) {
        this.photoUri = photoUri
    }
}
