package io.banditoz.dohmap.service;

import com.github.f4b6a3.uuid.UuidCreator;
import io.banditoz.dohmap.database.mapper.InspectionMapper;
import io.banditoz.dohmap.scraper.model.Inspection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InspectionService {
    private final InspectionMapper inspectionMapper;

    @Autowired
    public InspectionService(InspectionMapper inspectionMapper) {
        this.inspectionMapper = inspectionMapper;
    }

    public Inspection getOrCreateInspection(Inspection.Builder candidate) {
        Inspection in = inspectionMapper.getByInspection(candidate.build());
        if (in == null) {
            in = candidate.setId(UuidCreator.getTimeOrderedEpoch().toString()).build();
            inspectionMapper.insertInspection(in);
        }
        return in;
    }

    public List<Inspection> getAllInspectionsByEstablishmentId(String establishmentId) {
        return inspectionMapper.getAllInspectionsByEstablishmentId(establishmentId);
    }
}
