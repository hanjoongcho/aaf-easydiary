package me.blog.korn123.easydiary.models

import io.realm.RealmObject

open class Location : RealmObject {
    constructor()
    constructor(address: String?, latitude: Double, longitude: Double) : super() {
        this.address = address
        this.latitude = latitude
        this.longitude = longitude
    }

    var address: String? = null
    var latitude: Double = 0.0
    var longitude: Double = 0.0
}