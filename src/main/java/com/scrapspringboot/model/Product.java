package com.scrapspringboot.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Product {
    private String title;
    private String actualPrice;
    private String oldPrice;

    public Product(String title, String actualPrice, String oldPrice) {
        this.title = title;
        this.actualPrice = actualPrice;
        this.oldPrice = oldPrice;
    }
}