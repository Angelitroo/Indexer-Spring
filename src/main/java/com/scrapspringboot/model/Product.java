package com.scrapspringboot.model;

public class Product {
    private String title;
    private String actualPrice;
    private String oldPrice;
    private String image;
    private String rating;
    private String deliveryInfo;

    public Product(String title, String actualPrice, String oldPrice, String image, String rating, String deliveryInfo) {
        this.title = title;
        this.actualPrice = actualPrice;
        this.oldPrice = oldPrice;
        this.image = image;
        this.rating = rating;
        this.deliveryInfo = deliveryInfo;
    }

    // Getters necesarios para la serialización
    public String getTitle() {
        return title;
    }

    public String getActualPrice() {
        return actualPrice;
    }

    public String getOldPrice() {
        return oldPrice;
    }

    public String getImage() {
        return image;
    }

    public String getRating() {
        return rating;
    }

    public String getDeliveryInfo() {
        return deliveryInfo;
    }
}