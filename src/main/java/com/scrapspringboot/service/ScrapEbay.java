package com.scrapspringboot.service;

import com.scrapspringboot.model.Product;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class ScrapEbay {

    private static final String BASE_URL = "https://www.ebay.es/sch/i.html?_nkw=";
    private final ChromeOptions chromeOptions;

    public ScrapEbay(ChromeOptions chromeOptions) {
        this.chromeOptions = chromeOptions;
    }

    public List<Product> scrapEbay(String value) {
        List<Product> products = new ArrayList<>();
        ChromeDriver driver = new ChromeDriver(chromeOptions);

        try {
            String url = BASE_URL + value;
            driver.get(url);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("li.s-item")));
            List<WebElement> items = driver.findElements(By.cssSelector("li.s-item"));

            for (WebElement item : items) {
                try {
                    // Title
                    WebElement linkElement = item.findElement(By.cssSelector("a.s-item__link"));
                    String title;
                    try {
                        WebElement titleElement = linkElement.findElement(By.cssSelector("div.s-item__title"));
                        title = titleElement.getText();
                    } catch (Exception e) {
                        // fallback: the link text itself
                        title = linkElement.getText();
                    }

                    // Link
                    String productUrl = linkElement.getAttribute("href");

                    // Price
                    String priceText;
                    try {
                        WebElement priceElement = item.findElement(By.cssSelector("span.s-item__price"));
                        priceText = priceElement.getText();
                    } catch (Exception e) {
                        priceText = "0.0";
                    }

                    String numeric = priceText
                            .replace("EUR", "")
                            .replace("€", "")
                            .replace(",", ".")
                            .trim();
                    Double actualPrice = 0.0;
                    try {
                        actualPrice = Double.valueOf(numeric);
                    } catch (NumberFormatException e) {
                    }

                    String discount = "No discount";
                    Double oldPrice = actualPrice;

                    String rating = "No rating found";
                    String delivery = "No delivery info";

                    String image = "";
                    try {
                        WebElement imgElement = item.findElement(By.cssSelector("img.s-item__image-img"));
                        image = imgElement.getAttribute("src");
                    } catch (Exception e) {
                        image = "No image found";
                    }

                    Product product = new Product(
                            title,
                            discount,
                            actualPrice,
                            oldPrice,
                            image,
                            rating,
                            delivery,
                            productUrl
                    );
                    products.add(product);
                } catch (Exception e) {
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }

        return products;
    }
}
