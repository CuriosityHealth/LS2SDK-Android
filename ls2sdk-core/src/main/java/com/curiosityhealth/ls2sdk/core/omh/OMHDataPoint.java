package com.curiosityhealth.ls2sdk.core.omh;

import android.support.annotation.Nullable;

//import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by jameskizer on 2/2/17.
 */

public abstract class OMHDataPoint {
    public static final String           DATE_FORMAT_ISO_8601 = "yyyy-MM-dd'T'HH:mm:ssZZZZZ";
    public static final SimpleDateFormat ISO8601Formatter       = new SimpleDateFormat(OMHDataPoint.DATE_FORMAT_ISO_8601,
            Locale.getDefault());

    @Nullable
    public static String stringFromDate(Date date) {
        if (date == null) {
            return null;
        }
        return ISO8601Formatter.format(date);
    }

    @Nullable
    public static Date dateFromString(String dateString) {

        try {
            return ISO8601Formatter.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public abstract JSONObject toJson();
}
