package io.banditoz.dohmap.controller.mvc;

import io.banditoz.dohmap.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    private final StatsService statsService;

    @Autowired
    public HomeController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("count", statsService.getCountOfEstInspVioAsString());
        model.addAttribute("showLoad", true);
        return "index";
    }
}
