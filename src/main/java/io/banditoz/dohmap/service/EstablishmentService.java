package io.banditoz.dohmap.service;

import com.github.f4b6a3.uuid.UuidCreator;
import com.google.common.base.Suppliers;
import io.banditoz.dohmap.database.mapper.EstablishmentMapper;
import io.banditoz.dohmap.database.mapper.EstablishmentRankMapper;
import io.banditoz.dohmap.model.*;
import io.banditoz.dohmap.model.EstablishmentInspectionViolation;
import io.banditoz.dohmap.model.dto.EstablishmentDto;
import io.banditoz.dohmap.model.dto.EstablishmentInspectionViolationDto;
import io.banditoz.dohmap.model.dto.InspectionDto;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Service
public class EstablishmentService {
    private final EstablishmentMapper establishmentMapper;
    private final EstablishmentRankMapper establishmentRankMapper;
    private final InspectionService inspectionService;
    private final ViolationService violationService;
    private final GoogleMapsService googleMapsService;
    private final Supplier<Instant> lastSeenCache;
    private static final Logger log = LoggerFactory.getLogger(EstablishmentService.class);
    private final MeterRegistry registry;

    @Autowired
    public EstablishmentService(EstablishmentMapper establishmentMapper,
                                EstablishmentRankMapper establishmentRankMapper,
                                InspectionService inspectionService,
                                ViolationService violationService,
                                GoogleMapsService googleMapsService,
                                MeterRegistry registry) {
        this.establishmentMapper = establishmentMapper;
        this.establishmentRankMapper = establishmentRankMapper;
        this.inspectionService = inspectionService;
        this.violationService = violationService;
        this.googleMapsService = googleMapsService;
        this.lastSeenCache = Suppliers.memoizeWithExpiration(establishmentMapper::get50thLatestLastSeen, 12, TimeUnit.HOURS);
        this.registry = registry;
    }

    public Establishment getOrCreateEstablishment(Establishment.Builder candidate) {
        Establishment est = establishmentMapper.getByEstablishment(candidate.build());
        if (est == null) {
            est = candidate.setId(UuidCreator.getTimeOrderedEpoch().toString()).build();
            establishmentMapper.insertEstablishment(est);
            log.debug("{} was created!", est);
            registry.counter("dohmap_establishment_created", "source", est.source().name()).increment();
        } else {
            log.debug("{} already exists. Updating last seen...", est);
            establishmentMapper.updateLastSeen(est);
            registry.counter("dohmap_establishment_exists", "source", est.source().name()).increment();
        }
        // also fetch location from Google Maps, if needed
        googleMapsService.indexEstablishment(est);
        return est;
    }

    public EstablishmentInspectionViolation getIvByEstablishment(Establishment e) {
        // TODO n+1 present here
        List<InspectionViolation> list = inspectionService.getAllInspectionsByEstablishmentId(e.id())
                .stream()
                .sorted(Comparator.comparing(Inspection::inspectionDate).reversed())
                .map(i -> InspectionViolation.of(i, violationService.getViolationsByInspection(i.id())))
                .toList();
        Integer lastRank = getLastRankForEstablishment(e.id());
        return new EstablishmentInspectionViolation(e, list, lastRank);
    }

    public EstablishmentInspectionViolationDto getIvDtoByEstablishment(Establishment e) {
        EstablishmentInspectionViolation eiv = getIvByEstablishment(e);

        EstablishmentDto eDto = EstablishmentDto.fromEstablishment(eiv.establishment());
        List<InspectionDto> iDtos = eiv.inspectionViolations()
                .stream()
                .sorted(Comparator.comparing(i -> i.i().inspectionDate(), Comparator.reverseOrder()))
                .map(InspectionDto::fromInspectionViolation)
                .toList();
        return new EstablishmentInspectionViolationDto(eDto, eiv.lastRank(), iDtos);
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

    public List<EstablishmentLastInspection> getEstablishmentByWebSearchQuery(String query, int limit) {
        return establishmentMapper.getEstablishmentByWebSearchQuery(query, limit);
    }
}
