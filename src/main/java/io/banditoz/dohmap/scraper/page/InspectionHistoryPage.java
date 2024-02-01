package io.banditoz.dohmap.scraper.page;

import io.banditoz.dohmap.model.Establishment;
import io.banditoz.dohmap.scraper.page.base.Page;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InspectionHistoryPage extends Page<InspectionHistoryPage> {
    private final By inspectionsTable = By.id("INSPECTIONTableControlGrid");
    private final By establishmentInformation = By.cssSelector("#ctl00_PageContent_VW_EST_PUBLIC2RecordControlPanel > table:nth-child(1)");

    private static final Pattern CITY_STATE_ZIP = Pattern.compile("(.*),\\s+(\\w+)\\s+(\\d+)");

    public InspectionHistoryPage(WebDriver driver) {
        super(driver);
    }

    @Override
    protected InspectionHistoryPage ready() {
        waitForElementToBePresentAndVisible(inspectionsTable);
        return this;
    }

    public Establishment.Builder getEstablishmentInfo() {
        Establishment.Builder builder = new Establishment.Builder();
        List<WebElement> cols = driver.findElement(establishmentInformation).findElements(By.tagName("tr"));
        builder.setName(cols.get(0).findElements(By.tagName("td")).get(1).getText());
        builder.setAddress(cols.get(1).findElements(By.tagName("td")).get(1).getText());
        String csz = cols.get(2).findElements(By.tagName("td")).get(1).getText();
        Matcher m = CITY_STATE_ZIP.matcher(csz);
        if (m.find()) {
            builder.setCity(m.group(1));
            builder.setState(m.group(2));
            builder.setZip(m.group(3));
        } else {
            log.warn("CITY_STATE_ZIP regex did not match against \"{}\"", csz);
        }
        String phone = cols.get(3).findElements(By.tagName("td")).get(1).getText();
        builder.setPhone(phone.isBlank() ? null : phone);
        builder.setType(cols.get(4).findElements(By.tagName("td")).get(1).getText());
        return builder;
    }

    public Integer getRank() {
        List<WebElement> cols = driver.findElement(establishmentInformation).findElements(By.tagName("tr"));
        if (cols.size() == 6) {
            String percent = cols.get(5).getText().split("%")[0].replaceFirst("Rank ", "");
            if (percent.isBlank()) {
                return null;
            }
            try {
                return Integer.parseInt(percent);
            } catch (NumberFormatException ex) {
                if (!"Not ranked".equals(percent)) {
                    log.warn("Bad rank value {}", percent);
                }
            }
        }
        return null;
    }

    public InspectionPage clickInspection(int i) {
        waitForElementToBePresentAndVisible(inspectionsTable);
        click(driver.findElements(By.cssSelector("#INSPECTIONTableControlGrid > tbody > tr:not(.tch) a")).get(i));
        InspectionPage inspectionPage = new InspectionPage(driver);
        return inspectionPage.ready();
    }

    /** Map of Date, Pagenum. */
    public Map<String, Integer> getInspections() {
        Map<String, Integer> insps = new LinkedHashMap<>(); // maintain order for easier debugging
        List<WebElement> rows = driver.findElements(By.cssSelector("#INSPECTIONTableControlGrid > tbody > tr:not(.tch)"));
        for (int i = rows.size() - 1; i >= 0; i--) {
            WebElement row = rows.get(i);
            List<WebElement> cols = row.findElements(By.tagName("td"));
            String date = cols.get(10).getText();
            if (insps.get(date) != null) {
                log.warn("Establishment {} has inspections that fall under the same day. " +
                        "This is not allowed under the current data model. Choosing the newest by date...", getEstablishmentInfo().build());
            }
            insps.put(date, i);
        }
        return insps;
    }

    public int getInspectionSize() {
        waitForElementToBePresentAndVisible(inspectionsTable);
        return driver.findElements(By.cssSelector("#INSPECTIONTableControlGrid > tbody tr:not(.tch) a")).size();
    }

    public void back() {
        click(findElement(By.id("ctl00_PageContent_OKButton__Button")));
    }
}
