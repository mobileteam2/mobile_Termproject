package com.example.mobile_termproject.Data;

import java.text.NumberFormat;
import java.util.Locale;

public class NaverReturnResult {
    public final String name;
    public final String imageUrl;
    public final String category1;
    public final String category2;
    public final String category3;
    public final String category4;

    public final String price;
    public final String productUrl;

    public NaverReturnResult(String name, String imageUrl, String price, String productUrl,
                             String c1, String c2, String c3, String c4) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.price = price;
        this.productUrl = productUrl;
        this.category1 = c1;
        this.category2 = c2;
        this.category3 = c3;
        this.category4 = c4;
    }

    public String getFinalCategory() {
        if (!category4.isEmpty()) return category4;
        if (!category3.isEmpty()) return category3;
        if (!category2.isEmpty()) return category2;
        return category1;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() { return imageUrl; }

    @Override
    public String toString() {
        return String.join(" > ", category1, category2, category3, category4);
    }

    public String getPrice() {
        return price;
    }

    public String getFormattedPrice() {
        if (price == null || price.isEmpty()) return "가격 정보 없음";

        try {
            long priceValue = Long.parseLong(price);
            return NumberFormat.getNumberInstance(Locale.KOREA).format(priceValue) + "원";
        } catch (NumberFormatException e) {
            return "가격 정보 오류";
        }
    }

    public String getProductUrl() {
        return productUrl;
    }
}

