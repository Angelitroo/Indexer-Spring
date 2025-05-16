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

    private static final String BASE_URL = "https://www.elcorteingles.es/search-nwx/?s=";
    private static final String SEARCH_TYPE = "&stype=text_box_multi";
    private final ChromeOptions chromeOptions;

    public ScrapElCorteIngles(ChromeOptions chromeOptions) {
        this.chromeOptions = chromeOptions;
    }

    public List<Product> scrapElCorteIngles(String value) {
        List<Product> productList = new ArrayList<>();
        ChromeDriver driver = new ChromeDriver(chromeOptions);

        try {
            driver.get(BASE_URL + value + SEARCH_TYPE);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("script")));

            for (WebElement script : driver.findElements(By.tagName("script"))) {
                String content = script.getAttribute("innerHTML");
                if (content.contains("dataLayer =")) {
                    JSONArray dataLayer = extractDataLayer(content);
                    if (dataLayer.length() > 0) {
                        JSONObject firstObject = dataLayer.getJSONObject(0);
                        if (firstObject.has("products")) {
                            processProducts(firstObject.getJSONArray("products"), productList);
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

    private JSONArray extractDataLayer(String content) {
        int startIndex = content.indexOf("dataLayer =") + "dataLayer =".length();
        String jsonText = content.substring(startIndex).trim();
        if (jsonText.endsWith(";")) {
            jsonText = jsonText.substring(0, jsonText.length() - 1);
        }
        return new JSONArray(jsonText);
    }

    private void processProducts(JSONArray products, List<Product> productList) {
        for (int i = 0; i < products.length(); i++) {
            JSONObject product = products.getJSONObject(i);

            String title = product.getString("name");

            JSONObject priceObj = product.getJSONObject("price");
            double actualPrice = priceObj.optDouble("f_price", 0.0);
            double oldPrice = priceObj.optDouble("o_price", actualPrice);

            String discount = "No discount";
            if (priceObj.optInt("discount_percent", 0) > 0) {
                discount = priceObj.getInt("discount_percent") + "%";
            }

            JSONObject badges = product.getJSONObject("badges");
            String delivery = badges.optBoolean("express_delivery", false) ? "Express delivery" : "Standard delivery";

            String image = product.optJSONObject("media") != null ?
                    "https://cdn.elcorteingles.es/images/p/" + product.getString("id") :
                    "No image found";

            String rating = "No rating found";

            Product productObj = new Product(
                    title,
                    discount,
                    actualPrice,
                    oldPrice,
                    image,
                    rating,
                    delivery,
                    buildProductUrl(product),
                    "PCComponentes");
            productList.add(productObj);
        }
    }

    private String buildProductUrl(JSONObject product) {
        String[] category = product.getJSONArray("category").toList().toArray(new String[0]);
        String urlPath = String.join("/", category).toLowerCase();
        return "https://www.elcorteingles.es/" + urlPath + "/" + product.getString("id");
    }
}