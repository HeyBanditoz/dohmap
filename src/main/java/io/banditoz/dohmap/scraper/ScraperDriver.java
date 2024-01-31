package io.banditoz.dohmap.scraper;

import io.banditoz.dohmap.scraper.page.SearchPage;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
public class ScraperDriver implements ApplicationListener<ApplicationReadyEvent> {
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

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (maxSessions <= 0) {
            log.warn("NOT running ScraperDriver as max sessions is {}!", maxSessions);
            return;
        }
        for (PageConfiguration page : PageConfiguration.dividePages(getMaxPages(), maxSessions)) {
            Thread.ofVirtual().name(getThreadName(page)).start(() -> go(page));
        }
    }

    private void go(PageConfiguration pageConfiguration) {
        WebDriver webDriver = webDriverFactory.buildWebDriver();
        SLCOHealthInspectionScraper s = null;
        try {
            s = new SLCOHealthInspectionScraper(webDriver, pageConfiguration, establishmentService, inspectionService, violationService);
            s.run();
        } catch (Exception ex) {
            PageConfiguration newConfig = new PageConfiguration(s.getPage(), pageConfiguration.endPage());
            log.error("Encountered fatal exception. Will attempt to restart from {}. This could loop if there are network errors!", newConfig, ex);
            File screenshot = ((TakesScreenshot) webDriver).getScreenshotAs(OutputType.FILE);
            try {
                log.info("Screenshot saved.");
                FileUtils.copyFile(screenshot, new File("./%s-%d.png".formatted(Thread.currentThread().getName(), System.currentTimeMillis())));
            } catch (IOException e) {
                log.error("Error saving file.", e);
            }
            Thread.ofVirtual().name(getThreadName(newConfig)).start(() -> go(newConfig));
        }
        finally {
            webDriverFactory.disposeDriver(webDriver);
        }
    }

    private String getThreadName(PageConfiguration pc) {
        return "sl-" + UUID.randomUUID().toString().substring(0, 4) + '-' + pc.tName();
    }
}
