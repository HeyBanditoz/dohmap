package io.banditoz.dohmap.model.googlemaps;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PlusCode(
	@JsonProperty("compound_code")
	String compoundCode,

	@JsonProperty("global_code")
	String globalCode
) {
}