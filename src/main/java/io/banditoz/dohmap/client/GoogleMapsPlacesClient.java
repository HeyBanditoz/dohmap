package io.banditoz.dohmap.client;

import io.banditoz.dohmap.config.GoogleMapsPlacesClientConfiguration;
import io.banditoz.dohmap.model.googlemaps.places.Places;
import io.banditoz.dohmap.model.googlemaps.places.TextQuery;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(
        name = "GoogleMapsPlacesClient",
        url = "https://places.googleapis.com/v1/",
        configuration = GoogleMapsPlacesClientConfiguration.class
)
public interface GoogleMapsPlacesClient {
    @PostMapping(value = "places:searchText", headers = "X-Goog-FieldMask=places.id,places.location,places.googleMapsUri,places.displayName")
    Places getPlaceByQuery(TextQuery query);
}
