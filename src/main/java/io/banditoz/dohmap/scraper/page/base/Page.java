package io.banditoz.dohmap.scraper.page.base;

import org.openqa.selenium.By;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public abstract class Page<T> {
    protected final WebDriver driver;
    protected final Logger log = LoggerFactory.getLogger(getClass());

    private final static Duration SHORT_TIMEOUT = Duration.ofSeconds(30);

    public Page(WebDriver driver) {
        this.driver = driver;
    }

    protected abstract T ready();

    protected T navigate() {
        throw new UnsupportedOperationException(this.getClass().getSimpleName() + " cannot be directly navigated to.");
    }

    protected void click(WebElement element) {
        waitForElementToBeClickable(element, SHORT_TIMEOUT);
        element.click();
    }

    protected void enterString(WebElement element, String str) {
        waitForElementToBeClickable(element, SHORT_TIMEOUT);
        element.clear();
        element.sendKeys(str);
    }

    protected WebElement findElement(By by) {
        return waitForElementToBePresentAndVisible(by);
    }

    protected WebElement waitForElementToBeClickable(WebElement element, Duration duration) {
        new WebDriverWait(driver, duration).until(ExpectedConditions.visibilityOf(element));
        return new WebDriverWait(driver, duration).until(ExpectedConditions.elementToBeClickable(element));
    }

    protected WebElement waitForElementToBePresentAndVisible(By by) {
        return waitForElementToBePresentAndVisible(by, SHORT_TIMEOUT);
    }

    protected void waitForElementToBeHidden(By by) {
        new WebDriverWait(driver, SHORT_TIMEOUT).until(ExpectedConditions.invisibilityOfElementLocated(by));
    }

    protected WebElement waitForElementToBePresentAndVisible(By by, Duration duration) {
        return new WebDriverWait(driver, duration).until(ExpectedConditions.visibilityOfElementLocated(by));
    }

    protected Integer intOrNull(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        return Integer.parseInt(s);
    }
}
