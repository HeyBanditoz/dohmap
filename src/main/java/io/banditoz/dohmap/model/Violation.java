package io.banditoz.dohmap.model;

public record Violation(String id, String inspectionId, String code, String observed, Integer points, boolean critical,
                        int occurrences, boolean correctedOnSite, String publicHealthRationale) implements Entity {

    public static final class Builder {
        private String id;
        private String inspectionId;
        private String code;
        private String observed;
        private Integer points;
        private boolean critical;
        private int occurrences;
        private boolean correctedOnSite;
        private String publicHealthRationale;

        public Builder setId(String val) {
            id = val;
            return this;
        }

        public Builder setInspectionId(String val) {
            inspectionId = val;
            return this;
        }

        public Builder setCode(String val) {
            code = val;
            return this;
        }

        public Builder setObserved(String val) {
            observed = val;
            return this;
        }

        public Builder setPoints(Integer val) {
            points = val;
            return this;
        }

        public Builder setCritical(boolean val) {
            critical = val;
            return this;
        }

        public Builder setOccurrences(int val) {
            occurrences = val;
            return this;
        }

        public Builder setCorrectedOnSite(boolean val) {
            correctedOnSite = val;
            return this;
        }

        public Builder setPublicHealthRationale(String val) {
            publicHealthRationale = val;
            return this;
        }

        public Violation build() {
            return new Violation(id, inspectionId, code, observed, points, critical, occurrences, correctedOnSite, publicHealthRationale);
        }
    }
}
