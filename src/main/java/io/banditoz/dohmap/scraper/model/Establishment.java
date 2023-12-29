package io.banditoz.dohmap.scraper.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record Establishment(String id, String name, String address, String city, String state, String zip, String phone, String type) {
    @JsonIgnore
    public String getFullAddress() {
        return "%s, %s, %s %s".formatted(address, city, state, zip);
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

        public Establishment build() {
            return new Establishment(id, name, address, city, state, zip, phone, type);
        }
    }
}
