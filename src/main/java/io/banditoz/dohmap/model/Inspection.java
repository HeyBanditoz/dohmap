package io.banditoz.dohmap.model;

import java.time.LocalDate;

public record Inspection(String id, String establishmentId, LocalDate inspectionDate, String inspectionType, Integer score, String sysId) implements Entity {
    public static final class Builder {
        private String id;
        private String establishmentId;
        private LocalDate inspectionDate;
        private String inspectionType;
        private Integer score;
        private String sysId;

        public Builder() {
        }

        public Builder setId(String val) {
            id = val;
            return this;
        }

        public Builder setEstablishmentId(String val) {
            establishmentId = val;
            return this;
        }

        public Builder setInspectionDate(LocalDate val) {
            inspectionDate = val;
            return this;
        }

        public Builder setInspectionType(String val) {
            inspectionType = val;
            return this;
        }

        public Builder setScore(Integer val) {
            score = val;
            return this;
        }

        public Builder setSysId(String val) {
            sysId = val;
            return this;
        }

        public Inspection build() {
            return new Inspection(id, establishmentId, inspectionDate, inspectionType, score, sysId);
        }
    }
}
