package com.scrapspringboot.controller;

import com.scrapspringboot.model.Product;
import com.scrapspringboot.service.ScrapPComponentes;
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
    public List<Product> getValue(@PathVariable String value) {
        return scrapPComponentes.scrape(value);
    }
}