package io.banditoz.dohmap.model.googlemaps;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public record ResultsItem(
	@JsonProperty("formatted_address")
	String formattedAddress,

	@JsonProperty("types")
	List<String> types,

	@JsonProperty("partial_match")
	boolean partialMatch,

	@JsonProperty("geometry")
	Geometry geometry,

	@JsonProperty("address_components")
	List<AddressComponentsItem> addressComponents,

	@JsonProperty("plus_code")
	PlusCode plusCode,

	@JsonProperty("place_id")
	String placeId
) {
}