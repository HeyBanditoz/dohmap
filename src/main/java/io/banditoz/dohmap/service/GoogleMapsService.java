package io.banditoz.dohmap.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.f4b6a3.uuid.UuidCreator;
import io.banditoz.dohmap.client.GoogleMapsClient;
import io.banditoz.dohmap.database.mapper.EstablishmentLocationMapper;
import io.banditoz.dohmap.model.EstablishmentLocation;
import io.banditoz.dohmap.model.googlemaps.GoogleMapsResponse;
import io.banditoz.dohmap.model.googlemaps.Location;
import io.banditoz.dohmap.model.googlemaps.ResultsItem;
import io.banditoz.dohmap.model.Establishment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class GoogleMapsService {
    private final EstablishmentLocationMapper establishmentLocationMapper;
    private final GoogleMapsClient googleMapsClient;
    private final ObjectMapper objectMapper;

    @Autowired
    public GoogleMapsService(EstablishmentLocationMapper establishmentLocationMapper,
                             GoogleMapsClient googleMapsClient,
                             ObjectMapper objectMapper) {
        this.establishmentLocationMapper = establishmentLocationMapper;
        this.googleMapsClient = googleMapsClient;
        this.objectMapper = objectMapper;
    }

    @Async
    public void indexEstablishment(Establishment est) {
        if (establishmentLocationMapper.getByEstablishmentId(est.id()) == null) {
            GoogleMapsResponse gmr = googleMapsClient.get(est.getFullAddress());
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
}
