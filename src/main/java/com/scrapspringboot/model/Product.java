package com.scrapspringboot.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.awt.*;


public class Product {
    private String title;
    private String discount;
    private Double actualPrice;
    private Double oldPrice;
    private String image;
    private String rating;
    private String delivery;
    private String url;

    public Product(String title, String discount, Double actualPrice, Double oldPrice, String image, String rating, String delivery, String url) {
        this.title = title;
        this.discount = discount;
        this.actualPrice = actualPrice;
        this.oldPrice = oldPrice;
        this.image = image;
        this.rating = rating;
        this.delivery = delivery;
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public String getDiscount() {
        return discount;
    }

    public Double getActualPrice() {
        return actualPrice;
    }

    public Double getOldPrice() {
        return oldPrice;
    }

    public String getImage() {
        return image;
    }

    public String getRating() {
        return rating;
    }

    public String getDelivery() {
        return delivery;
    }

    public String getUrl() {
        return url;
    }
}