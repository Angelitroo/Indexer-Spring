package com.scrapspringboot.controller;

import com.scrapspringboot.model.Product;
import com.scrapspringboot.service.ScrapAmazon;
import com.scrapspringboot.service.ScrapPComponentes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/scrap")
public class ScrapController {
    private static final Logger logger = LoggerFactory.getLogger(ScrapController.class);
    private final ScrapPComponentes scrapPComponentes;
    private final ScrapAmazon scrapAmazon;

    public ScrapController(ScrapPComponentes scrapPComponentes, ScrapAmazon scrapAmazon) {
        this.scrapPComponentes = scrapPComponentes;
        this.scrapAmazon = scrapAmazon;
    }


    @GetMapping("/search/{value}")
    public ResponseEntity<List<Product>> getProducts(@PathVariable String value) {
        List<Product> products = new ArrayList<>();

        List<Product> amazonProducts = scrapAmazon.scrapAmazon(value);
        List<Product> pComponentesProducts = scrapPComponentes.scrapPComponentes(value);

        logger.info("Amazon products: {}", amazonProducts);
        logger.info("PComponentes products: {}", pComponentesProducts);

        products.addAll(amazonProducts);
        products.addAll(pComponentesProducts);

        return ResponseEntity.ok(products);
    }
}