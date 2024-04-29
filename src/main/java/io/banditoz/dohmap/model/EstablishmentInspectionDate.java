package io.banditoz.dohmap.model;

import io.banditoz.dohmap.utils.DateSysId;

import java.time.LocalDate;

public record EstablishmentInspectionDate(String establishment, LocalDate inspectionDate, String inspectionSysId) {
    public DateSysId toDateOrSysId() {
        return new DateSysId(inspectionDate, inspectionSysId);
    }
}
