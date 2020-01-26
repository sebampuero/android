package com.example.tm18app.util;

import android.content.Context;
import android.content.res.Resources;

import androidx.core.os.ConfigurationCompat;

import com.example.tm18app.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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

    private static final String TAG = "TimeUtils";

    private static final String DATEFORMAT_1 = "dd MMMM";
    private static final String DATEFORMAT_2 = "h:mm a";
    private static final String DATEFORMAT_DAY_IN_YEAR = "D";

    /**
     * Parses a UNIX Timestamp to a readable timestamp format depending on the user's locale
     * @param timestamp {@link Long} the UNIX Timestamp in seconds
     * @return {@link String} the formatted timestamp to current user's locale
     */
    public static String parseTimestampToLocaleDatetime(long timestamp, Context context){
        long milliseconds = timestamp * 1000L;
        DateFormat sdf = new SimpleDateFormat(DATEFORMAT_2, getLocale());
        String day = getDayDisplay(timestamp, context);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliseconds);
        TimeZone tz = TimeZone.getDefault();
        sdf.setTimeZone(tz);
        return sdf.format(calendar.getTime()) + " " + day;
    }

    /**
     * Calculates the day in human readable format with a given timestamp and the current timestamp
     * @param timestamp {@link Long} the timestamp to be compared against
     * @param context {@link Context}
     * @return the day in readable format e.g. today, yesterday or a date if more than yesterday
     */
    private static String getDayDisplay(long timestamp, Context context) {
        DateFormat sdf = new SimpleDateFormat(DATEFORMAT_DAY_IN_YEAR, getLocale());
        int dayOfPost = Integer.parseInt(sdf.format(new Date(timestamp * 1000L)));
        int today = Integer.parseInt(sdf.format(new Date()));
        String result;
        if(today == dayOfPost)
            result = context.getResources().getString(R.string.today);
        else if(today - dayOfPost == 1)
            result = context.getResources().getString(R.string.yesterday);
        else{
            sdf = new SimpleDateFormat(DATEFORMAT_1, getLocale());
            result = sdf.format(new Date(timestamp * 1000L));
        }
        return result;
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
