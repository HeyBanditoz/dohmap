package io.banditoz.dohmap.controller.rest;

import io.banditoz.dohmap.model.BaseResponse;
import io.banditoz.dohmap.service.SearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cities")
public class CitiesRestController {
    private final SearchService searchService;

    public CitiesRestController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping(produces = "application/json")
    public ResponseEntity<BaseResponse<CitiesResponse>> getAllCities() {
        return ResponseEntity.ok(BaseResponse.of(new CitiesResponse(searchService.getCities())));
    }

    public record CitiesResponse(List<String> cities) {} // for swagger.. I guess
}
