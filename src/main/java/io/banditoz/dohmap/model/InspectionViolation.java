package io.banditoz.dohmap.model;

import java.util.List;

public record InspectionViolation(Inspection i, List<Violation> v, int critCount, int nonCritCount) {
    public static InspectionViolation of(Inspection i, List<Violation> vs) {
        int critCount = 0, nonCritCount = 0;
        for (Violation v : vs) {
            if (v.critical()) critCount += v.occurrences();
            else nonCritCount += v.occurrences();
        }
        return new InspectionViolation(i, vs, critCount, nonCritCount);
    }
}
