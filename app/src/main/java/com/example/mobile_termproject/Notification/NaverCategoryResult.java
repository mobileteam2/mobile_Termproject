package com.example.mobile_termproject.Notification;

public class NaverCategoryResult {
    public final String category1;
    public final String category2;
    public final String category3;
    public final String category4;

    public NaverCategoryResult(String c1, String c2, String c3, String c4) {
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

    @Override
    public String toString() {
        return String.join(" > ", category1, category2, category3, category4);
    }
}

