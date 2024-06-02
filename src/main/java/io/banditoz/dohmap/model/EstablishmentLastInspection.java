package io.banditoz.dohmap.model;

import java.time.Instant;
import java.time.LocalDate;

public record EstablishmentLastInspection(Establishment establishment, LocalDate lastInspection) {
    @SuppressWarnings("unused") // for mybatis
    public EstablishmentLastInspection(String id, String name, String address, String city, String state, String zip, String phone, String type,
                                       Instant lastSeen, String sysId, DataSource source, LocalDate lastInspection) {
        this(new Establishment(id, name, address, city, state, zip, phone, type, lastSeen, sysId, source), lastInspection);
    }
}
