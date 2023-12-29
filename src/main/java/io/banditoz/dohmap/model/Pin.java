package io.banditoz.dohmap.model;

import io.banditoz.dohmap.scraper.model.Establishment;

public record Pin(Establishment establishment, Double lat, Double lng, Integer lastRank) {
    // for mybatis
    public Pin(String id, String name, String address, String city, String state, String zip, String phone, String type,
               Double lat, Double lng, Integer lastRank) {
        this(new Establishment(id, name, address, city, state, zip, phone, type), lat, lng, lastRank);
    }
}
