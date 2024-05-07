package io.banditoz.dohmap.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.banditoz.dohmap.utils.DateUtils;

import java.time.Instant;

public record Establishment(String id, String name, String address, String city, String state, String zip, String phone,
                            String type, Instant lastSeen, String sysId, DataSource source) implements Entity {
    @JsonIgnore
    public String getFullAddress() {
        return "%s, %s, %s %s".formatted(address, city, state, zip);
    }

    @JsonIgnore
    public String getNameAndFullAddress() {
        return "%s, %s".formatted(name, getFullAddress());
    }

    @JsonIgnore
    public boolean isSaltLakeCounty() {
        return source == DataSource.SALT_LAKE_COUNTY_CDP;
    }

    @JsonIgnore
    public boolean isUtahCounty() {
        return source == DataSource.UTAH_COUNTY_PARAGON;
    }

    @JsonIgnore
    public String getLastSeenAsRfc1123() {
        return DateUtils.getDateAsRfc1123(lastSeen());
    }

    public static final class Builder {
        private String id;
        private String name;
        private String address;
        private String city;
        private String state;
        private String zip;
        private String phone;
        private String type;
        private String sysId;
        private DataSource source;

        public Builder() {
        }

        public Builder setId(String val) {
            id = val;
            return this;
        }

        public Builder setName(String val) {
            name = val;
            return this;
        }

        public Builder setAddress(String val) {
            address = val;
            return this;
        }

        public Builder setCity(String val) {
            city = val;
            return this;
        }

        public Builder setState(String val) {
            state = val;
            return this;
        }

        public Builder setZip(String val) {
            zip = val;
            return this;
        }

        public Builder setPhone(String val) {
            phone = val;
            return this;
        }

        public Builder setType(String val) {
            type = val;
            return this;
        }

        public Builder setSysId(String val) {
            sysId = val;
            return this;
        }

        public Builder setSource(DataSource val) {
            source = val;
            return this;
        }

        public Establishment build() {
            return new Establishment(id, name, address, city, state, zip, phone, type, null, sysId, source);
        }
    }
}
