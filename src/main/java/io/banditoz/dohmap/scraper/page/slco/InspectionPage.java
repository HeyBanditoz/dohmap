package io.banditoz.dohmap.scraper.page.slco;

import io.banditoz.dohmap.model.Inspection;
import io.banditoz.dohmap.model.Violation;
import io.banditoz.dohmap.scraper.page.base.Page;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class InspectionPage extends Page<InspectionPage> {
    private static final By INSPTABLE = By.id("INSPECTION_VIOLATIONTableControlGrid");
    private static final DateTimeFormatter INSPDATE = DateTimeFormatter.ofPattern("M/d/uuuu");

    public InspectionPage(WebDriver driver) {
        super(driver);
    }

    @Override
    protected InspectionPage ready() {
        waitForElementToBePresentAndVisible(INSPTABLE);
        return this;
    }

    public Inspection.Builder getInspection() {
        List<WebElement> cols = driver.findElements(By.cssSelector("#ctl00_PageContent_INSPECTIONRecordControlPanel tr td:nth-child(2)"));
        return new Inspection.Builder()
                .setInspectionDate(LocalDate.parse(cols.get(0).getText(), INSPDATE))
                .setInspectionType(cols.get(1).getText())
                .setScore(intOrNull(cols.get(2).getText()));
    }

    /*
    result = {ImmutableCollections$ListN@11456}  size = 7
    0 = "4.3.4"
    1 = " "
    2 = "0"
    3 = "No"
    4 = "1"
    5 = "No"
    6 = "Containers, Drawers and Cabinets Clean and Sanitary"
     */

    /*
    result = {ImmutableCollections$ListN@10752}  size = 8
    0 = "4.24.1"
    1 = "Free chlorine disinfectant residual less than 1 ppm (required closure)"
    2 = "Free chlorine disinfectant residual less than 1 ppm (required closure)"
    3 = " "
    4 = "No"
    5 = "1"
    6 = "No"
    7 = "Disinfection and Quality of Water-Disinfection Process"
     */
    public List<Violation.Builder> getViolations() {
        List<Violation.Builder> vlns = new ArrayList<>();
        for (WebElement row : findElement(INSPTABLE).findElements(By.cssSelector("tr:nth-child(n+3)"))) {
            Violation.Builder builder = new Violation.Builder();
            List<WebElement> cols = row.findElements(By.tagName("td"));
            int i = -1;
            builder.setCode(cols.get(++i).getText());
            builder.setObserved(nullIfBlank(cols.get(++i).getText()));
            // two is missing because the site makes heavy use of inner tables for formatting
            if (cols.size() == 8) {
                i++; // if there is no "observed violations" a td column is removed... TODO better way to handle this!
            }
            builder.setPoints(intOrNull(cols.get(++i).getText()));
            builder.setCritical(popsicle(cols.get(++i).getText())); // assuming missing is false, unsure if missing is even allowed by slcohealth's data model
            builder.setOccurrences(intOrNull(cols.get(++i).getText()));
            builder.setCorrectedOnSite(popsicle(cols.get(++i).getText()));
            builder.setPublicHealthRationale(nullIfBlank(cols.get(++i).getText()));
            vlns.add(builder);
        }
        return vlns;
    }

    public void back() {
        driver.navigate().back();
//        click(driver.findElement(By.id("ctl00_PageContent_OKButton__Button")));
    }

    // move to Page?
    private String nullIfBlank(String str) {
        return str == null ? null : str.isBlank() ? null : str;
    }

    // move to Page?
    private Boolean popsicle(String str) {
        return "Yes".equals(nullIfBlank(str));
    }
}
