package io.banditoz.dohmap.scraper;

import io.banditoz.dohmap.model.Establishment;
import io.banditoz.dohmap.model.Inspection;
import io.banditoz.dohmap.model.Violation;
import io.banditoz.dohmap.scraper.page.slco.InspectionHistoryPage;
import io.banditoz.dohmap.scraper.page.slco.InspectionPage;
import io.banditoz.dohmap.scraper.page.slco.SearchPage;
import io.banditoz.dohmap.service.EstablishmentService;
import io.banditoz.dohmap.service.InspectionService;
import io.banditoz.dohmap.service.ViolationService;
import io.banditoz.dohmap.utils.DateSysId;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Metrics;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static java.util.Collections.emptyList;

public class SLCOHealthInspectionScraper implements Runnable {
    private final WebDriver driver;
    private final int pageAssignment;
    private final EstablishmentService establishmentService;
    private final InspectionService inspectionService;
    private final ViolationService violationService;
    private final Map<String, List<DateSysId>> previousInspectionDates;
    private static final Logger log = LoggerFactory.getLogger(SLCOHealthInspectionScraper.class);

    public SLCOHealthInspectionScraper(WebDriver driver,
                                       int pageAssignment,
                                       EstablishmentService establishmentService,
                                       InspectionService inspectionService,
                                       ViolationService violationService,
                                       Map<String, List<DateSysId>> previousInspectionDates) {
        this.driver = driver;
        this.pageAssignment = pageAssignment;
        this.establishmentService = establishmentService;
        this.inspectionService = inspectionService;
        this.violationService = violationService;
        this.previousInspectionDates = previousInspectionDates;
    }

    @Override
    public void run() {
        long before = System.currentTimeMillis();
        SearchPage page = new SearchPage(driver).navigate();
        page.gotoPage(pageAssignment);
        log.info("ON PAGE {}", pageAssignment);
        int jmax = measureFn("search", () -> page.ready().tableSize());
        for (int j = 0; j < jmax; j++) {
            int finalJ = j;
            InspectionHistoryPage inspectionHistoryPage = measureFn("establishmentDetails", () -> page.ready().clickEstablishmentInspections(finalJ));
            Establishment est = establishmentService.getOrCreateEstablishment(inspectionHistoryPage.getEstablishmentInfo());
            establishmentService.indexEstablishmentRank(est, inspectionHistoryPage.getRank());
            for (Map.Entry<LocalDate, Integer> ent : inspectionHistoryPage.getInspections().entrySet()) {
                inspectionHistoryPage.removeAjax();
                if (previousInspectionDates.getOrDefault(est.id(), emptyList()).contains(DateSysId.ofDate(ent.getKey()))) {
                    continue;
                }
                InspectionPage inspectionPage = measureFn("inspectionDetails", () -> inspectionHistoryPage.clickInspection(ent.getValue()));
                Inspection inspection = inspectionService.getOrCreateInspection(inspectionPage.getInspection().setEstablishmentId(est.id()));
                List<Violation.Builder> violations = inspectionPage.getViolations();
                for (Violation.Builder violation : violations) {
                    violation.setInspectionId(inspection.id());
                    violationService.getOrCreationViolation(violation);
                }
                inspectionPage.back();
            }
            inspectionHistoryPage.back();
        }
        log.info("This page took {} seconds.", (System.currentTimeMillis() - before) / 1000);
    }

    private <T> T measureFn(String op, Supplier<T> r) {
        long before = System.nanoTime();
        T t = r.get();
        double timeTookSec = (System.nanoTime() - before) / 1_000_000_000D;
        DistributionSummary.builder("dohmap_slco_request_time")
                .tags("op", op)
                .publishPercentiles(0.50, 0.75, 0.99)
                .register(Metrics.globalRegistry)
                .record(timeTookSec);
        return t;
    }
}
