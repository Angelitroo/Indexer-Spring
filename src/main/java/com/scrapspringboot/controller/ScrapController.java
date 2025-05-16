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
import java.util.Collections;

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

    private void setEmpresaForProducts(List<Product> products, String empresaName) {
        if (products != null) {
            for (Product product : products) {
                product.setEmpresa(empresaName);
            }
        }
    }

    @GetMapping("/search/{value}")
    public ResponseEntity<List<Product>> getProducts(@PathVariable String value) {
        List<Product> ebayProducts = scrapEbay.scrapEbay(value);
        setEmpresaForProducts(ebayProducts, "eBay");
        logger.info("eBay products found (raw): {}", ebayProducts.size());

        List<Product> mediaMarktProducts = scrapMediaMarkt.scrapMediaMarkt(value);
        setEmpresaForProducts(mediaMarktProducts, "MediaMarkt");
        logger.info("MediaMarkt products found (raw): {}", mediaMarktProducts.size());

        List<Product> corteInglesProducts = scrapElCorteIngles.scrapElCorteIngles(value);
        setEmpresaForProducts(corteInglesProducts, "El Corte Inglés");
        logger.info("El Corte Inglés products found (raw): {}", corteInglesProducts.size());

        List<Product> carrefourProducts = scrapCarrefour.scrapCarrefour(value);
        setEmpresaForProducts(carrefourProducts, "Carrefour");
        logger.info("Carrefour products found (raw): {}", carrefourProducts.size());

        List<Product> amazonProducts = scrapAmazon.scrapAmazon(value);
        setEmpresaForProducts(amazonProducts, "Amazon");
        logger.info("Amazon products found (raw): {}", amazonProducts.size());

        List<Product> pccomponentesProducts = scrapPComponentes.scrapPComponentes(value);
        setEmpresaForProducts(pccomponentesProducts, "PCComponentes");
        logger.info("PcComponentes products found (raw): {}", pccomponentesProducts.size());

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

        double deviationThreshold = 0.5;
        List<Product> filteredEbay = filterOutliers(ebayProducts, ebayMedian, deviationThreshold);
        logger.info("eBay products after outlier filter: {}", filteredEbay.size());
        List<Product> filteredMediaMarkt = filterOutliers(mediaMarktProducts, mediaMarktMedian, deviationThreshold);
        logger.info("MediaMarkt products after outlier filter: {}", filteredMediaMarkt.size());
        List<Product> filteredCorteIngles = filterOutliers(corteInglesProducts, corteInglesMedian, deviationThreshold);
        logger.info("El Corte Inglés products after outlier filter: {}", filteredCorteIngles.size());
        List<Product> filteredCarrefour = filterOutliers(carrefourProducts, carrefourMedian, deviationThreshold);
        logger.info("Carrefour products after outlier filter: {}", filteredCarrefour.size());
        List<Product> filteredAmazon = filterOutliers(amazonProducts, amazonMedian, deviationThreshold);
        logger.info("Amazon products after outlier filter: {}", filteredAmazon.size());
        List<Product> filteredPcComponentes = filterOutliers(pccomponentesProducts, pccomponentesMedian, deviationThreshold);
        logger.info("PcComponentes products after outlier filter: {}", filteredPcComponentes.size());

        List<Product> top5Ebay = getTop5ClosestToMedian(filteredEbay, ebayMedian);
        logger.info("eBay top 5 products: {}", top5Ebay.size());
        List<Product> top5MediaMarkt = getTop5ClosestToMedian(filteredMediaMarkt, mediaMarktMedian);
        logger.info("MediaMarkt top 5 products: {}", top5MediaMarkt.size());
        List<Product> top5CorteIngles = getTop5ClosestToMedian(filteredCorteIngles, corteInglesMedian);
        logger.info("El Corte Inglés top 5 products: {}", top5CorteIngles.size());
        List<Product> top5Carrefour = getTop5ClosestToMedian(filteredCarrefour, carrefourMedian);
        logger.info("Carrefour top 5 products: {}", top5Carrefour.size());
        List<Product> top5Amazon = getTop5ClosestToMedian(filteredAmazon, amazonMedian);
        logger.info("Amazon top 5 products: {}", top5Amazon.size());
        List<Product> top5PcComponentes = getTop5ClosestToMedian(filteredPcComponentes, pccomponentesMedian);
        logger.info("PcComponentes top 5 products: {}", top5PcComponentes.size());

        List<Product> result = new ArrayList<>();
        result.addAll(top5Ebay);
        result.addAll(top5MediaMarkt);
        result.addAll(top5CorteIngles);
        result.addAll(top5Carrefour);
        result.addAll(top5Amazon);
        result.addAll(top5PcComponentes);

        logger.info("Total products being sent to frontend: {}", result.size());
        if (result.isEmpty()) {
            logger.warn("No products are being sent to the frontend for query: {}", value);
        } else {
           }

        return ResponseEntity.ok(result);
    }

    private List<Product> filterOutliers(List<Product> products, double median, double deviationThreshold) {
        if (products == null || products.isEmpty() || median == 0) {
            return new ArrayList<>();
        }
        return products.stream()
                .filter(p -> {
                    double price = p.getActualPrice();
                    double relativeDifference = Math.abs(price - median) / median;
                    return relativeDifference <= deviationThreshold;
                })
                .collect(Collectors.toList());
    }

    private double calculateMedian(List<Product> products) {
        if (products == null || products.isEmpty()) {
            return 0;
        }
        List<Double> prices = products.stream()
                .limit(5)
                .map(Product::getActualPrice)
                .sorted()
                .collect(Collectors.toList());

        int size = prices.size();
        if (size == 0) return 0;
        if (size % 2 == 0) {
            return (prices.get(size / 2 - 1) + prices.get(size / 2)) / 2.0;
        } else {
            return prices.get(size / 2);
        }
    }

    private List<Product> getTop5ClosestToMedian(List<Product> products, double median) {
        if (products == null || products.isEmpty()) {
            return new ArrayList<>();
        }
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