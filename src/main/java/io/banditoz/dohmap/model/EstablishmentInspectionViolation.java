package io.banditoz.dohmap.model;

import java.util.List;

public record EstablishmentInspectionViolation(Establishment establishment,
                                               List<InspectionViolation> inspectionViolations,
                                               Integer lastRank) {
}
