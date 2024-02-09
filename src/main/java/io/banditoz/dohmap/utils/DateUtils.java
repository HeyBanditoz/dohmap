package io.banditoz.dohmap.utils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtils {
    public static String getDateAsRfc1123(Instant i) {
        return DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.ofInstant(i, ZoneId.systemDefault()));
    }

    private DateUtils() {}
}
