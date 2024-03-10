package io.banditoz.dohmap.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.banditoz.dohmap.model.InspectionViolation;
import io.banditoz.dohmap.model.Violation;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

public record InspectionDto(String id,
                            @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd", timezone="GMT") LocalDate inspectionDate,
                            String inspectionType, Integer score, List<ViolationDto> violations) {
    public static InspectionDto fromInspectionViolation(InspectionViolation iv) {
        // TODO address wasted cycles in InspectionViolations int count...
        List<ViolationDto> violations = iv.v()
                .stream()
                .sorted(Comparator.comparing(Violation::code))
                .map(ViolationDto::fromViolation)
                .toList();
        return new InspectionDto(iv.i().id(), iv.i().inspectionDate(), iv.i().inspectionType(), iv.i().score(), violations);
    }
}
