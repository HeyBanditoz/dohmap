package io.banditoz.dohmap.model.googlemaps;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public record AddressComponentsItem(
	@JsonProperty("types")
	List<String> types,

	@JsonProperty("short_name")
	String shortName,

	@JsonProperty("long_name")
	String longName
) {
}