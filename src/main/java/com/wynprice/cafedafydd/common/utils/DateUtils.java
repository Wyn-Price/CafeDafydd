package com.wynprice.cafedafydd.common.utils;

import lombok.extern.log4j.Log4j2;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

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

    /**
     * Converts d1 - d2 to a length string. TODO: MORE DOCUMENTATION
     * <pre>{@code
     *
     * getStringDifference(new Date(0), new Date(1000))         -> <No Time>
     * getStringDifference(new Date(0), new Date(0))            -> 1 second
     * getStringDifference(new Date(0), new Date(5000))         -> 5 seconds
     * getStringDifference(new Date(0), new Date(331000))       -> 5 minutes and 31 seconds
     * getStringDifference(new Date(0), new Date(86731000))     -> 1 day, 5 minutes and 31 seconds
     *
     * }</pre>
     * @param d1 the first date
     * @param d2 the second date, to subtract from the first date
     * @return the difference between the two dates in English terms.
     */
    public static String getStringDifference(Date d1, Date d2) {
        long time = d1.getTime() - d2.getTime();

        long seconds = TimeUnit.MILLISECONDS.toSeconds(time) % 60;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(time) % 60;
        long hours = TimeUnit.MILLISECONDS.toHours(time) % 24;
        long days = TimeUnit.MILLISECONDS.toDays(time); //Will anyone ever game for over days?


        List<String> resultList = new LinkedList<>();

        if(days != 0) {
            resultList.add(days + " day" + (days == 1 ? "" : "s"));
        }
        if(hours != 0) {
            resultList.add(hours + " hour" + (hours == 1 ? "" : "s"));
        }
        if(minutes != 0) {
            resultList.add(minutes + " minute" + (minutes == 1 ? "" : "s"));
        }
        if(seconds != 0) {
            resultList.add(seconds + " second" + (seconds == 1 ? "" : "s"));
        }


        if(resultList.isEmpty()) {
            return "<No Time>";
        }
        if(resultList.size() == 1) {
            return resultList.get(0);
        }


        StringBuilder builder = new StringBuilder();
        Iterator<String> iterator = resultList.iterator();

        boolean first = true;
        while (iterator.hasNext()) {
            String next = iterator.next();
            if (iterator.hasNext()) {
                if(!first) {
                    builder.append(", ");
                }
                builder.append(next);
                first = false;
            } else {
                builder.append(" and ").append(next);
            }
        }

        return builder.toString();
    }
}
