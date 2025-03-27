package com.scrapspringboot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scrapspringboot.model.Product;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class ScrapCarrefour {
    private static final String API_BASE_URL = "https://www.carrefour.es/search-api/query/v1/search";
    private static final Logger logger = LoggerFactory.getLogger(ScrapCarrefour.class);
    private final ChromeOptions chromeOptions;

    public ScrapCarrefour(ChromeOptions chromeOptions) {
        this.chromeOptions = chromeOptions;
    }

    public List<Product> scrapCarrefour(String value) {
        List<Product> products = new ArrayList<>();
        ChromeDriver driver = new ChromeDriver(chromeOptions);

        try {
            String encodedQuery = URLEncoder.encode(value, StandardCharsets.UTF_8);
            String apiUrl = API_BASE_URL +
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
            logger.info("Accessing Carrefour API: {}", apiUrl);

            String pageSource = driver.getPageSource();
            String json = pageSource;
            if (pageSource.contains("<pre>")) {
                json = pageSource.substring(pageSource.indexOf("<pre>") + 5, pageSource.indexOf("</pre>"));
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(json);
            JsonNode docs = rootNode.path("content").path("docs");

            logger.info("Found {} products from Carrefour", docs.size());

            for (JsonNode productNode : docs) {
                String title = productNode.path("display_name").asText();
                double activePrice = productNode.path("active_price").asDouble();
                double listPrice = productNode.path("list_price").asDouble();

                String discount = "No discount";
                if (listPrice > activePrice) {
                    double discountPercent = (listPrice - activePrice) / listPrice * 100;
                    discount = String.format("%.0f%%", discountPercent);
                }

                String image = "No image found";
                if (productNode.has("image_path") && productNode.path("image_path").has("nonFood")) {
                    image = productNode.path("image_path").path("nonFood").asText();
                }

                String productUrl = "No product url found";
                if (productNode.has("urls") && productNode.path("urls").has("nonFood")) {
                    String productPath = productNode.path("urls").path("nonFood").asText();
                    productUrl = "https://www.carrefour.es" + productPath;
                }

                String rating = "No rating found";
                String delivery = "No delivery info found";

                Product product = new Product(
                        title,
                        discount,
                        activePrice,
                        listPrice,
                        image,
                        rating,
                        delivery,
                        productUrl
                );
                products.add(product);
            }

        } catch (Exception e) {
            logger.error("Error scraping Carrefour: {}", e.getMessage());
        } finally {
            driver.quit();
            logger.info("ChromeDriver closed after Carrefour search");
        }

        return products;
    }
}