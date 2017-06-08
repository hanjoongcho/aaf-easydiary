package me.blog.korn123.easydiary.diary;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

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
