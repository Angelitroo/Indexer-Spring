package com.scrapspringboot.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.openqa.selenium.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class ScrapAnalyzer {
    private static final Logger logger = LoggerFactory.getLogger(ScrapAnalyzer.class);
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
    public static Map<String, Object> analyzeCarrefour(ChromeOptions options, String sampleQuery) {
        Map<String, Object> structure = new HashMap<>();
        ChromeDriver driver = new ChromeDriver(options);

        try {
            String encodedQuery = URLEncoder.encode(sampleQuery, StandardCharsets.UTF_8);
            String apiUrl = "https://www.carrefour.es/search-api/query/v1/search" +
                    "?internal=true" +
                    "&instance=x-carrefour" +
                    "&env=https%3A%2F%2Fwww.carrefour.es" +
                    "&scope=desktop" +
                    "&lang=es" +
                    "&session=empathy" +
                    "&citrusCatalog=home" +
                    "&raw=true" +
                    "&catalog=nonFood" +
                    "&query=" + encodedQuery;

            driver.get(apiUrl);
            structure.put("url", apiUrl);

            String pageSource = driver.getPageSource();

            String json = pageSource;
            if (pageSource.contains("<pre>")) {
                json = pageSource.substring(pageSource.indexOf("<pre>") + 5, pageSource.indexOf("</pre>"));
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(json);




            JsonNode docs = rootNode.path("content").path("docs");

            if (docs.isArray() && docs.size() > 0) {
                JsonNode firstProduct = docs.get(0);

                structure.put("title", firstProduct.path("display_name").asText());
                structure.put("actualPrice", firstProduct.path("active_price").asDouble());
                structure.put("oldPrice", firstProduct.path("list_price").asDouble());

                double listPrice = firstProduct.path("list_price").asDouble();
                double activePrice = firstProduct.path("active_price").asDouble();
                if (listPrice > activePrice) {
                    double discount = (listPrice - activePrice) / listPrice * 100;
                    structure.put("discount", String.format("%.0f%%", discount));
                }

                if (firstProduct.has("image_path") && firstProduct.path("image_path").has("nonFood")) {
                    structure.put("image", firstProduct.path("image_path").path("nonFood").asText());
                }

                if (firstProduct.has("urls") && firstProduct.path("urls").has("nonFood")) {
                    String productPath = firstProduct.path("urls").path("nonFood").asText();
                    structure.put("productUrl", "https://www.carrefour.es" + productPath);
                }

                structure.put("totalProducts", docs.size());
            } else {
                structure.put("error", "No products found in API response");
            }

        } catch (Exception e) {
            structure.put("error", e.getMessage());
            structure.put("errorType", e.getClass().getName());
            structure.put("stackTrace", e.getStackTrace()[0].toString());
        } finally {
            driver.quit();
        }

        return structure;
    }
}