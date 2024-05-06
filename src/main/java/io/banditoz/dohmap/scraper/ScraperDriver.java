package io.banditoz.dohmap.scraper;

import io.banditoz.dohmap.model.DataSource;
import io.banditoz.dohmap.scraper.page.slco.SearchPage;
import io.banditoz.dohmap.service.EstablishmentService;
import io.banditoz.dohmap.service.InspectionService;
import io.banditoz.dohmap.service.ViolationService;
import io.banditoz.dohmap.utils.DateSysId;
import io.banditoz.dohmap.utils.WorkQueue;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class ScraperDriver {
    private final WebDriverFactory webDriverFactory;
    private final EstablishmentService establishmentService;
    private final InspectionService inspectionService;
    private final ViolationService violationService;
    private final int maxSessions;
    private static final Logger log = LoggerFactory.getLogger(ScraperDriver.class);

    @Autowired
    public ScraperDriver(WebDriverFactory webDriverFactory,
                         EstablishmentService establishmentService,
                         InspectionService inspectionService,
                         ViolationService violationService,
                         @Value("${dohmap.selenium.sessions:1}") int maxSessions) {
        this.webDriverFactory = webDriverFactory;
        this.establishmentService = establishmentService;
        this.inspectionService = inspectionService;
        this.violationService = violationService;
        this.maxSessions = maxSessions;
    }

    private int getMaxPages() {
        WebDriver webDriver = webDriverFactory.buildWebDriver();
        try {
            SearchPage page = new SearchPage(webDriver).navigate();
            log.info("maxPages={}", page.getMaxPages());
            return page.getMaxPages();
        } finally {
            webDriverFactory.disposeDriver(webDriver);
        }
    }

    public void kickOffScraper(boolean fullRun) {
        Map<String, List<DateSysId>> previousInspections =
                fullRun ? Collections.emptyMap() : inspectionService.getAllEstablishmentStoredInspectionDates(DataSource.SALT_LAKE_COUNTY_CDP);
        log.info("Loaded previous inspection dates for {} establishments." +
                " If an inspection is encountered with an already stored date by its establishment, it will be skipped.", previousInspections.size());
        if (maxSessions <= 0) {
            log.warn("NOT running ScraperDriver as max sessions is {}!", maxSessions);
            return;
        }
        WorkQueue<Integer> queue = WorkQueue.fillOneToN(getMaxPages());
        for (int i = 0; i < maxSessions; i++) {
            Thread.ofVirtual().start(() -> {
                do {
                    Integer page = queue.getNextItem();
                    if (page == null) {
                        log.info("No more work.");
                        break;
                    }
                    Thread.currentThread().setName(getThreadName(page));
                    go(page, previousInspections);
                } while (queue.hasMoreWork());
                log.info("No more work.");
            });
        }
    }

    public void runOnePage() {
        log.info("Running *full* ScraperDriver for one page only...");
        Thread.ofVirtual().start(() -> {
            Thread.currentThread().setName(getThreadName(1));
            go(1, Collections.emptyMap());
        });
    }

    public void runOnePageLite() {
        log.info("Running ScraperDriver for one page only...");
        Thread.ofVirtual().start(() -> {
            Thread.currentThread().setName(getThreadName(1));
            go(1, inspectionService.getAllEstablishmentStoredInspectionDates(DataSource.SALT_LAKE_COUNTY_CDP));
        });
    }

    public int getMaxSessions() {
        return maxSessions;
    }

    private void go(int page, Map<String, List<DateSysId>> previousInspections) {
        WebDriver webDriver = webDriverFactory.buildWebDriver();
        SLCOHealthInspectionScraper s = null;
        try {
            s = new SLCOHealthInspectionScraper(webDriver, page, establishmentService, inspectionService, violationService, previousInspections);
            s.run();
        } catch (Exception ex) {
            log.error("Encountered fatal exception. Will attempt to restart current page {}. This could loop if there are network errors!", page, ex);
            File screenshot = ((TakesScreenshot) webDriver).getScreenshotAs(OutputType.FILE);
            try {
                log.info("Screenshot saved.");
                FileUtils.copyFile(screenshot, new File("./%s-%d.png".formatted(Thread.currentThread().getName(), System.currentTimeMillis())));
            } catch (IOException e) {
                log.error("Error saving file.", e);
            }
            Thread.ofVirtual().name(getThreadName(page)).start(() -> go(page, previousInspections));
        }
        finally {
            webDriverFactory.disposeDriver(webDriver);
        }
    }

    private String getThreadName(int cc) {
        return "sl-" + cc + "-" + StringUtils.randomAlphanumeric(4);
    }
}
