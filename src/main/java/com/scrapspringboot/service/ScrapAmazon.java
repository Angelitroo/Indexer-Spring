package com.scrapspringboot.service;

import com.scrapspringboot.model.Product;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ScrapAmazon {
    private static final String URL = "https://www.amazon.es/s?k=";

    public List<Product> scrapAmazon(String value) {
        List<Product> products = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(URL + value)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36")
                    .get();

            Elements items = doc.select("div.s-card-container");

            for (Element item : items) {
                Element imagen = item.selectFirst("img.s-image");
                String imageUrl = (imagen != null) ? imagen.attr("src") : "No disponible";

                Element titulo = item.selectFirst("h2.a-size-mini.a-spacing-none.a-color-base.a-text-normal, " +
                        "h2.a-size-base-plus.a-spacing-none.a-color-base.a-text-normal, " +
                        "h2.a-size-mini.a-spacing-none.a-color-base.s-line-clamp-2, " +
                        "h2.a-size-mini.a-spacing-none.a-color-base.s-line-clamp-4");
                String title = (titulo != null) ? titulo.text() : "No disponible";

                Element precioActual = item.selectFirst("span.a-price span.a-offscreen");
                String actualPriceText = cleanPrice(precioActual);
                double actualPrice = Double.parseDouble(actualPriceText);

                Element precioAntiguo = item.selectFirst("span.a-price.a-text-price span.a-offscreen");
                String oldPriceText = cleanPrice(precioAntiguo);
                double oldPrice = oldPriceText.isEmpty() ? actualPrice : Double.parseDouble(oldPriceText);

                String discount = (actualPrice == oldPrice) ? "No discount" : "-" + String.format("%.2f", ((oldPrice - actualPrice) / oldPrice) * 100) + "%";

                Element valoracion = item.selectFirst("span.a-icon-alt, i.a-icon-star-small");
                String rating = (valoracion != null) ? valoracion.text() : "No disponible";

                Element numValoraciones = item.selectFirst("span.a-size-base.s-underline-text, " +
                        "span.a-size-base.a-color-secondary, " +
                        "span.a-size-base.s-link-centralized-style");
                String numReviews = (numValoraciones != null) ? numValoraciones.text() : "No disponible";

                Element enlace = item.selectFirst("a.a-link-normal.s-no-outline");
                String productUrl = (enlace != null) ? "https://www.amazon.es" + enlace.attr("href") : "No disponible";

                if (!discount.equals("No discount") &&
                        !imageUrl.equals("No disponible") &&
                        !title.equals("No disponible") &&
                        !rating.equals("No disponible") &&
                        !numReviews.equals("No disponible") &&
                        !productUrl.equals("No disponible")) {

                    Product product = new Product(title, discount, actualPrice, oldPrice, imageUrl, rating, numReviews, productUrl, "PCComponentes");
                    products.add(product);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return products;
    }

    private String cleanPrice(Element priceElement) {
        if (priceElement == null) {
            return "0.0";
        }
        String priceText = priceElement.text().replaceAll("[^0-9.,]", "").replace(",", ".");
        int lastDotIndex = priceText.lastIndexOf(".");
        if (lastDotIndex != -1) {
            priceText = priceText.substring(0, lastDotIndex).replace(".", "") + priceText.substring(lastDotIndex);
        }
        return priceText;
    }
}