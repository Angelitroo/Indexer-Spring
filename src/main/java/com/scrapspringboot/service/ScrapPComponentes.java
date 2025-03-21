package com.scrapspringboot.service;

import com.scrapspringboot.model.Product;
import lombok.AllArgsConstructor;
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

                    // Descuento
                    List<WebElement> discountElements = item.findElements(By.cssSelector("span.product-card__img-container-discount-badge"));
                    String discount = discountElements.isEmpty() ? "No discount found" : discountElements.get(0).getText().trim();

                    // Precio
                    List<WebElement> priceElements = item.findElements(By.cssSelector("div.product-card__price-container span"));
                    Double actualPrice = 0.0;
                    Double oldPrice = actualPrice;
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
                        actualPrice = priceValues.get(0);
                        if (priceValues.size() > 1) {
                            oldPrice = priceValues.get(priceValues.size() - 1);
                        } else {
                            oldPrice = actualPrice;
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
                    String delivery = deliveryElements.isEmpty() ? "No delivery info found" : deliveryElements.get(0).getText().trim();

                    // Enlace Producto usando el atributo data-testid
                    List<WebElement> urlElements = item.findElements(By.cssSelector("a.product-card__link"));
                    String url = urlElements.isEmpty() ? "No product url found" : urlElements.get(0).getAttribute("href");


                    if (!"No discount found".equals(discount)) {
                        Product product = new Product(title, discount, actualPrice, oldPrice, image, rating, delivery, url);
                        resultados.add(product);
                    }

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