package com.scrapspringboot.service;

import com.scrapspringboot.model.Product;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class ScrapElCorteIngles {

    private static final String BASE_URL = "https://www.elcorteingles.es/videojuegos/ps5/?s=";
    private final ChromeOptions chromeOptions;

    public ScrapElCorteIngles(ChromeOptions chromeOptions) {
        this.chromeOptions = chromeOptions;
    }

    public List<Product> scrapElCorteIngles(String value) {
        List<Product> productList = new ArrayList<>();
        ChromeDriver driver = new ChromeDriver(chromeOptions);

        try {
            String url = BASE_URL + value;
            driver.get(url);

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("script")));

            List<WebElement> scripts = driver.findElements(By.tagName("script"));
            for (WebElement script : scripts) {
                String scriptContent = script.getAttribute("innerHTML");

                if (scriptContent.contains("dataLayer =")) {
                    int startIndex = scriptContent.indexOf("dataLayer =") + "dataLayer =".length();
                    String jsonText = scriptContent.substring(startIndex).trim();
                    if (jsonText.endsWith(";")) {
                        jsonText = jsonText.substring(0, jsonText.length() - 1);
                    }

                    JSONArray dataLayerArray = new JSONArray(jsonText);
                    if (dataLayerArray.length() > 0) {
                        JSONObject firstObject = dataLayerArray.getJSONObject(0);
                        if (firstObject.has("products")) {
                            JSONArray products = firstObject.getJSONArray("products");
                            for (int i = 0; i < products.length(); i++) {
                                JSONObject productObj = products.getJSONObject(i);
                                String title = productObj.optString("name", "N/A");

                                JSONObject priceObj = productObj.optJSONObject("price");
                                double fPrice = priceObj != null ? priceObj.optDouble("f_price", 0) : 0;

                                String discount = "No discount";
                                Double oldPrice = fPrice;
                                String rating = "No rating";
                                String delivery = "No delivery info";

                                // Extract URL and image for this product
                                String productUrl = "";
                                String imageUrl = "";

                                // Get URL from link with itemprop="url"
                                List<WebElement> urlElements = driver.findElements(By.cssSelector("link[itemprop='url']"));
                                if (!urlElements.isEmpty()) {
                                    productUrl = urlElements.get(0).getAttribute("href");
                                }

                                // Get image from meta with itemprop="image"
                                List<WebElement> imgElements = driver.findElements(By.cssSelector("meta[itemprop='image']"));
                                if (!imgElements.isEmpty()) {
                                    imageUrl = imgElements.get(0).getAttribute("content");
                                }

                                Product product = new Product(
                                        title,
                                        discount,
                                        fPrice,
                                        oldPrice,
                                        imageUrl,
                                        rating,
                                        delivery,
                                        productUrl
                                );
                                productList.add(product);
                            }
                        }
                    }
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }

        return productList;
    }
}