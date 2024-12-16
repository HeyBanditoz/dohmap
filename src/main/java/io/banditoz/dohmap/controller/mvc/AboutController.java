package io.banditoz.dohmap.controller.mvc;

import io.banditoz.dohmap.service.StatsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/about")
public class AboutController {
    private final StatsService statsService;
    private final String aboutText;

    public AboutController(StatsService statsService, @Value("${dohmap.about-page:<h1>About DOH Map!</h1>\n<p>No about page set.</p>}") String aboutText) {
        this.statsService = statsService;
        this.aboutText = aboutText;
    }

    @GetMapping("")
    public String about(Model model) {
        model.addAttribute("count", statsService.getCountOfEstInspVioAsString());
        model.addAttribute("activePage", "about");
        model.addAttribute("aboutText", aboutText);
        return "about";
    }
}
