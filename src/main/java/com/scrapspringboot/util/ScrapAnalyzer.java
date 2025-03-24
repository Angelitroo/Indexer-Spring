package com.scrapspringboot.util;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class ScrapAnalyzer {

    public static Map<String, Object> analyzePComponentes(ChromeOptions options, String sampleQuery) {
        Map<String, Object> structure = new HashMap<>();
        ChromeDriver driver = new ChromeDriver(options);

        try {
            driver.get("https://www.pccomponentes.com/search/?query=" + sampleQuery);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement productCard = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.product-card")));

            structure.put("allElements", productCard.getAttribute("outerHTML"));
            structure.put("availableClasses", productCard.getAttribute("class"));
            structure.put("dataAttributes", extractDataAttributes(productCard));

        } catch (Exception e) {
            structure.put("error", e.getMessage());
        } finally {
            driver.quit();
        }
        return structure;
    }

    public static Map<String, Object> analyzeMediaMarkt(String sampleQuery) {
        Map<String, Object> structure = new HashMap<>();

        try {
            Document doc = Jsoup.connect("https://www.mediamarkt.es/es/search.html?query=" + sampleQuery).get();
            Element scriptTag = doc.selectFirst("script[type=application/ld+json]");

            if (scriptTag != null) {
                JSONObject jsonObj = new JSONObject(scriptTag.html());
                structure.put("jsonStructure", jsonObj.toString(2));
                structure.put("availableKeys", jsonObj.keySet());
            }

        } catch (Exception e) {
            structure.put("error", e.getMessage());
        }
        return structure;
    }

    public static Map<String, Object> analyzeCorteIngles(ChromeOptions options, String sampleQuery) {
        Map<String, Object> structure = new HashMap<>();
        ChromeDriver driver = new ChromeDriver(options);

        try {
            driver.get("https://www.elcorteingles.es/search-nwx/?s=" + sampleQuery);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("script")));

            for (WebElement script : driver.findElements(By.tagName("script"))) {
                String content = script.getAttribute("innerHTML");
                if (content.contains("dataLayer =")) {
                    structure.put("dataLayerContent", content);
                    structure.put("scriptType", script.getAttribute("type"));
                    break;
                }
            }

        } catch (Exception e) {
            structure.put("error", e.getMessage());
        } finally {
            driver.quit();
        }
        return structure;
    }
    public static Map<String, Object> analyzeEbay(ChromeOptions options, String sampleQuery) {
        Map<String, Object> structure = new HashMap<>();
        ChromeDriver driver = new ChromeDriver(options);

        try {
            driver.get("https://www.ebay.es/sch/i.html?_nkw=" + sampleQuery);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement listingItem = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("li.s-item")));

            structure.put("allElements", listingItem.getAttribute("outerHTML"));
            structure.put("availableClasses", listingItem.getAttribute("class"));

            // Analyze specific elements
            WebElement title = listingItem.findElement(By.cssSelector("div.s-item__title"));
            structure.put("titleStructure", title.getAttribute("outerHTML"));

            WebElement price = listingItem.findElement(By.cssSelector("span.s-item__price"));
            structure.put("priceStructure", price.getAttribute("outerHTML"));

            WebElement link = listingItem.findElement(By.cssSelector("a.s-item__link"));
            structure.put("linkStructure", link.getAttribute("outerHTML"));

            WebElement image = listingItem.findElement(By.cssSelector("img.s-item__image-img"));
            structure.put("imageStructure", image.getAttribute("outerHTML"));

        } catch (Exception e) {
            structure.put("error", e.getMessage());
        } finally {
            driver.quit();
        }
        return structure;
    }

    private static Map<String, String> extractDataAttributes(WebElement element) {
        Map<String, String> dataAttributes = new HashMap<>();
        String[] attributes = element.getAttribute("outerHTML").split(" ");

        for (String attribute : attributes) {
            if (attribute.startsWith("data-")) {
                String[] parts = attribute.split("=");
                if (parts.length == 2) {
                    dataAttributes.put(parts[0], parts[1].replace("\"", ""));
                }
            }
        }
        return dataAttributes;
    }

}