package me.blog.korn123.easydiary.diary;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

/**
 * Created by hanjoong on 2017-02-15.
 */

public class User extends RealmObject {
    @PrimaryKey
    private String id;
    @Ignore
    private int sessionId; // document에 key를 생성하지 않음??
    private String name;
    private long currentMillis;
    private int age;

    // IDE에 의해 생성된 표준 게터와 세터들...
    public String getName() { return name; }
    public void   setName(String name) { this.name = name; }
    public int    getAge() { return age; }
    public void   setAge(int age) { this.age = age; }
    public int    getSessionId() { return sessionId; }
    public void   setSessionId(int sessionId) { this.sessionId = sessionId; }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getCurrentMillis() {
        return currentMillis;
    }

    public void setCurrentMillis(long currentMillis) {
        this.currentMillis = currentMillis;
    }
}
