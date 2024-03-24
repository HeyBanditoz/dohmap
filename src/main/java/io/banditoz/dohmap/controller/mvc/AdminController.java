package io.banditoz.dohmap.controller.mvc;

import io.banditoz.dohmap.scraper.ScraperDriver;
import io.banditoz.dohmap.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final StatsService statsService;
    private final ScraperDriver scraperDriver;

    @Autowired
    public AdminController(StatsService statsService,
                           ScraperDriver scraperDriver) {
        this.statsService = statsService;
        this.scraperDriver = scraperDriver;
    }

    @GetMapping("")
    public String index(Model model) {
        model.addAttribute("count", statsService.getCountOfEstInspVioAsString());
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
}
