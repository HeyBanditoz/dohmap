package io.banditoz.dohmap.service;

import com.github.f4b6a3.uuid.UuidCreator;
import io.banditoz.dohmap.database.mapper.ViolationMapper;
import io.banditoz.dohmap.model.Violation;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ViolationService {
    private final ViolationMapper violationMapper;
    private final MeterRegistry registry;

    @Autowired
    public ViolationService(ViolationMapper violationMapper,
                            MeterRegistry registry) {
        this.violationMapper = violationMapper;
        this.registry = registry;
    }

    public Violation getOrCreationViolation(Violation.Builder candidate) {
        Violation v = violationMapper.getByViolation(candidate.build());
        if (v == null) {
            v = candidate.setId(UuidCreator.getTimeOrderedEpoch().toString()).build();
            Integer cPhrId = violationMapper.getViolationCodePublicHealthRationale(v);
            if (cPhrId == null) {
                cPhrId = violationMapper.insertViolationCodePublicHealthRationale(v);
            }
            violationMapper.insertViolation(v, cPhrId);
            registry.counter("dohmap_violation_created").increment();
        } else {
            registry.counter("dohmap_violation_exists").increment();
        }
        return v;
    }

    public List<Violation> getViolationsByInspection(String inspectionId) {
        return violationMapper.getViolationsByInspection(inspectionId);
    }
}
