package io.banditoz.dohmap.service;

import com.github.f4b6a3.uuid.UuidCreator;
import com.google.common.base.Suppliers;
import io.banditoz.dohmap.database.mapper.EstablishmentMapper;
import io.banditoz.dohmap.database.mapper.EstablishmentRankMapper;
import io.banditoz.dohmap.model.EstablishmentRank;
import io.banditoz.dohmap.model.Establishment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Service
public class EstablishmentService {
    private final EstablishmentMapper establishmentMapper;
    private final EstablishmentRankMapper establishmentRankMapper;
    private final GoogleMapsService googleMapsService;
    private final Supplier<Instant> lastSeenCache;

    @Autowired
    public EstablishmentService(EstablishmentMapper establishmentMapper,
                                EstablishmentRankMapper establishmentRankMapper,
                                GoogleMapsService googleMapsService) {
        this.establishmentMapper = establishmentMapper;
        this.establishmentRankMapper = establishmentRankMapper;
        this.googleMapsService = googleMapsService;
        this.lastSeenCache = Suppliers.memoizeWithExpiration(establishmentMapper::get50thLatestLastSeen, 12, TimeUnit.HOURS);
    }

    public Establishment getOrCreateEstablishment(Establishment.Builder candidate) {
        Establishment est = establishmentMapper.getByEstablishment(candidate.build());
        if (est == null) {
            est = candidate.setId(UuidCreator.getTimeOrderedEpoch().toString()).build();
            establishmentMapper.insertEstablishment(est);
        } else {
            establishmentMapper.updateLastSeen(est);
        }
        // also fetch location from Google Maps, if needed
        googleMapsService.indexEstablishment(est);
        return est;
    }

    public Establishment getEstablishmentById(String id) {
        return establishmentMapper.getById(id);
    }

    public Integer getLastRankForEstablishment(String establishmentId) {
        return establishmentMapper.getLastRankForEstablishment(establishmentId);
    }

    public void indexEstablishmentRank(Establishment e, Integer rank) {
        if (rank == null) {
            return;
        }
        EstablishmentRank establishmentRank = new EstablishmentRank(UuidCreator.getTimeOrderedEpoch().toString(), e.id(), rank);
        establishmentRankMapper.insert(establishmentRank);
    }

    public Instant getLastSeenCutoff() {
        return lastSeenCache.get();
    }
}
