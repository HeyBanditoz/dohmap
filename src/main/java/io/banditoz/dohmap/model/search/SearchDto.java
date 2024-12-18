package io.banditoz.dohmap.model.search;

import io.banditoz.dohmap.model.PageMetadata;
import io.banditoz.dohmap.model.dto.EstablishmentSearchDto;

import java.util.List;

public record SearchDto(List<EstablishmentSearchDto> results, PageMetadata pageMetadata, Search query) {
}
