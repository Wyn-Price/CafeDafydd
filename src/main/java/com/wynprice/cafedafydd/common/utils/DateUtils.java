package com.wynprice.cafedafydd.common.utils;

import lombok.extern.log4j.Log4j2;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.TimeZone;

@Log4j2
public class DateUtils {

    public static final Date EMPTY_DATE = new Date(0);

    public static String toISO8691(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return sdf.format(date);
    }

    public static Date fromISO8691(String date, boolean required) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        try {
            return sdf.parse(date);
        } catch (ParseException e) {
            if(required) {
                log.error("Unable to parse date " + date + " at " + e.getErrorOffset(), e);
            }
        }
        return EMPTY_DATE;
    }

    public static Date getCurrentDate() {
        return Date.from(Instant.now());
    }
}
