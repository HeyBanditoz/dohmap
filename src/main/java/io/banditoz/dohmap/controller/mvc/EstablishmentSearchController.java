package io.banditoz.dohmap.controller.mvc;

import io.banditoz.dohmap.model.EstablishmentSearch;
import io.banditoz.dohmap.model.search.Search;
import io.banditoz.dohmap.model.search.SearchOrder;
import io.banditoz.dohmap.service.SearchService;
import io.banditoz.dohmap.service.StatsService;
import io.banditoz.dohmap.utils.Instrumentation;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.text.DecimalFormat;
import java.util.List;

import static io.banditoz.dohmap.utils.Instrumentation.instrument;

@Controller
@RequestMapping("/establishmentSearch")
public class EstablishmentSearchController {
    private static final Logger log = LoggerFactory.getLogger(EstablishmentSearchController.class);
    private static final DecimalFormat df = new DecimalFormat("#.#");
    private final SearchService searchService;
    private final StatsService statsService;

    @Autowired
    public EstablishmentSearchController(SearchService searchService,
                                         StatsService statsService) {
        this.searchService = searchService;
        this.statsService = statsService;
    }

    @GetMapping
    public String search(@ModelAttribute Search query,
                         @RequestParam(defaultValue = "1") int page,
                         Model model,
                         HttpServletRequest request) {
        if (query.getRealSearch() == null) {
            query.setSearch("");
            query.setOrderBy(SearchOrder.LAST_INSPECTION);
        }
        Instrumentation<Integer> count = instrument(() -> searchService.getCountForQuery(query));
        Instrumentation<List<EstablishmentSearch>> establishments = instrument(() -> searchService.getEstablishmentByWebSearchQuery(query, page));
        String queryString = request == null ? "" : request.getQueryString() == null ? "" : request.getQueryString().replaceAll("&page=\\d+", "");
        model.addAttribute("activePage", "search");
        model.addAttribute("establishments", establishments.getResult());
        model.addAttribute("count", statsService.getCountOfEstInspVioAsString());
        model.addAttribute("cities", searchService.getCities());
        model.addAttribute("totalCount", count.getResult());
        model.addAttribute("maxPages", (int) Math.ceil((double) count.getResult() / 50));
        model.addAttribute("page", page);
        model.addAttribute("startAt", ((page - 1) * 50) + 1);
        model.addAttribute("endAt", Math.min(page * 50, count.getResult()));
        model.addAttribute("countTime", df.format(count.getNanosTook() / 1_000_000D));
        model.addAttribute("searchTime", df.format(establishments.getNanosTook() / 1_000_000D));
        model.addAttribute("pager", request.getRequestURI() + '?' + queryString + "&page=");
        return "establishment_search";
    }
}
