package com.scrapspringboot.controller;

import com.scrapspringboot.model.Product;
import com.scrapspringboot.service.ScrapPComponentes;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/scrap")
public class ScrapController {

    private final ScrapPComponentes scrapPComponentes;

    public ScrapController(ScrapPComponentes scrapPComponentes) {
        this.scrapPComponentes = scrapPComponentes;
    }

    @GetMapping("/search/{value}")
    public ResponseEntity<List<Product>> getProducts(@PathVariable String value) {
        List<Product> products = scrapPComponentes.scrape(value);
        return ResponseEntity.ok(products);
    }
}
