package com.scrapspringboot.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.json.JSONArray;
import org.json.JSONObject;

public class ScrapMediaMarkt {
    public static void main(String[] args) {
        try {
            // Conectarse y obtener el HTML de la página
            Document doc = Jsoup.connect("https://www.mediamarkt.es/es/search.html?query=ps5").get();

            // Seleccionar el primer script con JSON-LD
            Element scriptTag = doc.selectFirst("script[type=application/ld+json]");
            if(scriptTag != null) {
                String jsonText = scriptTag.html();

                // Parsear el contenido JSON
                JSONObject jsonObj = new JSONObject(jsonText);
                JSONArray items = jsonObj.getJSONArray("itemListElement");

                // Recorrer cada producto
                for (int i = 0; i < items.length(); i++) {
                    JSONObject item = items.getJSONObject(i).getJSONObject("item");
                    String titulo = item.getString("name");
                    // Extraer el precio
                    JSONObject offers = item.getJSONObject("offers");
                    double precio = offers.getDouble("price");

                    System.out.println("Título: " + titulo);
                    System.out.println("Precio: " + precio);
                    System.out.println("----------------------");
                }
            } else {
                System.out.println("No se encontró el script JSON-LD.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
