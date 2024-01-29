package io.banditoz.dohmap.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.f4b6a3.uuid.util.UuidUtil;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Common database entity, whose <code>id</code> column is a
 * <a href="https://buildkite.com/blog/goodbye-integers-hello-uuids">UUIDv7</a>-compatible timestamp.
 */
public interface Entity {
    String id();

    @JsonIgnore // required?
    default String getEntityCreationAsRfc1123() {
        Instant creationDate = UuidUtil.getInstant(UUID.fromString(id()));
        return DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.ofInstant(creationDate, ZoneId.systemDefault()));
    }
}
