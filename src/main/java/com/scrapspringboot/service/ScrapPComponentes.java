package com.scrapspringboot.service;

import com.scrapspringboot.model.Product;
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

    public ScrapPComponentes(ChromeOptions chromeOptions) {
        this.chromeOptions = chromeOptions;
    }

    public List<Product> scrape(final String value) {
        List<Product> resultados = new ArrayList<>();
        ChromeDriver driver = new ChromeDriver(chromeOptions);

        try {
            driver.get(URL + value);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.product-card")));

            List<WebElement> items = driver.findElements(By.cssSelector("div.product-card"));

            for (WebElement item : items) {
                try {
                    // Título
                    List<WebElement> titleElements = item.findElements(By.cssSelector("h3.product-card__title"));
                    String title = titleElements.isEmpty() ? "No title found" : titleElements.get(0).getText().trim();

                    // Precio
                    List<WebElement> priceElements = item.findElements(By.cssSelector("div.product-card__price-container span"));
                    String actualPrice = "No price found";
                    String oldPrice = "";
                    List<Double> priceValues = new ArrayList<>();

                    for (WebElement priceElement : priceElements) {
                        String priceText = priceElement.getText().trim().replace("€", "").replace(",", ".");
                        try {
                            double priceValue = Double.parseDouble(priceText);
                            priceValues.add(priceValue);
                        } catch (NumberFormatException ignored) {
                        }
                    }
                    if (!priceValues.isEmpty()) {
                        priceValues.sort(Double::compareTo);
                        actualPrice = String.format("%.2f€", priceValues.get(0));
                        if (priceValues.size() > 1) {
                            oldPrice = String.format("%.2f€", priceValues.get(priceValues.size() - 1));
                        }
                    }

                    // Imagen
                    List<WebElement> imageElements = item.findElements(By.cssSelector("div.product-card__img-container img"));
                    String image = imageElements.isEmpty() ? "No image found" : imageElements.get(0).getAttribute("src");

                    // Valoración
                    List<WebElement> ratingElements = item.findElements(By.cssSelector("div.product-card__rating-container span"));
                    String rating = ratingElements.isEmpty() ? "No rating found" : ratingElements.get(0).getText().trim();

                    // Información de entrega
                    List<WebElement> deliveryElements = item.findElements(By.cssSelector("div.product-card__delivery-container span"));
                    String deliveryInfo = deliveryElements.isEmpty() ? "No delivery info found" : deliveryElements.get(0).getText().trim();


                    Product product = new Product(title, actualPrice, oldPrice, image, rating, deliveryInfo);
                    resultados.add(product);

                } catch (Exception e) {
                    logger.error("Error obteniendo los datos de un producto: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            logger.error("Error en el scraping: {}", e.getMessage());
        } finally {
            driver.quit();
            logger.info("ChromeDriver cerrado después de la búsqueda.");
        }
        return resultados;
    }
}