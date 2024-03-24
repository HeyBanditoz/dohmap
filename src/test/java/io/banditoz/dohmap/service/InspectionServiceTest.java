package io.banditoz.dohmap.service;

import io.banditoz.dohmap.database.mapper.InspectionMapper;
import io.banditoz.dohmap.model.EstablishmentInspectionDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InspectionServiceTest {
    private InspectionService inspectionService;
    private InspectionMapper inspectionMapper;

    @BeforeEach
    void setup() {
        inspectionMapper = mock(InspectionMapper.class);
        inspectionService = new InspectionService(inspectionMapper);
    }

    @Test
    void testGetAllEstablishmentStoredInspectionDates_happyPath() {
        List<EstablishmentInspectionDate> list = List.of(
                new EstablishmentInspectionDate("1", "01/01/2001"),
                new EstablishmentInspectionDate("2", "02/01/2001"),
                new EstablishmentInspectionDate("2", "02/02/2001"),
                new EstablishmentInspectionDate("1", "04/01/2001"),
                new EstablishmentInspectionDate("3", "03/24/2024"),
                new EstablishmentInspectionDate("4", "10/01/2001"),
                new EstablishmentInspectionDate("4", "10/02/2001"),
                new EstablishmentInspectionDate("4", "10/03/2001")
        );
        when(inspectionMapper.getEstablishmentInspectionDates()).thenReturn(list);
        Map<String, List<String>> allEstablishmentStoredInspectionDates = inspectionService.getAllEstablishmentStoredInspectionDates();
        assertThat(allEstablishmentStoredInspectionDates).hasSize(4);
        assertThat(allEstablishmentStoredInspectionDates.get("1")).containsExactlyInAnyOrder("01/01/2001", "04/01/2001");
        assertThat(allEstablishmentStoredInspectionDates.get("2")).containsExactlyInAnyOrder("02/01/2001", "02/02/2001");
        assertThat(allEstablishmentStoredInspectionDates.get("3")).containsExactlyInAnyOrder("03/24/2024");
        assertThat(allEstablishmentStoredInspectionDates.get("4")).containsExactlyInAnyOrder("10/01/2001", "10/02/2001", "10/03/2001");
    }

    @Test
    void testGetAllEstablishmentStoredInspectionDates_empty() {
        when(inspectionMapper.getEstablishmentInspectionDates()).thenReturn(Collections.emptyList());
        assertThat(inspectionService.getAllEstablishmentStoredInspectionDates()).isEmpty();
    }
}