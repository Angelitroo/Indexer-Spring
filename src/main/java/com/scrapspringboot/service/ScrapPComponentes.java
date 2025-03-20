package com.scrapspringboot.service;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class ScrapPComponentes {
    private static final String URL = "https://www.pccomponentes.com/search/?query=";
    private final ChromeOptions chromeOptions;
    private static final Logger logger = LoggerFactory.getLogger(ScrapPComponentes.class);

    // Constructor manual para inyectar ChromeOptions
    public ScrapPComponentes(ChromeOptions chromeOptions) {
        this.chromeOptions = chromeOptions;
    }

    public List<String> scrape(final String value) {
        List<String> titles = new ArrayList<>();
        ChromeDriver driver = new ChromeDriver(chromeOptions);

        try {
            driver.get(URL + value);

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.product-card")));

            List<WebElement> items = driver.findElements(By.cssSelector("div.product-card"));

            for (WebElement item : items) {
                try {
                    WebElement titleElement = item.findElement(By.cssSelector("h3.product-card__title"));
                    titles.add(titleElement.getText());
                } catch (Exception e) {
                    logger.error("Error obteniendo el título de un producto: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            logger.error("Error en el scraping: {}", e.getMessage());
        } finally {
            driver.quit();
            logger.info("ChromeDriver cerrado después de la búsqueda.");
        }
        return titles;
    }
}
