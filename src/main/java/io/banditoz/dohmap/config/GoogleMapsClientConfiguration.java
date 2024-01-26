package io.banditoz.dohmap.config;

import dev.failsafe.RateLimiter;
import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class GoogleMapsClientConfiguration {
    private final String gmapsApiKey;
    private final RateLimiter<?> limit = RateLimiter.smoothBuilder(50, Duration.ofSeconds(1)).build();

    @Autowired
    public GoogleMapsClientConfiguration(@Value("${dohmap.google-api-key}") String gmapsApiKey) {
        this.gmapsApiKey = gmapsApiKey;
    }

    @Bean
    public RequestInterceptor apiKeyInterceptor() {
        return requestTemplate -> requestTemplate.uri(requestTemplate.url() + "&key=" + gmapsApiKey);
    }

    @Bean
    public RequestInterceptor rateLimitInterceptor() {
        return requestTemplate -> {
            try {
                limit.acquirePermit();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };
    }
}
