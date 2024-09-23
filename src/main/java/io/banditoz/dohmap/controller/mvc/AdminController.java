package io.banditoz.dohmap.controller.mvc;

import io.banditoz.dohmap.model.Establishment;
import io.banditoz.dohmap.scraper.ScraperDriver;
import io.banditoz.dohmap.scraper.utco.UTCOScraperDriver;
import io.banditoz.dohmap.service.EstablishmentService;
import io.banditoz.dohmap.service.GoogleMapsService;
import io.banditoz.dohmap.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final StatsService statsService;
    private final ScraperDriver scraperDriver;
    private final UTCOScraperDriver utcoScraperDriver;
    private final EstablishmentService establishmentService;
    private final GoogleMapsService googleMapsService;

    @Autowired
    public AdminController(StatsService statsService,
                           ScraperDriver scraperDriver,
                           UTCOScraperDriver utcoScraperDriver,
                           EstablishmentService establishmentService, GoogleMapsService googleMapsService) {
        this.statsService = statsService;
        this.scraperDriver = scraperDriver;
        this.utcoScraperDriver = utcoScraperDriver;
        this.establishmentService = establishmentService;
        this.googleMapsService = googleMapsService;
    }

    @GetMapping("")
    public String index(Model model) {
        model.addAttribute("count", statsService.getCountOfEstInspVioAsString());
        model.addAttribute("activePage", "admin");
        return "admin/admin";
    }

    @GetMapping("/browserTest")
    public RedirectView browserTest(RedirectAttributes attributes) {
        scraperDriver.runOnePage();
        attributes.addFlashAttribute("successText", "Full ScraperDriver started for page 1 only. Please check the logs to monitor success.");
        return new RedirectView("/admin");
    }

    @GetMapping("/liteBrowserTest")
    public RedirectView liteBrowserTest(RedirectAttributes attributes) {
        scraperDriver.runOnePageLite();
        attributes.addFlashAttribute("successText", "Lite ScraperDriver started for page 1 only. Please check the logs to monitor success.");
        return new RedirectView("/admin");
    }

    @GetMapping("/fullRun")
    public RedirectView fullRun(RedirectAttributes attributes) {
        if (scraperDriver.getMaxSessions() <= 0) {
            attributes.addFlashAttribute("failureText", "Refusing to run ScraperDriver as the configured max sessions is zero.");
        } else {
            scraperDriver.kickOffScraper(true);
            attributes.addFlashAttribute("successText", "Kicked off full scrape run. Please check the logs to monitor success.");
        }
        return new RedirectView("/admin");
    }

    @GetMapping("/liteRun")
    public RedirectView liteRun(RedirectAttributes attributes) {
        if (scraperDriver.getMaxSessions() <= 0) {
            attributes.addFlashAttribute("failureText", "Refusing to run ScraperDriver as the configured max sessions is zero.");
        } else {
            scraperDriver.kickOffScraper(false);
            attributes.addFlashAttribute("successText", "Kicked off lite scrape run. Please check the logs to monitor success.");
        }
        return new RedirectView("/admin");
    }

    @GetMapping("/utcoTest")
    public RedirectView utcoTest(@RequestParam String letters, RedirectAttributes attributes) {
        utcoScraperDriver.goOnlyWithLetters(letters);
        attributes.addFlashAttribute("successText", "UTCO Scraper kicked off for letters \"" + letters + "\" Please check the logs to monitor success.");
        return new RedirectView("/admin");
    }


    @GetMapping("/utcoTestStartAt")
    public RedirectView utcoTestStartAt(@RequestParam String letters, RedirectAttributes attributes) {
        utcoScraperDriver.go(letters);
        attributes.addFlashAttribute("successText", "UTCO Scraper kicked off for letters \"" + letters + "\" Please check the logs to monitor success.");
        return new RedirectView("/admin");
    }

    @GetMapping("/fullUtcoTest")
    public RedirectView utcoTest(RedirectAttributes attributes) {
        utcoScraperDriver.go(null);
        attributes.addFlashAttribute("successText", "Full UTCOScraperDriver started. Please check the logs to monitor success.");
        return new RedirectView("/admin");
    }

    @GetMapping("/reaffirmLocations")
    public RedirectView reaffirmLocations(RedirectAttributes attributes) {
        List<Establishment> missingLocs = establishmentService.getAllEstablishmentsWithMissingLocations().stream()
                .filter(establishment -> googleMapsService.isEstablishmentTypeAllowed(establishment.type()))
                .toList();
        missingLocs.forEach(googleMapsService::indexEstablishment);
        attributes.addFlashAttribute("successText", "Reaffirming locations for " + missingLocs.size() + " establishments with no locations.");
        return new RedirectView("/admin");
    }
}
