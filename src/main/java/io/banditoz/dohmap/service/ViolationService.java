package io.banditoz.dohmap.service;

import com.github.f4b6a3.uuid.UuidCreator;
import io.banditoz.dohmap.database.mapper.ViolationMapper;
import io.banditoz.dohmap.scraper.model.Violation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ViolationService {
    private final ViolationMapper violationMapper;

    @Autowired
    public ViolationService(ViolationMapper violationMapper) {
        this.violationMapper = violationMapper;
    }

    public Violation getOrCreationViolation(Violation.Builder candidate) {
        Violation v = violationMapper.getByViolation(candidate.build());
        if (v == null) {
            v = candidate.setId(UuidCreator.getTimeOrderedEpoch().toString()).build();
            violationMapper.insertInspection(v);
        }
        return v;
    }

    public List<Violation> getViolationsByInspection(String inspectionId) {
        return violationMapper.getViolationsByInspection(inspectionId);
    }
}
