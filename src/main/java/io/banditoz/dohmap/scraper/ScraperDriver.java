package io.banditoz.dohmap.scraper;

import io.banditoz.dohmap.scraper.page.slco.SearchPage;
import io.banditoz.dohmap.scraper.page.base.PageConfiguration;
import io.banditoz.dohmap.service.EstablishmentService;
import io.banditoz.dohmap.service.InspectionService;
import io.banditoz.dohmap.service.ViolationService;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

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

    public void kickOffScraper() {
        if (maxSessions <= 0) {
            log.warn("NOT running ScraperDriver as max sessions is {}!", maxSessions);
            return;
        }
        for (PageConfiguration page : PageConfiguration.dividePages(getMaxPages(), maxSessions)) {
            Thread.ofVirtual().start(() -> {
                for (int i = page.startPage(); i <= page.endPage(); i++) {
                    Thread.currentThread().setName(getThreadName(page, i));
                    go(page, i);
                }
            });
        }
    }

    private void go(PageConfiguration pc, int page) {
        WebDriver webDriver = webDriverFactory.buildWebDriver();
        SLCOHealthInspectionScraper s = null;
        try {
            s = new SLCOHealthInspectionScraper(webDriver, page, establishmentService, inspectionService, violationService);
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
            Thread.ofVirtual().name(getThreadName(pc, page)).start(() -> go(pc, page));
        }
        finally {
            webDriverFactory.disposeDriver(webDriver);
        }
    }

    private String getThreadName(PageConfiguration pc, int cc) {
        return "sl-" + cc + "-" + pc.tName();
    }
}
