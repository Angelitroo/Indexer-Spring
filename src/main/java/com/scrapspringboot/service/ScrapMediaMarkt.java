package com.scrapspringboot.service;

import com.scrapspringboot.model.Product;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ScrapMediaMarkt {
    private static final String BASE_URL = "https://www.mediamarkt.es/es/search.html?query=";

    public List<Product> scrapMediaMarkt(String value) {
        List<Product> productList = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(BASE_URL + value).get();
            Element scriptTag = doc.selectFirst("script[type=application/ld+json]");

            if (scriptTag != null) {
                JSONObject jsonObj = new JSONObject(scriptTag.html());
                JSONArray items = jsonObj.getJSONArray("itemListElement");

                for (int i = 0; i < items.length(); i++) {
                    JSONObject itemWrapper = items.getJSONObject(i);

                    if (!itemWrapper.has("item") || itemWrapper.isNull("item") ||
                            !(itemWrapper.get("item") instanceof JSONObject)) {
                        System.out.println("Skipping item at index " + i + " - 'item' is not a JSONObject");
                        continue;
                    }

                    JSONObject item = itemWrapper.getJSONObject("item");

                    String title = item.optString("name", "No title");
                    String image = item.optString("image", "No image found");
                    String url = item.optString("url", "No product url found");
                    JSONObject offers = item.optJSONObject("offers");
                    double price = offers != null ? offers.optDouble("price", 0.0) : 0.0;
                    JSONObject aggregateRating = item.optJSONObject("aggregateRating");
                    String rating = aggregateRating != null ?
                            String.format("%.1f (%d reviews)",
                                    aggregateRating.optDouble("ratingValue", 0.0),
                                    aggregateRating.optInt("reviewCount", 0)) :
                            "No rating found";

                    String discount = "No discount";
                    Double oldPrice = price;
                    String delivery = "No delivery info";

                    Product product = new Product(
                            title,
                            discount,
                            price,
                            oldPrice,
                            image,
                            rating,
                            delivery,
                            url
                    );
                    productList.add(product);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error scraping MediaMarkt: " + e.getMessage());
            e.printStackTrace();
        }
        return productList;
    }
}

