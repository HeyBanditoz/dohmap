package io.banditoz.dohmap.model.googlemaps;

public enum Source {
    /** <a href="https://developers.google.com/maps/documentation/geocoding/requests-geocoding">Google Maps' Geocoding API</a> */
    GOOGLE_MAPS_GEOCODING_API,
    /** <a href="https://developers.google.com/maps/documentation/places/web-service/text-search">Google Maps' Places Text Search API</a> */
    GOOGLE_MAPS_PLACES_API,
    /** Manually added. */
    MANUAL,
    /**
     * <a href="https://developers.google.com/maps/documentation/geocoding/requests-geocoding">Google Maps' Geocoding API</a>
     * but search fell back to omitting the establishment's name.
     */
    GOOGLE_MAPS_GEOCODING_API_ADDRESS_ONLY
}
