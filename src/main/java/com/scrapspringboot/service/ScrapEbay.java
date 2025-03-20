package com.scrapspringboot.service;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class ScrapEbay {
    public static void main(String[] args) {
        // URL for eBay search results for "ps5"
        String url = "https://www.ebay.es/sch/i.html?_nkw=ps5";

        // Set up ChromeOptions (e.g., uncomment headless mode if desired)
        ChromeOptions options = new ChromeOptions();
        // options.addArguments("--headless");

        // Initialize ChromeDriver with the options
        ChromeDriver driver = new ChromeDriver(options);

        try {
            // Open the target URL
            driver.get(url);

            // Wait until listing containers (<li class="s-item">) are visible (timeout: 10 seconds)
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("li.s-item")));

            // Find all listing items on the page
            List<WebElement> items = driver.findElements(By.cssSelector("li.s-item"));

            // Iterate over each listing item
            for (WebElement item : items) {
                try {
                    // Extract the anchor element containing the link and title
                    WebElement linkElement = item.findElement(By.cssSelector("a.s-item__link"));

                    // Get the title text from the descendant element with class "s-item__title"
                    String title = "";
                    try {
                        WebElement titleElement = linkElement.findElement(By.cssSelector("div.s-item__title"));
                        title = titleElement.getText();
                    } catch (Exception e) {
                        // Fallback: if the inner structure is different, get the link's text directly
                        title = linkElement.getText();
                    }

                    // Get the link URL from the href attribute
                    String link = linkElement.getAttribute("href");

                    // Extract the price from the listing; it is typically in an element with class "s-item__price"
                    String price = "";
                    try {
                        WebElement priceElement = item.findElement(By.cssSelector("span.s-item__price"));
                        price = priceElement.getText();
                    } catch (Exception e) {
                        price = "Price not found";
                    }

                    // Print the extracted data
                    System.out.println("Title: " + title);
                    System.out.println("Price: " + price);
                    System.out.println("Link: " + link);
                    System.out.println("---------------------");
                } catch (Exception e) {
                    // Log or handle any errors for this listing item if needed
                    System.out.println("Error extracting data from one of the items: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Always quit the driver to close the browser session
            driver.quit();
        }
    }
}
