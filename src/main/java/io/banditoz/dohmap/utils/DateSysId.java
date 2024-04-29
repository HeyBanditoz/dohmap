package io.banditoz.dohmap.utils;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Union-esque type representing an inspection's date and/or its sysId. It's used to determine if an inspection has been
 * seen previously in the system. It alleviates the need to write two mapper methods to get all inspections for an
 * establishment.
 *
 * @param date LocalDate of an inspection.
 * @param sysId sysId of an inspection, this is unique per-inspection.
 */
public record DateSysId(LocalDate date, String sysId) {
    public DateSysId {
        if (date == null && sysId == null) {
            throw new IllegalArgumentException("both parameters cannot be null");
        }
    }

    public static DateSysId ofDate(LocalDate date) {
        Objects.requireNonNull(date, "date cannot be null");
        return new DateSysId(date, null);
    }

    public static DateSysId ofSysId(String sysId) {
        Objects.requireNonNull(sysId, "sysId cannot be null");
        return new DateSysId(null, sysId);
    }
}
