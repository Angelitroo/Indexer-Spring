package com.scrapspringboot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scrapspringboot.model.Product;
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
    private static final String URL = "https://www.pccomponentes.com/api/articles/search?&query=";
    private static final Logger logger = LoggerFactory.getLogger(ScrapPComponentes.class);
    private final ChromeOptions chromeOptions;

    public ScrapPComponentes(ChromeOptions chromeOptions) {
        this.chromeOptions = chromeOptions;
    }

    public List<Product> scrapPComponentes(final String value) {
        List<Product> resultados = new ArrayList<>();
        WebDriver driver = new ChromeDriver(chromeOptions);

        try {
            driver.get(URL + value);
            String pageSource = driver.getPageSource();
            String json = pageSource.substring(pageSource.indexOf("{"), pageSource.lastIndexOf("}") + 1);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(json);
            JsonNode articles = rootNode.path("articles");

            for (JsonNode article : articles) {
                String title = article.path("name").asText("No title found");
                String discount = article.path("discount").asText("No discount found") + "%";
                double actualPrice = article.path("promotionalPrice").asDouble(0.0);
                double oldPrice = article.path("originalPrice").asDouble(actualPrice);
                String image = article.path("images").path("medium").path("path").asText("No image found");
                String rating = article.path("ratingAvg").asText("No rating found");
                String delivery = article.path("delivery").path("minDeliveryDate").asText("No delivery info found");
                String urlProduct = "https://www.pccomponentes.com/" + article.path("slug").asText("");

                if (!"No discount found".equals(discount)) {
                    Product product = new Product(title, discount, actualPrice, oldPrice, image, rating, delivery, urlProduct);
                    resultados.add(product);
                }
            }
        } catch (Exception e) {
            logger.error("Error en el scraping: {}", e.getMessage());
        } finally {
            driver.quit();
            logger.info("ChromeDriver closed after scraping");
        }
        return resultados;
    }
}