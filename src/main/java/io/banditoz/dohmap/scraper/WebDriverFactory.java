package io.banditoz.dohmap.scraper;

import jakarta.annotation.PreDestroy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class WebDriverFactory {
    private static final Logger log = LoggerFactory.getLogger(WebDriverFactory.class);
    private final List<String> customArgs;
    private final URL remote;
    private final Set<WebDriver> drivers = new HashSet<>();

    @Autowired
    public WebDriverFactory(@Value("${dohmap.selenium.args:}") List<String> customArgs,
                            @Value("${dohmap.selenium.remote:}") String remote) throws MalformedURLException {
        this.customArgs = customArgs;
        if (!(remote == null || remote.isEmpty())) {
            this.remote = URI.create(remote).toURL();
        } else {
            this.remote = null;
        }
    }

    public WebDriver buildWebDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments(customArgs);
        if (remote == null) {
            ChromeDriver driver = new ChromeDriver(options);
            log.info("ChromeDriver {} built with custom arguments {}", driver, customArgs);
            drivers.add(driver);
            return driver;
        } else {
            WebDriver driver = new RemoteWebDriver(remote, options);
            log.info("RemoteWebDriver {} built with custom arguments {}", driver, customArgs);
            drivers.add(driver);
            return driver;
        }
    }

    public void disposeDriver(WebDriver driver) {
        log.info("Disposing of {}", driver);
        driver.close();
        driver.quit();
        if (!drivers.remove(driver)) {
            log.warn("{} wasn't contained within {}", driver, drivers);
        }
    }

    @PreDestroy
    public void destroyAllDrivers() {
        for (WebDriver driver : drivers) {
            if (driver == null) {
                continue;
            }
            try {
                driver.close();
                driver.quit();
                log.info("Closed driver {}", driver);
            } catch (Exception ex) {
                log.error("Exception destroying {}!", driver, ex);
            }
        }
    }
}
