package io.banditoz.dohmap.model.googlemaps.geocode;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public record GoogleMapsResponse(
	@JsonProperty("results")
	List<ResultsItem> results,

	@JsonProperty("status")
	String status
) {
}