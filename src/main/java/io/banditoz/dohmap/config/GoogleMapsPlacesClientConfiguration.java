package io.banditoz.dohmap.config;

import dev.failsafe.RateLimiter;
import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import java.time.Duration;

public class GoogleMapsPlacesClientConfiguration {
    /**
     * TODO ratelimit should support by API method, per Google Maps' docs:<br>
     * <i>Places API (New): Rate limit is 600 QPM (requests per minute) per API method per project.
     * Meaning each API method has a separate quota.</i>
     */
    private final RateLimiter<?> limit = RateLimiter.smoothBuilder(60, Duration.ofSeconds(1)).build();
    private final String gmapsApiKey;

    public GoogleMapsPlacesClientConfiguration(@Value("${dohmap.google-maps.api-key}") String gmapsApiKey) {
        this.gmapsApiKey = gmapsApiKey;
    }

    @Bean("placesRateLimitInterceptor")
    public RequestInterceptor rateLimitInterceptor() {
        return requestTemplate -> {
            try {
                limit.acquirePermit();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };
    }

    @Bean("placesAuthInterceptor")
    public RequestInterceptor authInterceptor() {
        return requestTemplate -> requestTemplate.header("X-Goog-Api-Key", gmapsApiKey);
    }
}
