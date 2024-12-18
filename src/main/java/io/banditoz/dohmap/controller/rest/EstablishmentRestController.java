package io.banditoz.dohmap.controller.rest;

import io.banditoz.dohmap.model.BaseResponse;
import io.banditoz.dohmap.model.Establishment;
import io.banditoz.dohmap.model.dto.EstablishmentInspectionViolationDto;
import io.banditoz.dohmap.service.EstablishmentService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/establishment")
public class EstablishmentRestController {
    private final EstablishmentService establishmentService;

    @Autowired
    public EstablishmentRestController(EstablishmentService establishmentService) {
        this.establishmentService = establishmentService;
    }

    @GetMapping(value = "/{uuid}", produces = "application/json")
    @Operation(summary = "Get establishment by ID, and its inspections and violations within.")
    public ResponseEntity<BaseResponse<EstablishmentInspectionViolationDto>> getEstablishmentAndInspectionsViolations(@PathVariable UUID uuid) {
        Establishment est = establishmentService.getEstablishmentById(uuid.toString());
        if (est == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Could not find establishment");
        }
        return ResponseEntity.ok(BaseResponse.of(establishmentService.getIvDtoByEstablishment(est)));
    }
}
