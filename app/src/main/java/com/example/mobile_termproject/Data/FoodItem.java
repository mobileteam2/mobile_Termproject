package com.example.mobile_termproject.Data;

public class FoodItem {
    private String name;
    private String category;
    private String expiration;  // 또는 Expiration 객체로 바꾸고 싶으면 알려줘

    public Expiration expirationc;

    private String storagetype;
    private String docId;

    private String imageUrl;
    private long timestamp;

    public FoodItem() {
        // Firebase 역직렬화용 기본 생성자
        this("테스트음식", "테스트카테고리", "테스트유통기한", null, null);
    }

    public FoodItem(String name, String category, String expiration) {
        this(name, category, expiration, null, null);
    }
    public FoodItem(String name, String category, String expiration, String docId) {
        this(name, category, expiration, docId, null);
    }
    public FoodItem(String name, String category, String expiration, String docId, String imageUrl) {
        this.name = name;
        this.category = category;
        this.expiration = expiration;
        this.docId = docId;
        this.imageUrl = imageUrl;
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

    public Expiration getExpirationc() {
        return expirationc;
    }

    public void setExpirationc(Expiration expirationc) {
        this.expirationc = expirationc;
    }

    public void setImageUrl(String imageUrl){ this.imageUrl = imageUrl; }
    public String getImageUrl() { return imageUrl; }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getId(){ return docId; }

    public String getStoragetype() {
        return storagetype;
    }

    public void setStoragetype(String storagetype) {
        this.storagetype = storagetype;
    }
}
