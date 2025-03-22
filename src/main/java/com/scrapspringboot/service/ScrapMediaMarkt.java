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
                String jsonText = scriptTag.html();
                JSONObject jsonObj = new JSONObject(jsonText);
                JSONArray items = jsonObj.getJSONArray("itemListElement");

                for (int i = 0; i < items.length(); i++) {
                    JSONObject item = items.getJSONObject(i).getJSONObject("item");
                    String title = item.getString("name");

                    String image = item.optString("image", "No image found");
                    String url = item.optString("url", "No product url found");
                    JSONObject offers = item.optJSONObject("offers");
                    double precio = 0.0;
                    if (offers != null) {
                        precio = offers.optDouble("price", 0.0);
                    }

                    String discount = "No discount";
                    Double oldPrice = precio;
                    String rating = "No rating found";
                    String delivery = "No delivery info";

                    Product product = new Product(
                            title,
                            discount,
                            precio,
                            oldPrice,
                            image,
                            rating,
                            delivery,
                            url
                    );

                    productList.add(product);
                }
            } else {
                System.out.println("No se encontró el script JSON-LD.");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return productList;

    }
}
