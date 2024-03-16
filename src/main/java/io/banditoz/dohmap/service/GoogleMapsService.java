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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class GoogleMapsService {
    private final EstablishmentLocationMapper establishmentLocationMapper;
    private final GoogleMapsClient googleMapsClient;
    private final GoogleMapsPlacesClient googleMapsPlacesClient;
    private final ObjectMapper objectMapper;
    private final boolean usePlacesApi;
    private static final Logger log = LoggerFactory.getLogger(GoogleMapsService.class);

    @Autowired
    public GoogleMapsService(EstablishmentLocationMapper establishmentLocationMapper,
                             GoogleMapsClient googleMapsClient,
                             GoogleMapsPlacesClient googleMapsPlacesClient,
                             ObjectMapper objectMapper,
                             @Value("${dohmap.google-maps.use-places-api:false}") boolean usePlacesApi) {
        this.establishmentLocationMapper = establishmentLocationMapper;
        this.googleMapsClient = googleMapsClient;
        this.googleMapsPlacesClient = googleMapsPlacesClient;
        this.objectMapper = objectMapper;
        this.usePlacesApi = usePlacesApi;

        log.info("Using Google Maps " + (usePlacesApi ? "places" : "geocoding") + " API for geo lookups.");
    }

    @Async
    public void indexEstablishment(Establishment est) {
        if (establishmentLocationMapper.getByEstablishmentId(est.id()) == null) {
            if (usePlacesApi) {
                indexEstablishmentPlacesApi(est);
            } else {
                indexEstablishmentGoogleMapsGeocoding(est);
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
        establishmentLocationMapper.insert(new EstablishmentLocation(
                UuidCreator.getTimeOrderedEpoch().toString(),
                null,
                establishment.id(),
                lat,
                lng,
                Source.MANUAL,
                null), null);
    }

    private void indexEstablishmentGoogleMapsGeocoding(Establishment est) {
        GoogleMapsResponse gmr = googleMapsClient.get(est.getFullAddress());
        if (gmr == null) {
            log.warn("Google Maps Geocoding API returned null/empty object for {}", est);
            return;
        }
        if (gmr.results().isEmpty()) {
            log.warn("Google Maps Geocoding API returned empty list for {}", est);
            return;
        }
        if (gmr.results().size() > 1) {
            log.warn("Google Maps Geocoding API returned more than one response for {}! Using the first response...", est);
        }

        ResultsItem ri = gmr.results().getFirst();
        Location loc = ri.geometry().location();
        insert(new EstablishmentLocation(UuidCreator.getTimeOrderedEpoch().toString(), ri.placeId(), est.id(), loc.lat(), loc.lng(), Source.GOOGLE_MAPS_GEOCODING_API, null), gmr);
    }

    private void indexEstablishmentPlacesApi(Establishment est) {
        Places places = googleMapsPlacesClient.getPlaceByQuery(new TextQuery(est.getNameAndFullAddress()));
        if (places == null || places.places() == null) {
            log.warn("Google Maps Places API returned null/empty object for {}, falling back to geocoding API...", est);
            indexEstablishmentGoogleMapsGeocoding(est);
            return;
        }
        if (places.places().isEmpty()) {
            log.warn("Google Maps Places API returned empty list for {}, falling back to geocoding API...", est);
            indexEstablishmentGoogleMapsGeocoding(est);
            return;
        }
        if (places.places().size() > 1) {
            log.warn("Google Maps Places API returned more than one response for {}! Using the first response...", est);
        }

        Place p = places.places().getFirst();
        insert(new EstablishmentLocation(UuidCreator.getTimeOrderedEpoch().toString(), p.id(), est.id(), p.location().lat(), p.location().lng(), Source.GOOGLE_MAPS_PLACES_API, null), places);
    }

    private void insert(EstablishmentLocation estLoc, Object json) {
        try {
            establishmentLocationMapper.insert(estLoc, objectMapper.writeValueAsString(json));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
