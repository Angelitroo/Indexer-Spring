package com.scrapspringboot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scrapspringboot.model.Product; // Assuming Product model is updated
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ScrapPComponentes {
    private static final String URL_TEMPLATE = "https://www.pccomponentes.com/api/articles/search?&query=";
    private static final Logger logger = LoggerFactory.getLogger(ScrapPComponentes.class);
    private final ChromeOptions chromeOptions;

    public ScrapPComponentes(ChromeOptions chromeOptions) {
        this.chromeOptions = chromeOptions;
    }

    public List<Product> scrapPComponentes(final String value) {
        List<Product> resultados = new ArrayList<>();
        WebDriver driver = new ChromeDriver(chromeOptions);

        try {
            driver.get(URL_TEMPLATE + value);
            String pageSource = driver.getPageSource();
            int startIndex = pageSource.indexOf("{\"articles\":");
            if (startIndex == -1) {
                startIndex = pageSource.indexOf("{");
            }

            String jsonString = "{}";
            if (startIndex != -1) {
                int braceCount = 0;
                int endIndex = -1;
                for (int i = startIndex; i < pageSource.length(); i++) {
                    if (pageSource.charAt(i) == '{') {
                        braceCount++;
                    } else if (pageSource.charAt(i) == '}') {
                        braceCount--;
                        if (braceCount == 0) {
                            endIndex = i;
                            break;
                        }
                    }
                }
                if (endIndex != -1) {
                    jsonString = pageSource.substring(startIndex, endIndex + 1);
                } else {
                    jsonString = pageSource.substring(startIndex, pageSource.lastIndexOf("}") + 1);
                }
            } else {
                logger.warn("Could not find JSON starting point in page source for PCComponentes query: {}", value);
            }


            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(jsonString);
            JsonNode articles = rootNode.path("articles");

            if (articles.isMissingNode() || !articles.isArray()) {
                logger.warn("No 'articles' array found or it's not an array in PCComponentes JSON for query: {}", value);
            } else {
                for (JsonNode article : articles) {
                    String title = article.path("name").asText("No title found");
                    String discountValue = article.path("discount").asText(null);
                    String discount = (discountValue != null && !discountValue.isEmpty()) ? discountValue + "%" : "No discount found";

                    double actualPrice = article.path("promotionalPrice").asDouble(0.0);
                    double oldPrice = article.path("originalPrice").asDouble(actualPrice); // Keep default if not present
                    String image = article.path("images").path("medium").path("path").asText("No image found");
                    String rating = article.path("ratingAvg").asText("No rating found");
                    String delivery = article.path("delivery").path("minDeliveryDate").asText("No delivery info found");
                    String productUrl = "https://www.pccomponentes.com/" + article.path("slug").asText("");

                    if (!"No title found".equals(title) && actualPrice > 0) {
                        Product product = new Product(title, discount, actualPrice, oldPrice, image, rating, delivery, productUrl, "PCComponentes");
                        resultados.add(product);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error en el scraping de PCComponentes para '{}': {}", value, e.getMessage(), e);
        } finally {
            driver.quit();
            logger.info("ChromeDriver closed after PCComponentes scraping for '{}'", value);
        }
        return resultados;
    }
}