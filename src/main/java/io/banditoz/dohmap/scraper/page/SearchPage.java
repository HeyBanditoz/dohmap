package io.banditoz.dohmap.scraper.page;

import io.banditoz.dohmap.scraper.page.base.Page;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class SearchPage extends Page<SearchPage> {
    private final By inspectionsTable = By.id("VW_EST_PUBLICTableControlGrid");
    private final By ajaxLoadIndicator = By.id("ctl00_PageContent_UpdatePanel1_UpdateProgress1");
    private final By pageGoButton = By.id("ctl00_PageContent_VW_EST_PUBLICPagination__PageSizeButton");
    private final By pageEntry = By.id("ctl00_PageContent_VW_EST_PUBLICPagination__CurrentPage");
    private final By maxPages = By.id("ctl00_PageContent_VW_EST_PUBLICPagination__TotalPages");

    public SearchPage(WebDriver driver) {
        super(driver);
    }

    @Override
    public SearchPage navigate() {
        driver.navigate().to("https://public.cdpehs.com/UTEnvPbl/VW_EST_PUBLIC/ShowVW_EST_PUBLICTablePage.aspx");
        return this;
    }

    @Override
    public SearchPage ready() {
        waitForElementToBePresentAndVisible(inspectionsTable);
        waitForElementToBeHidden(ajaxLoadIndicator);
        return this;
    }

    public int tableSize() {
        return driver.findElements(By.cssSelector("#VW_EST_PUBLICTableControlGrid > tbody > tr:not(.tch)")).size();
    }

    public int getMaxPages() {
        return intOrNull(findElement(maxPages).getText());
    }

    public void gotoPage(int page) {
        enterString(findElement(pageEntry), String.valueOf(page));
        click(findElement(pageGoButton));
        waitForElementToBePresentAndVisible(ajaxLoadIndicator);
    }

    public InspectionHistoryPage clickEstablishmentInspections(int i) {
        click(findElement(inspectionsTable).findElements(By.cssSelector("#VW_EST_PUBLICTableControlGrid a")).get(i));
        InspectionHistoryPage inspectionHistoryPage = new InspectionHistoryPage(driver);
        return inspectionHistoryPage.ready();
    }
}