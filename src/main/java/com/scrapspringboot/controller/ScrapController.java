package com.scrapspringboot.controller;

import com.scrapspringboot.model.Product;
import com.scrapspringboot.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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


    public ScrapController(ScrapEbay scrapEbay,
                           ScrapMediaMarkt scrapMediaMarkt,
                           ScrapElCorteIngles scrapElCorteIngles
                           ) {
        this.scrapEbay = scrapEbay;
        this.scrapMediaMarkt = scrapMediaMarkt;
        this.scrapElCorteIngles = scrapElCorteIngles;

    }

    @GetMapping("/search/{value}")
    public ResponseEntity<List<Product>> getProducts(@PathVariable String value) {
        // Get products from each source
        List<Product> ebayProducts = scrapEbay.scrapEbay(value);
        List<Product> mediaMarktProducts = scrapMediaMarkt.scrapMediaMarkt(value);
        List<Product> corteInglesProducts = scrapElCorteIngles.scrapElCorteIngles(value);

        logger.info("eBay products found: {}", ebayProducts.size());
        logger.info("MediaMarkt products found: {}", mediaMarktProducts.size());
        logger.info("El Corte Inglés products found: {}", corteInglesProducts.size());
        double ebayMedian = calculateMedian(ebayProducts);
        double mediaMarktMedian = calculateMedian(mediaMarktProducts);
        double corteInglesMedian = calculateMedian(corteInglesProducts);
        logger.info("eBay median price: {}", ebayMedian);
        logger.info("MediaMarkt median price: {}", mediaMarktMedian);
        logger.info("El Corte Inglés median price: {}", corteInglesMedian);
        double deviationThreshold = 0.5; // 50% deviation threshold
        List<Product> filteredEbay = filterOutliers(ebayProducts, ebayMedian, deviationThreshold);
        List<Product> filteredMediaMarkt = filterOutliers(mediaMarktProducts, mediaMarktMedian, deviationThreshold);
        List<Product> filteredCorteIngles = filterOutliers(corteInglesProducts, corteInglesMedian, deviationThreshold);
        List<Product> top5Ebay = getTop5ClosestToMedian(filteredEbay, ebayMedian);
        List<Product> top5MediaMarkt = getTop5ClosestToMedian(filteredMediaMarkt, mediaMarktMedian);
        List<Product> top5CorteIngles = getTop5ClosestToMedian(filteredCorteIngles, corteInglesMedian);

        List<Product> result = new ArrayList<>();
        result.addAll(top5Ebay);
        result.addAll(top5MediaMarkt);
        result.addAll(top5CorteIngles);

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
}