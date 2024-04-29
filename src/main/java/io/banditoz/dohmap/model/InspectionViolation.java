package io.banditoz.dohmap.model;

import java.util.List;

import static java.lang.Math.max;

public record InspectionViolation(Inspection i, List<Violation> v, int critCount, int nonCritCount) {
    public static InspectionViolation of(Inspection i, List<Violation> vs) {
        int critCount = 0, nonCritCount = 0;
        for (Violation v : vs) {
            if (v.critical()) critCount += max(v.occurrences(), 1);
            else nonCritCount += max(v.occurrences(), 1);
        }
        return new InspectionViolation(i, vs, critCount, nonCritCount);
    }
}
