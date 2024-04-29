package io.banditoz.dohmap.service;

import io.banditoz.dohmap.database.mapper.InspectionMapper;
import io.banditoz.dohmap.model.DataSource;
import io.banditoz.dohmap.model.EstablishmentInspectionDate;
import io.banditoz.dohmap.utils.DateSysId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
    void testGetAllEstablishmentStoredInspectionDates_happyPath_dates() {
        List<EstablishmentInspectionDate> list = List.of(
                new EstablishmentInspectionDate("1", LocalDate.parse("2001-01-01"), null),
                new EstablishmentInspectionDate("2", LocalDate.parse("2001-02-01"), null),
                new EstablishmentInspectionDate("2", LocalDate.parse("2001-02-02"), null),
                new EstablishmentInspectionDate("1", LocalDate.parse("2001-04-01"), null),
                new EstablishmentInspectionDate("3", LocalDate.parse("2024-03-24"), null),
                new EstablishmentInspectionDate("4", LocalDate.parse("2001-10-01"), null),
                new EstablishmentInspectionDate("4", LocalDate.parse("2001-10-02"), null),
                new EstablishmentInspectionDate("4", LocalDate.parse("2001-10-03"), null)
        );
        when(inspectionMapper.getEstablishmentInspectionDates(any())).thenReturn(list);
        Map<String, List<DateSysId>> allEstablishmentStoredInspectionDates = inspectionService.getAllEstablishmentStoredInspectionDates(DataSource.SALT_LAKE_COUNTY_CDP);
        assertThat(allEstablishmentStoredInspectionDates).hasSize(4);

        assertThat(allEstablishmentStoredInspectionDates.get("1")).containsExactlyInAnyOrder(d("2001-01-01"), d("2001-04-01"));
        assertThat(allEstablishmentStoredInspectionDates.get("2")).containsExactlyInAnyOrder(d("2001-02-01"), d("2001-02-02"));
        assertThat(allEstablishmentStoredInspectionDates.get("3")).containsExactlyInAnyOrder(d("2024-03-24"));
        assertThat(allEstablishmentStoredInspectionDates.get("4")).containsExactlyInAnyOrder(d("2001-10-01"), d("2001-10-02"), d("2001-10-03"));
    }

    private DateSysId d(String localDate) {
        return DateSysId.ofDate(LocalDate.parse(localDate));
    }

    @Test
    void testGetAllEstablishmentStoredInspectionDates_empty() {
        when(inspectionMapper.getEstablishmentInspectionDates(any())).thenReturn(Collections.emptyList());
        assertThat(inspectionService.getAllEstablishmentStoredInspectionDates(any())).isEmpty();
    }
}