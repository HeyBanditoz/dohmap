package io.banditoz.dohmap.controller.mvc;

import io.banditoz.dohmap.scraper.model.Establishment;
import io.banditoz.dohmap.scraper.model.Inspection;
import io.banditoz.dohmap.scraper.model.Violation;
import io.banditoz.dohmap.service.EstablishmentService;
import io.banditoz.dohmap.service.InspectionService;
import io.banditoz.dohmap.service.StatsService;
import io.banditoz.dohmap.service.ViolationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/establishment")
public class EstablishmentController {
    private final EstablishmentService establishmentService;
    private final InspectionService inspectionService;
    private final ViolationService violationService;
    private final StatsService statsService;

    @Autowired
    public EstablishmentController(EstablishmentService establishmentService,
                                   InspectionService inspectionService,
                                   ViolationService violationService,
                                   StatsService statsService) {
        this.establishmentService = establishmentService;
        this.inspectionService = inspectionService;
        this.violationService = violationService;
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
        // TODO n+1 present here
        List<InspectionViolation> list = inspectionService.getAllInspectionsByEstablishmentId(est.id())
                .stream()
                .sorted(Comparator.comparing(Inspection::inspectionDate).reversed())
                .map(i -> InspectionViolation.of(i, violationService.getViolationsByInspection(i.id())))
                .toList();
        Integer lastRank = establishmentService.getLastRankForEstablishment(est.id());

        model.addAttribute("establishment", est);
        model.addAttribute("lastRank", lastRank);
        model.addAttribute("inspections", list);
        model.addAttribute("count", statsService.getCountOfEstInspVioAsString());
    }

    private record InspectionViolation(Inspection i, List<Violation> v, int critCount, int nonCritCount) {
        static InspectionViolation of(Inspection i, List<Violation> vs) {
            int critCount = 0, nonCritCount = 0;
            for (Violation v : vs) {
                if (v.critical()) critCount += v.occurrences();
                else nonCritCount += v.occurrences();
            }
            return new InspectionViolation(i, vs, critCount, nonCritCount);
        }
    }
}
