package io.banditoz.dohmap.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.base.Suppliers;
import io.banditoz.dohmap.database.mapper.SearchMapper;
import io.banditoz.dohmap.model.EstablishmentSearch;
import io.banditoz.dohmap.model.search.Search;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Service
public class SearchService {
    private final SearchMapper searchMapper;
    private final Cache<Search, Integer> rowCountCache = Caffeine.newBuilder()
            .expireAfterWrite(2, TimeUnit.HOURS)
            .maximumSize(1000)
            .build();
    private final Supplier<List<String>> cities;

    @Autowired
    public SearchService(SearchMapper searchMapper) {
        this.searchMapper = searchMapper;
        this.cities = Suppliers.memoizeWithExpiration(searchMapper::getCities, 1, TimeUnit.HOURS);
    }

    public List<EstablishmentSearch> getEstablishmentByWebSearchQuery(Search search, int page) {
        if (rowCountCache.asMap().get(search) == 0) {
            return Collections.emptyList(); // don't run the search query if the row count was determined to be zero
        }
        return searchMapper.getEstablishmentByWebSearchQuery(search, 50, (page - 1) * 50);
    }

    public List<String> getCities() {
        return cities.get();
    }

    public int getCountForQuery(Search query) {
        return rowCountCache.get(query, searchMapper::countWebSearchQuery);
    }
}
