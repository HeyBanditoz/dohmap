package io.banditoz.dohmap.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.f4b6a3.uuid.UuidCreator;
import dev.failsafe.RateLimiter;
import io.banditoz.dohmap.database.mapper.EstablishmentLocationMapper;
import io.banditoz.dohmap.model.EstablishmentLocation;
import io.banditoz.dohmap.model.googlemaps.GoogleMapsResponse;
import io.banditoz.dohmap.model.googlemaps.Location;
import io.banditoz.dohmap.model.googlemaps.ResultsItem;
import io.banditoz.dohmap.model.Establishment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Duration;

@Service
public class GoogleMapsService {
    private final EstablishmentLocationMapper establishmentLocationMapper;
    private final ObjectMapper objectMapper;
    private final String gmapsApiKey;

    private static final RateLimiter<?> LIMIT = RateLimiter.smoothBuilder(49, Duration.ofSeconds(1)).build();

    @Autowired
    public GoogleMapsService(@Value("${dohmap.google-api-key}") String gmapsApiKey,
                             EstablishmentLocationMapper establishmentLocationMapper,
                             ObjectMapper objectMapper) {
        this.gmapsApiKey = gmapsApiKey;
        this.establishmentLocationMapper = establishmentLocationMapper;
        this.objectMapper = objectMapper;
    }

    @Async
    public void indexEstablishment(Establishment est) {
        acquirePermit();
        if (establishmentLocationMapper.getByEstablishmentId(est.id()) == null) {
            GoogleMapsResponse gmr = getGoogleMapsLocationForEstablishment(est);
            if (gmr.results() != null && !gmr.results().isEmpty()) {
                // eh?
                ResultsItem ri = gmr.results().get(0);
                Location loc = ri.geometry().location();
                EstablishmentLocation estLoc = new EstablishmentLocation(UuidCreator.getTimeOrderedEpoch().toString(), ri.placeId(), est.id(), loc.lat(), loc.lng());
                try {
                    establishmentLocationMapper.insert(estLoc, objectMapper.writeValueAsString(gmr));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public GoogleMapsResponse getGoogleMapsLocationForEstablishment(Establishment establishment) {
        RestTemplate template = new RestTemplate();
        URI url = UriComponentsBuilder.fromHttpUrl("https://maps.googleapis.com/maps/api/geocode/json")
                .queryParam("address", establishment.getFullAddress())
                .queryParam("key", gmapsApiKey)
                .build()
                .encode()
                .toUri();
        ResponseEntity<GoogleMapsResponse> googleMapsResponse = template.getForEntity(url, GoogleMapsResponse.class);
        return googleMapsResponse.getBody();
    }

    private void acquirePermit() {
        try {
            LIMIT.acquirePermit();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
