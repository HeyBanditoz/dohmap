package io.banditoz.dohmap.controller.mvc;

import io.banditoz.dohmap.model.EstablishmentInspectionCount;
import io.banditoz.dohmap.service.EstablishmentService;
import io.banditoz.dohmap.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/establishmentSearch")
public class EstablishmentSearchController {
    private static final Comparator<EstablishmentInspectionCount> REVERSE_LAST_SEEN = Comparator.comparing((EstablishmentInspectionCount eic) -> eic.establishment().lastSeen()).reversed();
    private final EstablishmentService establishmentService;
    private final StatsService statsService;

    @Autowired
    public EstablishmentSearchController(EstablishmentService establishmentService,
                                         StatsService statsService) {
        this.establishmentService = establishmentService;
        this.statsService = statsService;
    }

    @GetMapping
    public String viewSearchPage(Model model) {
        model.addAttribute("search", new Search());
        model.addAttribute("count", statsService.getCountOfEstInspVioAsString());
        return "establishment_search";
    }

    @PostMapping
    public String processSearch(@ModelAttribute Search query, Model model) {
        List<EstablishmentInspectionCount> establishments = establishmentService.getEstablishmentByWebSearchQuery(query.getSearch(), 101);
        establishments.sort(REVERSE_LAST_SEEN);
        model.addAttribute("establishments", establishments);
        model.addAttribute("count", statsService.getCountOfEstInspVioAsString());
        return "establishment_search";
    }

    public static final class Search {
        private String search;

        public String getSearch() {
            return search;
        }

        public void setSearch(String search) {
            this.search = search;
        }

        @Override
        public String toString() {
            return "Search[" +
                    "search=" + search + ']';
        }
    }
}