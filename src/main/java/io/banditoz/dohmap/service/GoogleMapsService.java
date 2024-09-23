package io.banditoz.dohmap.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.f4b6a3.uuid.UuidCreator;
import io.banditoz.dohmap.client.GoogleMapsClient;
import io.banditoz.dohmap.client.GoogleMapsPlacesClient;
import io.banditoz.dohmap.database.mapper.EstablishmentLocationMapper;
import io.banditoz.dohmap.model.EstablishmentLocation;
import io.banditoz.dohmap.model.googlemaps.Source;
import io.banditoz.dohmap.model.googlemaps.geocode.GoogleMapsResponse;
import io.banditoz.dohmap.model.googlemaps.geocode.Location;
import io.banditoz.dohmap.model.googlemaps.geocode.ResultsItem;
import io.banditoz.dohmap.model.Establishment;
import io.banditoz.dohmap.model.googlemaps.places.Place;
import io.banditoz.dohmap.model.googlemaps.places.Places;
import io.banditoz.dohmap.model.googlemaps.places.TextQuery;
import io.micrometer.core.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class GoogleMapsService {
    private final EstablishmentLocationMapper establishmentLocationMapper;
    private final GoogleMapsClient googleMapsClient;
    private final GoogleMapsPlacesClient googleMapsPlacesClient;
    private final ObjectMapper objectMapper;
    private final boolean usePlacesApi;
    private final List<String> allowedTypes;
    private static final Logger log = LoggerFactory.getLogger(GoogleMapsService.class);

    @Autowired
    public GoogleMapsService(EstablishmentLocationMapper establishmentLocationMapper,
                             GoogleMapsClient googleMapsClient,
                             GoogleMapsPlacesClient googleMapsPlacesClient,
                             ObjectMapper objectMapper,
                             @Value("${dohmap.google-maps.use-places-api:false}") boolean usePlacesApi,
                             @Value("${dohmap.google-maps.allowed-establishment-types:false}") List<String> allowedTypes) {
        this.establishmentLocationMapper = establishmentLocationMapper;
        this.googleMapsClient = googleMapsClient;
        this.googleMapsPlacesClient = googleMapsPlacesClient;
        this.objectMapper = objectMapper;
        this.usePlacesApi = usePlacesApi;
        this.allowedTypes = allowedTypes.stream().map(String::toUpperCase).toList();

        log.info("Using Google Maps " + (usePlacesApi ? "places" : "geocoding") + " API for geo lookups.");
        log.info("Allowed establishment types (if contains, case ignored) whose location will be searched {}", this.allowedTypes);
    }

    @Async
    @Timed(percentiles = {0.50, 0.75, 0.90, 0.99})
    public void indexEstablishment(Establishment est) {
        allowedTypes.stream()
                .filter(this::isEstablishmentTypeAllowed)
                .findFirst()
                .ifPresentOrElse(
                        ignored -> _indexEstablishment(est),
                        () -> log.debug("{} wasn't in any of the allowed establishment types: {}", est, allowedTypes)
                );
    }

    public boolean isEstablishmentTypeAllowed(String s) {
        return allowedTypes.stream()
                .anyMatch(type -> type.toUpperCase().contains(s.toUpperCase()));
    }

    private void _indexEstablishment(Establishment est) {
        if (establishmentLocationMapper.getByEstablishmentId(est.id()) == null) {
            if (usePlacesApi) {
                getLocationFromPlacesApi(est, false)
                        .or(() -> {
                            log.warn("Falling back to address only...");
                            return getLocationFromPlacesApi(est, true);
                        })
                        .or(() -> {
                            log.warn("Falling back to geocoding API...");
                            return getLocationFromGeocodingApi(est);
                        })
                        .ifPresentOrElse(this::insert, () -> log.error("Couldn't find location for establishment {}.", est));
            } else {
                getLocationFromGeocodingApi(est)
                        .ifPresentOrElse(this::insert, () -> log.error("Couldn't find location for establishment {}.", est));
            }
        } else {
            log.debug("{}'s location already exists, skipping GoogleMapsService...", est);
        }
    }

    @Transactional
    public void manualLocationUpdate(Establishment establishment, double lat, double lng) {
        if (establishmentLocationMapper.markCurrentLocationDeleted(establishment.id()) == 0) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Update didn't change any rows");
        }
        insert(new LocationRaw(new EstablishmentLocation(
                UuidCreator.getTimeOrderedEpoch().toString(),
                null,
                establishment.id(),
                lat,
                lng,
                Source.MANUAL,
                null), null));
    }

    private Optional<LocationRaw> getLocationFromGeocodingApi(Establishment est) {
        GoogleMapsResponse gmr = googleMapsClient.get(est.getFullAddress());
        if (gmr == null) {
            log.warn("Google Maps Geocoding API returned null/empty object for {}", est);
            return Optional.empty();
        }
        if (gmr.results().isEmpty()) {
            log.warn("Google Maps Geocoding API returned empty list for {}", est);
            return Optional.empty();
        }
        if (gmr.results().size() > 1) {
            log.warn("Google Maps Geocoding API returned more than one response for {}! Using the first response...", est);
        }

        ResultsItem ri = gmr.results().getFirst();
        Location loc = ri.geometry().location();
        EstablishmentLocation el = new EstablishmentLocation(UuidCreator.getTimeOrderedEpoch().toString(), ri.placeId(), est.id(), loc.lat(), loc.lng(), Source.GOOGLE_MAPS_GEOCODING_API, null);
        return LocationRaw.of(el, gmr);
    }

    private Optional<LocationRaw> getLocationFromPlacesApi(Establishment est, boolean addressOnly) {
        Places places = googleMapsPlacesClient.getPlaceByQuery(new TextQuery(addressOnly ? est.getFullAddress() : est.getNameAndFullAddress()));
        if (places == null || places.places() == null) {
            log.warn("Google Maps Places API returned null/empty object for {}... addressOnly={}", est, addressOnly);
            return Optional.empty();
        }
        if (places.places().isEmpty()) {
            log.warn("Google Maps Places API returned empty list for {}... addressOnly={}", est, addressOnly);
            return Optional.empty();
        }
        if (places.places().size() > 1) {
            log.warn("Google Maps Places API returned more than one response for {}! Using the first response... addressOnly={}", est, addressOnly);
        }

        Place p = places.places().getFirst();
        EstablishmentLocation el = new EstablishmentLocation(UuidCreator.getTimeOrderedEpoch().toString(), p.id(),
                est.id(), p.location().lat(), p.location().lng(),
                addressOnly ? Source.GOOGLE_MAPS_GEOCODING_API_ADDRESS_ONLY : Source.GOOGLE_MAPS_PLACES_API, null
        );
        return LocationRaw.of(el, places);
    }

    private void insert(LocationRaw loc) {
        try {
            EstablishmentLocation el = loc.el();
            establishmentLocationMapper.insert(loc.el(), objectMapper.writeValueAsString(loc.raw()));
            log.info("Indexed {}'s location using {} at {},{}", el.establishmentId(), el.source(), el.lat(), el.lng());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private record LocationRaw(EstablishmentLocation el, Object raw) {
        public static Optional<LocationRaw> of(EstablishmentLocation el, Object raw) {
            return Optional.of(new LocationRaw(el, raw));
        }
    }
}
