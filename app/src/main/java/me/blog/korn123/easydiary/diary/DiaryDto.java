package me.blog.korn123.easydiary.diary;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

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

    public DiaryDto() {}

    public DiaryDto(int sequence, long currentTimeMillis, String title, String contents) {
        this.sequence = sequence;
        this.currentTimeMillis = currentTimeMillis;
        this.title = title;
        this.contents = contents;
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
}
