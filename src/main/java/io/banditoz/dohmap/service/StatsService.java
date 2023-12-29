package io.banditoz.dohmap.service;

import com.google.common.base.Suppliers;
import io.banditoz.dohmap.database.mapper.EstablishmentMapper;
import io.banditoz.dohmap.database.mapper.InspectionMapper;
import io.banditoz.dohmap.database.mapper.ViolationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Service
public class StatsService {
    private final EstablishmentMapper establishmentMapper;
    private final InspectionMapper inspectionMapper;
    private final ViolationMapper violationMapper;
    private final Supplier<String> responseCache;

    @Autowired
    public StatsService(EstablishmentMapper establishmentMapper,
                        InspectionMapper inspectionMapper,
                        ViolationMapper violationMapper) {
        this.establishmentMapper = establishmentMapper;
        this.inspectionMapper = inspectionMapper;
        this.violationMapper = violationMapper;
        responseCache = Suppliers.memoizeWithExpiration(() -> "Currently storing %,d establishments (%,d food/beverage related), %,d inspections, and %,d violations."
                        .formatted(establishmentMapper.getCount(), establishmentMapper.getRestaurantBeverageCount(), inspectionMapper.getCount(), violationMapper.getCount()),
                1, TimeUnit.MINUTES);
    }

    public String getCountOfEstInspVioAsString() {
        return responseCache.get();
    }
}
