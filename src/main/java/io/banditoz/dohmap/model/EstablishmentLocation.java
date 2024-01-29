package io.banditoz.dohmap.model;

import io.banditoz.dohmap.model.googlemaps.Source;

import java.time.LocalDateTime;

public record EstablishmentLocation(String id, String placeId, String establishmentId, double lat, double lng,
                                    Source source, LocalDateTime deletedOn) implements Entity {
}
