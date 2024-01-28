package io.banditoz.dohmap.model.googlemaps.geocode;

import com.fasterxml.jackson.annotation.JsonAlias;

public record Location(
	@JsonAlias("latitude")
	double lat,

	@JsonAlias("longitude")
	double lng
) {
}