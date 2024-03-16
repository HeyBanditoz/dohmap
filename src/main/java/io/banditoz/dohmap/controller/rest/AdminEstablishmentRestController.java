package io.banditoz.dohmap.controller.rest;

import io.banditoz.dohmap.model.Establishment;
import io.banditoz.dohmap.model.googlemaps.geocode.Location;
import io.banditoz.dohmap.service.EstablishmentService;
import io.banditoz.dohmap.service.GoogleMapsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/v1/establishment")
public class AdminEstablishmentRestController {
    private final GoogleMapsService googleMapsService;
    private final EstablishmentService establishmentService;

    @Autowired
    public AdminEstablishmentRestController(GoogleMapsService googleMapsService,
                                            EstablishmentService establishmentService) {
        this.googleMapsService = googleMapsService;
        this.establishmentService = establishmentService;
    }

    @PutMapping("/manualLocationUpdate/{uuid}")
    public void updateCoordinates(@PathVariable("uuid") UUID uuid, @RequestBody Location location) {
        Establishment est = establishmentService.getEstablishmentById(uuid.toString());
        if (est == null) {
            // TODO need custom exception type...
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Could not find establishment");
        }
        googleMapsService.manualLocationUpdate(est, location.lat(), location.lng());
    }
}
