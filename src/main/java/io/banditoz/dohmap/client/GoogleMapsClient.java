package io.banditoz.dohmap.client;

import io.banditoz.dohmap.config.GoogleMapsClientConfiguration;
import io.banditoz.dohmap.model.googlemaps.geocode.GoogleMapsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "GoogleMapsClient",
        url = "https://maps.googleapis.com/maps/api/geocode/",
        configuration = GoogleMapsClientConfiguration.class
)
public interface GoogleMapsClient {
    @GetMapping("json?address={address}")
    GoogleMapsResponse get(@RequestParam(value = "address") String address);
}
