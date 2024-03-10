package io.banditoz.dohmap.model.dto;

import java.util.List;

public record EstablishmentInspectionViolationDto(EstablishmentDto establishment,
                                                  Integer lastRank,
                                                  List<InspectionDto> inspections) {
}
