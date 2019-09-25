package com.wynprice.cafedafydd.common.utils;

import lombok.extern.log4j.Log4j2;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.TimeZone;

/**
 * A util class for using dates.
 */
@Log4j2
public class DateUtils {

    /**
     * The epoch date
     */
    public static final Date EMPTY_DATE = new Date(0);

    /**
     * Converts a date to an iso8691 string
     * @param date the date to convert
     * @return the iso8691
     */
    public static String toISO8691(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return sdf.format(date);
    }

    /**
     * Converts a iso8691 string to date
     * @param date the string to convert
     * @param required if the date is required. If true, an error will be logged if the data cannot be parsed.
     * @return the converted date, or {@link #EMPTY_DATE} if there is a parse error.
     */
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

    /**
     * Gets the current date at this moment.
     * @return the {@link Instant#now()}
     */
    public static Date getCurrentDate() {
        return Date.from(Instant.now());
    }
}
