package io.banditoz.dohmap.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.github.f4b6a3.uuid.util.UuidUtil;
import io.banditoz.dohmap.model.Establishment;

import java.time.Instant;
import java.util.UUID;

public record EstablishmentDto(String id, String name, String address, String city, String state, String zip,
                               String phone, String type,
                               @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone="UTC") Instant firstSeen,
                               @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone="UTC") Instant lastSeen,
                               String sysId, String source) {
    public static EstablishmentDto fromEstablishment(Establishment e) {
        return new EstablishmentDto(e.id(), e.name(), e.address(), e.city(), e.state(), e.zip(), e.phone(), e.type(),
                UuidUtil.getInstant(UUID.fromString(e.id())), e.lastSeen(), e.sysId(), e.source().toString());
    }
}
