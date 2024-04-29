package io.banditoz.dohmap;

import io.banditoz.dohmap.scraper.ScraperDriver;
import io.banditoz.dohmap.scraper.utco.UTCOScraperDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class Scheduler {
    private final ScraperDriver scraperDriver;
    private final UTCOScraperDriver utcoScraperDriver;

    @Autowired
    public Scheduler(ScraperDriver scraperDriver, UTCOScraperDriver utcoScraperDriver) {
        this.scraperDriver = scraperDriver;
        this.utcoScraperDriver = utcoScraperDriver;
    }

    @Scheduled(cron = "${dohmap.jobs.lite-run}")
    public void runScraper() {
        scraperDriver.kickOffScraper(false);
        utcoScraperDriver.kickOffScraper();
    }

    @Scheduled(cron = "${dohmap.jobs.full-run}")
    public void runFullScraper() {
        scraperDriver.kickOffScraper(true);
        utcoScraperDriver.kickOffScraper();
    }
}
