package io.banditoz.dohmap.scraper.utco;

import io.banditoz.dohmap.model.Violation;

record UTCOViolation(String code, String observed, boolean critical, boolean correctedOnSite) {
    Violation.Builder toDbViolation(String inspectionId) {
        return new Violation.Builder()
                .setInspectionId(inspectionId)
                .setCode(code)
                .setObserved(observed)
                .setCritical(critical)
                .setCorrectedOnSite(correctedOnSite);
    }
}
