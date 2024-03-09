package io.banditoz.dohmap;

import io.banditoz.dohmap.scraper.ScraperDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class Scheduler {
    private final ScraperDriver scraperDriver;

    @Autowired
    public Scheduler(ScraperDriver scraperDriver) {
        this.scraperDriver = scraperDriver;
    }

    @Scheduled(cron = "0 0 22 * * 1-5")
    public void runScraper() {
        scraperDriver.kickOffScraper();
    }
}
