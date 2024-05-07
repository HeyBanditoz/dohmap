package io.banditoz.dohmap.model;

import java.time.Instant;

public record EstablishmentInspectionCount(Establishment establishment, int inspectionCount) {
    @SuppressWarnings("unused") // for mybatis
    public EstablishmentInspectionCount(String id, String name, String address, String city, String state, String zip, String phone, String type,
               Instant lastSeen, String sysId, DataSource source, int inspectionCount) {
        this(new Establishment(id, name, address, city, state, zip, phone, type, lastSeen, sysId, source), inspectionCount);
    }
}
