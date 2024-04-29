package io.banditoz.dohmap.scraper.utco;

import io.banditoz.dohmap.model.Inspection;

import java.time.LocalDate;

record UTCOInspection(String unid, LocalDate inspectionDate, String inspectionType) {
    Inspection.Builder toDbInspection(String establishmentId) {
        return new Inspection.Builder()
                .setInspectionDate(inspectionDate)
                .setInspectionType(inspectionType)
                .setEstablishmentId(establishmentId)
                .setSysId(unid);
    }

    String getUrlToViolations() {
        return "https://www.inspectionsonline.us/UT/UtahProvo/inspect.nsf/(ag_dspPubDetail)?OpenAgent&pUNID=" + unid;
    }
}
