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
    private final PageConfiguration scraperConfig;
    private final EstablishmentService establishmentService;
    private final InspectionService inspectionService;
    private final ViolationService violationService;
    private int i;
    private static final Logger log = LoggerFactory.getLogger(SLCOHealthInspectionScraper.class);

    public SLCOHealthInspectionScraper(WebDriver driver,
                                       PageConfiguration scraperConfig,
                                       EstablishmentService establishmentService,
                                       InspectionService inspectionService,
                                       ViolationService violationService) {
        this.driver = driver;
        this.scraperConfig = scraperConfig;
        this.establishmentService = establishmentService;
        this.inspectionService = inspectionService;
        this.violationService = violationService;
        i = scraperConfig.startPage();
    }

    @Override
    public void run() {
        SearchPage page = new SearchPage(driver).navigate();
        for (; i <= scraperConfig.endPage(); i++) {
            page.gotoPage(i);
            log.info("ON PAGE {}", i);
            int jmax = page.ready().tableSize();
            for (int j = 0; j < jmax; j++) {
                InspectionHistoryPage inspectionHistoryPage = page.ready().clickEstablishmentInspections(j);
                Establishment est = establishmentService.getOrCreateEstablishment(inspectionHistoryPage.getEstablishmentInfo());
                establishmentService.indexEstablishmentRank(est, inspectionHistoryPage.getRank());
//                log.info(est.toString());
                for (Map.Entry<String, Integer> ent : inspectionHistoryPage.getInspections().entrySet()) {
                    InspectionPage inspectionPage = inspectionHistoryPage.clickInspection(ent.getValue());
                    Inspection inspection = inspectionService.getOrCreateInspection(inspectionPage.getInspection().setEstablishmentId(est.id()));
                    List<Violation.Builder> violations = inspectionPage.getViolations();
                    for (Violation.Builder violation : violations) {
                        violation.setInspectionId(inspection.id());
//                        log.info(violation.toString());
                        violationService.getOrCreationViolation(violation);
                    }
//                    log.info(inspectionPage.getInspection().toString());
                    inspectionPage.back();
                }
                inspectionHistoryPage.back();
            }
        }
    }

    public int getPage() {
        return i;
    }
}
