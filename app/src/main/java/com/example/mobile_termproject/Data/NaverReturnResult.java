package com.example.mobile_termproject.Data;

public class NaverReturnResult {
    public final String name;
    public final String imageUrl;
    public final String category1;
    public final String category2;
    public final String category3;
    public final String category4;

    public NaverReturnResult(String name, String imageUrl,String c1, String c2, String c3, String c4){
        this.name = name;
        this.imageUrl = imageUrl;
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

