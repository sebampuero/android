package com.example.tm18app.util;

import android.content.res.Resources;

import androidx.core.os.ConfigurationCompat;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Utils class that parses time formats
 *
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 03.12.2019
 */
public class TimeUtils {

    private static final String DATEFORMAT_1 = "h:mm a dd MMMM";
    private static final String DATEFORMAT_2 = "h:mm a";

    private TimeUtils() {
        throw new AssertionError();
    }

    /**
     * Parses a UNIX Timestamp to a readable timestamp format depending on the user's locale
     * @param timestamp {@link Long} the UNIX Timestamp in seconds
     * @return {@link String} the formatted timestamp to current user's locale
     */
    public static String parseTimestampToLocaleDatetime(long timestamp){
        long milliseconds = timestamp * 1000L;
        DateFormat sdf = new SimpleDateFormat(DATEFORMAT_1, getLocale());
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliseconds);
        TimeZone tz = TimeZone.getDefault();
        sdf.setTimeZone(tz);
        return sdf.format(calendar.getTime());
    }

    /**
     * Parses a UNIX timestamp to a readable hour timestamp (e.g. 10:00 PM) format
     * depending onn the user's locale
     * @param timestamp {@link Long}
     * @return {@link String} the formatted hour timestamp
     */
    public static String parseTimestampToLocaleTime(long timestamp) {
        long milliseconds = timestamp * 1000L;
        DateFormat sdf = new SimpleDateFormat(DATEFORMAT_2, getLocale());
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliseconds);
        TimeZone tz = TimeZone.getDefault();
        sdf.setTimeZone(tz);
        return sdf.format(calendar.getTime());
    }

    /**
     * Retrieves the locale the user is using on the phone
     * @return {@link Locale} of the user
     */
    private static Locale getLocale(){
        // the user can have several locales configured, the first item in the array is the default one
        return ConfigurationCompat.getLocales(Resources.getSystem().getConfiguration()).get(0);
    }

}
