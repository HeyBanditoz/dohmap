package io.banditoz.dohmap.controller.rest;

import io.banditoz.dohmap.model.BaseResponse;
import io.banditoz.dohmap.model.DataSource;
import io.banditoz.dohmap.model.EstablishmentSearch;
import io.banditoz.dohmap.model.PageMetadata;
import io.banditoz.dohmap.model.dto.EstablishmentSearchDto;
import io.banditoz.dohmap.model.search.Search;
import io.banditoz.dohmap.model.search.SearchDto;
import io.banditoz.dohmap.model.search.SearchOrder;
import io.banditoz.dohmap.service.SearchService;
import io.banditoz.dohmap.utils.Instrumentation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static io.banditoz.dohmap.utils.Instrumentation.instrument;

@RestController
@RequestMapping("/api/v1/establishmentSearch")
public class EstablishmentSearchRestController {
    private final SearchService searchService;

    public EstablishmentSearchRestController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping
    @Parameters({
            @Parameter(name = "search", example = "McDonalds", description = "The search query. Fuzzy matching per Postgres' full-text search. For details, see https://adamj.eu/tech/2024/01/03/postgresql-full-text-search-websearch/ !!This may be blank to search all!!"),
            @Parameter(name = "cities", example = "SALT LAKE CITY,SOUTH JORDAN", description = "Case-sensitive (in most cases, uppercase) comma-separated list of cities to match for."),
            @Parameter(
                    name = "orderBy",
                    example = "LAST_INSPECTION",
                    description = "Sorting to apply to search.",
                    schema = @Schema(
                            implementation = SearchOrder.class
                    )
            ),
            @Parameter(
                    name = "sources",
                    example = "SALT_LAKE_COUNTY_CDP",
                    description = "Comma-separated list of datasources to match.",
                    array = @ArraySchema(
                            schema = @Schema(
                                    nullable = true,
                                    implementation = DataSource.class
                            )
                    )
            ),
            @Parameter(name = "minimumInspCount", example = "0", description = "Only fetches establishments where its inspection count is greater than or equal to the value here. Try combining this with MOST_VIOLATIONS_AVG ordering.")
    })
    public ResponseEntity<BaseResponse<SearchDto>> search(@ModelAttribute @Parameter(hidden = true /* defined above */) Search query,
                                                          @RequestParam(defaultValue = "1") @Valid @Min(1) @Parameter(description = "The page number, from 1 to maxPages") int page) {
        if (query.getRealSearch() == null) {
            query.setSearch("");
            query.setOrderBy(SearchOrder.LAST_INSPECTION);
        }
        if (query.getOrderBy() == null) {
            query.setOrderBy(SearchOrder.LAST_INSPECTION);
        }
        Instrumentation<Integer> count = instrument(() -> searchService.getCountForQuery(query));
        Instrumentation<List<EstablishmentSearch>> establishments = instrument(() -> searchService.getEstablishmentByWebSearchQuery(query, page));
        PageMetadata pageMetadata = new PageMetadata(page, (int) Math.ceil((double) count.getResult() / 50), ((page - 1) * 50) + 1, count.getNanosTook() / 1_000_000, establishments.getNanosTook() / 1_000_000);
        List<EstablishmentSearchDto> establishmentSearchDtos = establishments.getResult().stream()
                .map(EstablishmentSearchDto::ofSearch)
                .toList();
        return ResponseEntity.ok(BaseResponse.of(new SearchDto(establishmentSearchDtos, pageMetadata, query)));
    }
}
