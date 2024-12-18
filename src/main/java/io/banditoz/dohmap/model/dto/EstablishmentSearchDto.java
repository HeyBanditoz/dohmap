package io.banditoz.dohmap.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.banditoz.dohmap.model.EstablishmentSearch;

import java.time.LocalDate;

public record EstablishmentSearchDto(EstablishmentDto establishment,
                                     int inspectionCount,
                                     int violationCount,
                                     @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd", timezone="GMT") LocalDate lastInspection) {
    public static EstablishmentSearchDto ofSearch(EstablishmentSearch search) {
        return new EstablishmentSearchDto(EstablishmentDto.fromEstablishment(search.establishment()),search.inspectionCount(), search.violationCount(), search.lastInspection());
    }
}
