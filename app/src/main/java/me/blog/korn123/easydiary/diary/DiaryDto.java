package me.blog.korn123.easydiary.diary;

import java.util.Date;
import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;
import me.blog.korn123.commons.utils.DateUtils;

/**
 * Created by CHO HANJOONG on 2017-03-16.
 */

public class DiaryDto extends RealmObject {

    @PrimaryKey
    private int sequence;

    @Ignore
    private int sessionId;

    private long currentTimeMillis;

    private String title;

    private String contents;

    private String dateString;

    private int weather;

    private RealmList<PhotoUriDto> photoUris;

    public DiaryDto() {}

    public DiaryDto(int sequence, long currentTimeMillis, String title, String contents) {
        this.sequence = sequence;
        this.currentTimeMillis = currentTimeMillis;
        this.title = title;
        this.contents = contents;
        this.dateString = DateUtils.timeMillisToDateTime(currentTimeMillis, DateUtils.DATE_PATTERN_DASH);
    }

    public DiaryDto(int sequence, long currentTimeMillis, String title, String contents, int weather) {
        this.sequence = sequence;
        this.currentTimeMillis = currentTimeMillis;
        this.title = title;
        this.contents = contents;
        this.dateString = DateUtils.timeMillisToDateTime(currentTimeMillis, DateUtils.DATE_PATTERN_DASH);
        this.weather = weather;
    }

    public String getDateString() {
        return dateString;
    }

    public void setDateString(String dateString) {
        this.dateString = dateString;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public long getCurrentTimeMillis() {
        return currentTimeMillis;
    }

    public void setCurrentTimeMillis(long currentTimeMillis) {
        this.currentTimeMillis = currentTimeMillis;
    }

    public int getWeather() {
        return weather;
    }

    public void setWeather(int weather) {
        this.weather = weather;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public RealmList<PhotoUriDto> getPhotoUris() {
        return photoUris;
    }

    public void setPhotoUris(RealmList<PhotoUriDto> photoUris) {
        this.photoUris = photoUris;
    }
}
