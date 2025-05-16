package com.scrapspringboot.model;


public class Product {
    private String title;
    private String discount;
    private Double actualPrice;
    private Double oldPrice;
    private String image;
    private String rating;
    private String delivery;
    private String url;
    private String empresa;

    public Product(String title, String discount, Double actualPrice, Double oldPrice, String image, String rating, String delivery, String url, String empresa) {
        this.title = title;
        this.discount = discount;
        this.actualPrice = actualPrice;
        this.oldPrice = oldPrice;
        this.image = image;
        this.rating = rating;
        this.delivery = delivery;
        this.url = url;
        this.empresa = empresa;
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

    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }

    public String getEmpresa() {
        return empresa;
    }
}