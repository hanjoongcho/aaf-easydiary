package me.blog.korn123.commons.utils;

import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Created by CHO HANJOONG on 2017-03-16.
 */

public class DateUtils {

    // EEE: Tue
    // EEEE: Tuesday
    // EEEEE: T
    public static final int HOURS_24 = 24;
    public static final int MINUTES_60 = 60;
    public static final int SECONDS_60 = 60;
    public static final int MILLI_SECONDS_1000 = 1000;
    private static final int UNIT_HEX = 16;
    public static final String DATE_PATTERN_DASH = "yyyy-MM-dd";
    public static final String TIME_PATTERN = "HH:mm";
    public static final String TIME_PATTERN_WITH_SECONDS = "HH:mm ss";
    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_TIME_PATTERN_WITHOUT_DELIMITER = "yyyyMMddHHmmss";
    public static final String DATE_HMS_PATTERN = "yyyyMMddHHmmss";
    public static final String TIMESTAMP_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final String YEAR_PATTERN = "yyyy";
    public static final String MONTH_PATTERN = "MM";
    public static final String DAY_PATTERN = "dd";
    public static final String DATE_PATTERN = "yyyyMMdd";
    public static final String TIME_HMS_PATTERN = "HHmmss";
    public static final String TIME_HMS_PATTERN_COLONE = "HH:mm:ss";

    public static String getCurrentDateAsString()
    {
        return getCurrentDateAsString("yyyy-MM-dd");
    }

    public static String getCurrentDateAsString(String pattern) {
        SimpleDateFormat df = new SimpleDateFormat(pattern);
        return df.format(new Date());
    }

    public static void main(String[] args)
    {
        System.out.println(getCurrentDateTime());
    }

    public static String getCurrentDateTime()
    {
        return getCurrentDateTime("yyyy-MM-dd HH:mm:ss");
    }

    public static String getCurrentDateTime(String pattern)
    {
        DateTime dt = new DateTime();
        DateTimeFormatter fmt = DateTimeFormat.forPattern(pattern);
        return fmt.print(dt);
    }

    public static String getDateTime(long currentTimeMillis) {
        DateTime dt = new DateTime(currentTimeMillis);
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        return fmt.print(dt);
    }

    public static String timeMillisToDateTime(long timeMillis, String pattern) {
        DateTime dt = new DateTime(timeMillis);
        DateTimeFormatter fmt = DateTimeFormat.forPattern(pattern);
        return fmt.print(dt);
    }

    public static String timeMillisToDateTime(long timeMillis) {
        DateTime dt = new DateTime(timeMillis);
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        return fmt.print(dt);
    }

    public static String timeMillisToHour(long timeMillis) {
        DateTime dt = new DateTime(timeMillis);
        DateTimeFormatter fmt = DateTimeFormat.forPattern("HH");
        return fmt.print(dt);
    }

    public static String getFullPatternDate(long timeMillis) {
//        DateTime dt = new DateTime(timeMillis);
//        DateTimeFormatter fmt = DateTimeFormat.forPattern(DateTimeFormat.patternForStyle("LL", Locale.getDefault()));
//        return fmt.print(dt);
        Date date = new Date(timeMillis);
        DateFormat dateFormat = SimpleDateFormat.getDateInstance(SimpleDateFormat.FULL, Locale.getDefault());
        return dateFormat.format(date);
    }

    public static String getFullPatternDateWithTime(long timeMillis) {
        Date date = new Date(timeMillis);
        DateFormat dateFormat = SimpleDateFormat.getDateInstance(SimpleDateFormat.FULL, Locale.getDefault());
        DateFormat hourFormat = new SimpleDateFormat(TIME_PATTERN_WITH_SECONDS);
        return String.format("%s %s", dateFormat.format(date), hourFormat.format(date));
    }
}
