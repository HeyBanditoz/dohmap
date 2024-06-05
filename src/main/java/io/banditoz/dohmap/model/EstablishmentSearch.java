package io.banditoz.dohmap.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.Instant;
import java.time.LocalDate;

public record EstablishmentSearch(Establishment establishment, int inspectionCount, int violationCount, LocalDate lastInspection) {
    @SuppressWarnings("unused") // for mybatis
    public EstablishmentSearch(String id, String name, String address, String city, String state, String zip, String phone, String type,
                               Instant lastSeen, String sysId, DataSource source, int inspectionCount, int violationCount, LocalDate lastInspection) {
        this(new Establishment(id, name, address, city, state, zip, phone, type, lastSeen, sysId, source), inspectionCount, violationCount, lastInspection);
    }

    @JsonIgnore // just in case
    public String getAverageViolationCountPerInspection() {
        return String.valueOf((double) Math.round(((double) violationCount / inspectionCount) * 10) / 10);
    }
}
