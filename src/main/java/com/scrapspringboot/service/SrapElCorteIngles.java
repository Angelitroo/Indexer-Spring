package com.scrapspringboot.service;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.Duration;
import java.util.List;

public class SrapElCorteIngles {
    public static void main(String[] args) {
        String url = "https://www.elcorteingles.es/videojuegos/ps5/?s=ps5";

        // Set up ChromeOptions; uncomment headless mode if desired
        ChromeOptions options = new ChromeOptions();
        // options.addArguments("--headless");

        // Initialize ChromeDriver with the options
        ChromeDriver driver = new ChromeDriver(options);

        try {
            // Open the target URL
            driver.get(url);

            // Wait until at least one <script> element is present on the page (timeout: 30 seconds)
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("script")));

            // Get all <script> elements from the page
            List<WebElement> scripts = driver.findElements(By.tagName("script"));
            boolean dataLayerFound = false;

            for (WebElement script : scripts) {
                // Get the inner HTML content of each script element
                String scriptContent = script.getAttribute("innerHTML");
                if (scriptContent.contains("dataLayer =")) {
                    dataLayerFound = true;

                    // Extract the JSON content starting right after "dataLayer ="
                    int startIndex = scriptContent.indexOf("dataLayer =") + "dataLayer =".length();
                    String jsonText = scriptContent.substring(startIndex).trim();

                    // Remove the trailing semicolon if it exists
                    if (jsonText.endsWith(";")) {
                        jsonText = jsonText.substring(0, jsonText.length() - 1);
                    }

                    // Parse the JSON content into a JSONArray
                    JSONArray dataLayerArray = new JSONArray(jsonText);
                    if (dataLayerArray.length() > 0) {
                        JSONObject firstObject = dataLayerArray.getJSONObject(0);
                        // Check if the "products" array exists
                        if (firstObject.has("products")) {
                            JSONArray products = firstObject.getJSONArray("products");
                            // Iterate over each product and extract the title ("name") and final price ("f_price")
                            for (int i = 0; i < products.length(); i++) {
                                JSONObject product = products.getJSONObject(i);
                                String title = product.optString("name", "N/A");
                                JSONObject priceObj = product.optJSONObject("price");
                                double fPrice = priceObj != null ? priceObj.optDouble("f_price", 0) : 0;

                                System.out.println("Título: " + title);
                                System.out.println("Precio: " + fPrice);
                                System.out.println("---------------------");
                            }
                        } else {
                            System.out.println("No se encontró el array 'products' en el JSON.");
                        }
                    } else {
                        System.out.println("El array dataLayer está vacío.");
                    }
                    // Exit the loop once the relevant script is processed
                    break;
                }
            }
            if (!dataLayerFound) {
                System.out.println("No se encontró ningún script con 'dataLayer ='");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Always quit the driver to close the browser
            driver.quit();
        }
    }
}
