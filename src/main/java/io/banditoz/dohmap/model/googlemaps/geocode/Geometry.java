package io.banditoz.dohmap.model.googlemaps.geocode;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Geometry(
	@JsonProperty("location")
	Location location,

	@JsonProperty("location_type")
	String locationType
) {
}