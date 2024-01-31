package io.banditoz.dohmap.scraper;

import io.banditoz.dohmap.model.Establishment;
import io.banditoz.dohmap.model.Inspection;
import io.banditoz.dohmap.model.Violation;
import io.banditoz.dohmap.scraper.page.InspectionHistoryPage;
import io.banditoz.dohmap.scraper.page.InspectionPage;
import io.banditoz.dohmap.scraper.page.SearchPage;
import io.banditoz.dohmap.scraper.page.base.PageConfiguration;
import io.banditoz.dohmap.service.EstablishmentService;
import io.banditoz.dohmap.service.InspectionService;
import io.banditoz.dohmap.service.ViolationService;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class SLCOHealthInspectionScraper implements Runnable {
    private final WebDriver driver;
    private final int pageAssignment;
    private final EstablishmentService establishmentService;
    private final InspectionService inspectionService;
    private final ViolationService violationService;
    private static final Logger log = LoggerFactory.getLogger(SLCOHealthInspectionScraper.class);

    public SLCOHealthInspectionScraper(WebDriver driver,
                                       int pageAssignment,
                                       EstablishmentService establishmentService,
                                       InspectionService inspectionService,
                                       ViolationService violationService) {
        this.driver = driver;
        this.pageAssignment = pageAssignment;
        this.establishmentService = establishmentService;
        this.inspectionService = inspectionService;
        this.violationService = violationService;
    }

    @Override
    public void run() {
        SearchPage page = new SearchPage(driver).navigate();
        page.gotoPage(pageAssignment);
        log.info("ON PAGE {}", pageAssignment);
        int jmax = page.ready().tableSize();
        for (int j = 0; j < jmax; j++) {
            InspectionHistoryPage inspectionHistoryPage = page.ready().clickEstablishmentInspections(j);
            Establishment est = establishmentService.getOrCreateEstablishment(inspectionHistoryPage.getEstablishmentInfo());
            establishmentService.indexEstablishmentRank(est, inspectionHistoryPage.getRank());
            for (Map.Entry<String, Integer> ent : inspectionHistoryPage.getInspections().entrySet()) {
                InspectionPage inspectionPage = inspectionHistoryPage.clickInspection(ent.getValue());
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
    }
}
