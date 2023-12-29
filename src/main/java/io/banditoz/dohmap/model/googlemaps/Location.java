package io.banditoz.dohmap.model.googlemaps;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Location(
	@JsonProperty("lat")
	double lat,

	@JsonProperty("lng")
	double lng
) {
}