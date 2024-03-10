package io.banditoz.dohmap.controller.mvc;

import io.banditoz.dohmap.model.Establishment;
import io.banditoz.dohmap.model.EstablishmentInspectionViolation;
import io.banditoz.dohmap.service.EstablishmentService;
import io.banditoz.dohmap.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Controller
@RequestMapping("/establishment")
public class EstablishmentController {
    private final EstablishmentService establishmentService;
    private final StatsService statsService;

    @Autowired
    public EstablishmentController(EstablishmentService establishmentService,
                                   StatsService statsService) {
        this.establishmentService = establishmentService;
        this.statsService = statsService;
    }

    @GetMapping("/{uuid}")
    public String viewEstablishment(@PathVariable UUID uuid, Model model) {
        buildModel(uuid, model);
        return "establishment";
    }

    @GetMapping("/{uuid}/fragment")
    public String viewEstablishmentFragment(@PathVariable UUID uuid, Model model) {
        buildModel(uuid, model);
        model.addAttribute("fragView", true);
        return "fragments/inspection";
    }

    private void buildModel(UUID uuid, Model model) {
        Establishment est = establishmentService.getEstablishmentById(uuid.toString());
        if (est == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Could not find establishment");
        }
        Instant lastSeenCutoff = establishmentService.getLastSeenCutoff().minus(1, ChronoUnit.DAYS);
        EstablishmentInspectionViolation eiv = establishmentService.getIvByEstablishment(est);

        model.addAttribute("establishment", est);
        model.addAttribute("lastSeenCutoff", lastSeenCutoff);
        model.addAttribute("lastRank", eiv.lastRank());
        model.addAttribute("inspections", eiv.inspectionViolations());
        model.addAttribute("count", statsService.getCountOfEstInspVioAsString());
    }
}
