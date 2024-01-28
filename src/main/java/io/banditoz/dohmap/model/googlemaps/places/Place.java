package io.banditoz.dohmap.model.googlemaps.places;

import io.banditoz.dohmap.model.googlemaps.geocode.Location;

public record Place(String id, Location location, String googleMapsUri, DisplayName displayName) {
}
