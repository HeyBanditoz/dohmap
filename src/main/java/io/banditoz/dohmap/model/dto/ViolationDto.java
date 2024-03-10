package io.banditoz.dohmap.model.dto;

import io.banditoz.dohmap.model.Violation;

public record ViolationDto(String id, String code, String observed, Integer points, boolean critical,
                           int occurrences, boolean correctedOnSite, String publicHealthRationale) {
    public static ViolationDto fromViolation(Violation v) {
        return new ViolationDto(v.id(), v.code(), v.observed(), v.points(), v.critical(), v.occurrences(),
                v.correctedOnSite(), v.publicHealthRationale());
    }
}
