package io.banditoz.dohmap.scraper.utco;

import io.banditoz.dohmap.model.DataSource;
import io.banditoz.dohmap.service.EstablishmentService;
import io.banditoz.dohmap.service.InspectionService;
import io.banditoz.dohmap.service.ViolationService;
import io.banditoz.dohmap.utils.DateSysId;
import io.banditoz.dohmap.utils.LetterUtils;
import io.banditoz.dohmap.utils.WorkQueue;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class UTCOScraperDriver {
    private static final Logger log = LoggerFactory.getLogger(UTCOScraperDriver.class);
    private final EstablishmentService establishmentService;
    private final InspectionService inspectionService;
    private final ViolationService violationService;
    private final AtomicInteger activeThreads;

    public UTCOScraperDriver(EstablishmentService establishmentService,
                             InspectionService inspectionService,
                             ViolationService violationService,
                             MeterRegistry registry) {
        this.establishmentService = establishmentService;
        this.inspectionService = inspectionService;
        this.violationService = violationService;
        // should this be a class instead?
        this.activeThreads = registry.gauge("dohmap_active_scrapers", List.of(Tag.of("kind", "utco")), new AtomicInteger(0));
    }

    public void go(String startAt) {
        Map<String, List<DateSysId>> inspDates = inspectionService.getAllEstablishmentStoredInspectionDates(DataSource.UTAH_COUNTY_PARAGON);
        WorkQueue<String> queue = new WorkQueue<>(LetterUtils.TWO_LETTER_PAIRS);
        if (startAt != null) {
            if (queue.offsetToFirstOccurrence(startAt.toLowerCase()) == 0) {
                throw new IllegalArgumentException("No letters were skipped. Is your letter contained within the list?");
            }
        }
        Set<String> seenEstablishments = ConcurrentHashMap.newKeySet();
        for (int i = 0; i < 3; i++) {
            Thread.ofVirtual().start(() -> {
               try {
                    do {
                        String letters = queue.getNextItem();
                        if (letters == null) {
                            log.info("No more work.");
                            break;
                        }
                        Thread.currentThread().setName("utco-" + letters + "-" + StringUtils.randomAlphanumeric(4));
                        _go(seenEstablishments, inspDates, letters);
                    } while (queue.hasMoreWork());
                    log.info("No more work.");
               } catch (Exception ex) {
                   log.error("Fatal error running work.", ex);
               }
            });
        }
    }

    private void _go(Set<String> seenEstablishments, Map<String, List<DateSysId>> inspDates, String letters) throws IOException {
        try {
            activeThreads.getAndIncrement();
            new UTCOHealthInspectionScraper(establishmentService, inspectionService, violationService, seenEstablishments, inspDates)
                    .run(letters);
        } finally {
            activeThreads.getAndDecrement();
        }
    }

    public void goOnlyWithLetters(String letters) {
        Thread.ofVirtual().start(() -> {
            try {
                _go(null, Collections.emptyMap(), letters);
            } catch (Exception e) {
                log.error("Uncaught exception running scraper!", e);
            }
        });
    }

    public void kickOffScraper() {
        go(null);
    }
}
