package io.banditoz.dohmap.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableConfigurationProperties
public class CityMappingsConfiguration {
    @Bean("citiesMap")
    @ConfigurationProperties(prefix = "dohmap.city-mappings")
    public Map<String, String> cityMappings() {
        return new HashMap<>();
    }
}
