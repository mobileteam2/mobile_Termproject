package com.example.mobile_termproject;

public class FoodItem {
    private String name;
    private String category;
    private String expiration;  // 또는 Expiration 객체로 바꾸고 싶으면 알려줘

    public FoodItem() {
        // Firebase 역직렬화용 기본 생성자
    }

    public FoodItem(String name, String category, String expiration) {
        this.name = name;
        this.category = category;
        this.expiration = expiration;
    }

    // getter & setter
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }

    public String getExpiration() {
        return expiration;
    }
    public void setExpiration(String expiration) {
        this.expiration = expiration;
    }
}
