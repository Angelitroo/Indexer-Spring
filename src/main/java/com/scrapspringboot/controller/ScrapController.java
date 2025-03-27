package com.scrapspringboot.controller;

import com.scrapspringboot.model.Product;
import com.scrapspringboot.service.*;
import com.scrapspringboot.util.ScrapAnalyzer;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/scrap")
public class ScrapController {

    private static final Logger logger = LoggerFactory.getLogger(ScrapController.class);

    private final ScrapEbay scrapEbay;
    private final ScrapMediaMarkt scrapMediaMarkt;
    private final ScrapElCorteIngles scrapElCorteIngles;
    private final ScrapCarrefour scrapCarrefour;
    private final ScrapAmazon scrapAmazon;
    private final ScrapPComponentes scrapPComponentes;
    private final ChromeOptions chromeOptions;

    public ScrapController(ScrapEbay scrapEbay,
                           ScrapMediaMarkt scrapMediaMarkt,
                           ScrapElCorteIngles scrapElCorteIngles,
                           ScrapCarrefour scrapCarrefour,
                            ScrapAmazon scrapAmazon,
                            ScrapPComponentes scrapPComponentes,
                           ChromeOptions chromeOptions) {
        this.scrapEbay = scrapEbay;
        this.scrapMediaMarkt = scrapMediaMarkt;
        this.scrapElCorteIngles = scrapElCorteIngles;
        this.scrapCarrefour = scrapCarrefour;
        this.scrapAmazon = scrapAmazon;
        this.scrapPComponentes = scrapPComponentes;
        this.chromeOptions = chromeOptions;
    }

    @GetMapping("/search/{value}")
    public ResponseEntity<List<Product>> getProducts(@PathVariable String value) {
        // Get products from each source
        List<Product> ebayProducts = scrapEbay.scrapEbay(value);
        List<Product> mediaMarktProducts = scrapMediaMarkt.scrapMediaMarkt(value);
        List<Product> corteInglesProducts = scrapElCorteIngles.scrapElCorteIngles(value);
        List<Product> carrefourProducts = scrapCarrefour.scrapCarrefour(value);
        List<Product> amazonProducts = scrapAmazon.scrapAmazon(value);
        List<Product> pccomponentesProducts = scrapPComponentes.scrapPComponentes(value);

        logger.info("eBay products found: {}", ebayProducts.size());
        logger.info("MediaMarkt products found: {}", mediaMarktProducts.size());
        logger.info("El Corte Inglés products found: {}", corteInglesProducts.size());
        logger.info("Carrefour products found: {}", carrefourProducts.size());
        logger.info("Amazon products found: {}", amazonProducts.size());
        logger.info("PcComponentes products found: {}", pccomponentesProducts.size());

        double ebayMedian = calculateMedian(ebayProducts);
        double mediaMarktMedian = calculateMedian(mediaMarktProducts);
        double corteInglesMedian = calculateMedian(corteInglesProducts);
        double carrefourMedian = calculateMedian(carrefourProducts);
        double amazonMedian = calculateMedian(amazonProducts);
        double pccomponentesMedian = calculateMedian(pccomponentesProducts);

        logger.info("eBay median price: {}", ebayMedian);
        logger.info("MediaMarkt median price: {}", mediaMarktMedian);
        logger.info("El Corte Inglés median price: {}", corteInglesMedian);
        logger.info("Carrefour median price: {}", carrefourMedian);
        logger.info("Amazon median price: {}", amazonMedian);
        logger.info("PcComponentes median price: {}", pccomponentesMedian);

        double deviationThreshold = 0.5; // 50% deviation threshold
        List<Product> filteredEbay = filterOutliers(ebayProducts, ebayMedian, deviationThreshold);
        List<Product> filteredMediaMarkt = filterOutliers(mediaMarktProducts, mediaMarktMedian, deviationThreshold);
        List<Product> filteredCorteIngles = filterOutliers(corteInglesProducts, corteInglesMedian, deviationThreshold);
        List<Product> filteredCarrefour = filterOutliers(carrefourProducts, carrefourMedian, deviationThreshold);
        List<Product> filteredAmazon = filterOutliers(amazonProducts, amazonMedian, deviationThreshold);
        List<Product> filteredPcComponentes = filterOutliers(pccomponentesProducts, pccomponentesMedian, deviationThreshold);

        List<Product> top5Ebay = getTop5ClosestToMedian(filteredEbay, ebayMedian);
        List<Product> top5MediaMarkt = getTop5ClosestToMedian(filteredMediaMarkt, mediaMarktMedian);
        List<Product> top5CorteIngles = getTop5ClosestToMedian(filteredCorteIngles, corteInglesMedian);
        List<Product> top5Carrefour = getTop5ClosestToMedian(filteredCarrefour, carrefourMedian);
        List<Product> top5Amazon = getTop5ClosestToMedian(filteredAmazon, amazonMedian);
        List<Product> top5PcComponentes = getTop5ClosestToMedian(filteredPcComponentes, pccomponentesMedian);

        List<Product> result = new ArrayList<>();
        result.addAll(top5Ebay);
        result.addAll(top5MediaMarkt);
        result.addAll(top5CorteIngles);
        result.addAll(top5Carrefour);
        result.addAll(top5Amazon);
        result.addAll(top5PcComponentes);

        return ResponseEntity.ok(result);
    }

    private List<Product> filterOutliers(List<Product> products, double median, double deviationThreshold) {
        return products.stream()
                .filter(p -> {
                    double price = p.getActualPrice();
                    double relativeDifference = Math.abs(price - median) / median;
                    // Filter out prices that deviate too much from median
                    return relativeDifference <= deviationThreshold;
                })
                .collect(Collectors.toList());
    }

    private double calculateMedian(List<Product> products) {
        List<Double> prices = products.stream()
                .limit(5)  // Avoids using irrelevant data like controllers.
                .map(Product::getActualPrice)
                .sorted()
                .collect(Collectors.toList());

        int size = prices.size();
        if (size == 0) return 0;
        if (size % 2 == 0) {
            return (prices.get(size/2 - 1) + prices.get(size/2)) / 2.0;
        } else {
            return prices.get(size/2);
        }
    }

    private List<Product> getTop5ClosestToMedian(List<Product> products, double median) {
        return products.stream()
                .sorted((p1, p2) -> {
                    double diff1 = Math.abs(p1.getActualPrice() - median);
                    double diff2 = Math.abs(p2.getActualPrice() - median);
                    return Double.compare(diff1, diff2);
                })
                .limit(5)
                .collect(Collectors.toList());
    }

    @GetMapping("/analyze/carrefour")
    public ResponseEntity<Map<String, Object>> analyzeCarrefour(@RequestParam String query) {
        logger.info("Analyzing Carrefour structure for query: {}", query);
        Map<String, Object> result = ScrapAnalyzer.analyzeCarrefour(chromeOptions, query);
        return ResponseEntity.ok(result);
    }
}