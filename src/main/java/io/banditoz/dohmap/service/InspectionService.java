package io.banditoz.dohmap.service;

import com.github.f4b6a3.uuid.UuidCreator;
import io.banditoz.dohmap.database.mapper.InspectionMapper;
import io.banditoz.dohmap.model.DataSource;
import io.banditoz.dohmap.model.EstablishmentInspectionDate;
import io.banditoz.dohmap.model.Inspection;
import io.banditoz.dohmap.utils.DateSysId;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class InspectionService {
    private final InspectionMapper inspectionMapper;
    private final MeterRegistry registry;

    @Autowired
    public InspectionService(InspectionMapper inspectionMapper,
                             MeterRegistry registry) {
        this.inspectionMapper = inspectionMapper;
        this.registry = registry;
    }

    public Inspection getOrCreateInspection(Inspection.Builder candidate) {
        Inspection in = inspectionMapper.getByInspection(candidate.build());
        if (in == null) {
            in = candidate.setId(UuidCreator.getTimeOrderedEpoch().toString()).build();
            inspectionMapper.insertInspection(in);
            registry.counter("dohmap_inspection_created").increment();
        } else {
            registry.counter("dohmap_inspection_exists").increment();
        }
        return in;
    }

    public List<Inspection> getAllInspectionsByEstablishmentId(String establishmentId) {
        return inspectionMapper.getAllInspectionsByEstablishmentId(establishmentId);
    }

    // this whole method is probably way too complicated...
    public Map<String, List<DateSysId>> getAllEstablishmentStoredInspectionDates(DataSource source) {
        return inspectionMapper.getEstablishmentInspectionDates(source)
                .stream()
                .collect(Collectors.groupingBy(EstablishmentInspectionDate::establishment))
                .entrySet()
                .stream()
                .map(listEntry ->
                        new Holder(listEntry.getKey(), listEntry.getValue().stream()
                                .map(EstablishmentInspectionDate::toDateOrSysId)
                                .toList())
                )
                .collect(Collectors.toMap(Holder::establishmentId, Holder::inspections));
    }

    private record Holder(String establishmentId, List<DateSysId> inspections) {}
}
