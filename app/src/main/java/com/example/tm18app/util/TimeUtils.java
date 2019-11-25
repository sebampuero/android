package com.example.tm18app.util;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.util.Log;

import androidx.core.os.ConfigurationCompat;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class TimeUtils {

    private static final String DATEFORMAT_1 = "h:mm a dd MMMM";

    private TimeUtils() {
        throw new AssertionError();
    }

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

    private static Locale getLocale(){
        Log.v("Locales", ConfigurationCompat.getLocales(Resources.getSystem().getConfiguration()).toLanguageTags());
        return ConfigurationCompat.getLocales(Resources.getSystem().getConfiguration()).get(0);
    }

}
