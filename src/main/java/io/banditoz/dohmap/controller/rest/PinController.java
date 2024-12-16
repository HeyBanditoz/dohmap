package io.banditoz.dohmap.controller.rest;

import io.banditoz.dohmap.model.BaseResponse;
import io.banditoz.dohmap.model.Pin;
import io.banditoz.dohmap.service.EstablishmentPinService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pins")
public class PinController {
    private final EstablishmentPinService establishmentPinService;

    @Autowired
    public PinController(EstablishmentPinService establishmentPinService) {
        this.establishmentPinService = establishmentPinService;
    }

    @GetMapping("all")
    @Operation(
            summary = "Internal endpoint for the map renderer.",
            description = "Fetches all establishments that are food service related, and their location on a map."
    )
    ResponseEntity<BaseResponse<List<Pin>>> getAllPins() {
        return ResponseEntity.ok(BaseResponse.of(establishmentPinService.getPins()));
    }
}
