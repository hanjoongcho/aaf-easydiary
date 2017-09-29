package me.blog.korn123.easydiary.models;

import io.realm.RealmObject;

/**
 * Created by hanjoong on 2017-06-08.
 */

public class PhotoUriDto extends RealmObject {

    private String photoUri;

    public PhotoUriDto() {}

    public PhotoUriDto(String photoUri) {
        this.photoUri = photoUri;
    }

    public String getPhotoUri() {
        return photoUri;
    }

    public void setPhotoUri(String photoUri) {
        this.photoUri = photoUri;
    }
}
