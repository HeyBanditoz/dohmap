package io.banditoz.dohmap.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles renaming of cities on-the-fly.
 */
@Service
public class CityRemapper {
    private final Map<String, String> cities;

    @Autowired
    public CityRemapper(@Qualifier("citiesMap") Map<String, String> cities) {
        Map<String, String> fixedCities = new HashMap<>(cities.size());
        for (Map.Entry<String, String> cityMapping : cities.entrySet()) {
            String key = cityMapping.getKey();
            fixedCities.put(key.substring(1, key.length() - 1), cityMapping.getValue());
        }
        this.cities = fixedCities;
    }

    public String rename(String s) {
        return cities.getOrDefault(s, s);
    }

    public boolean shouldRename(String s) {
        return cities.containsKey(s);
    }
}
