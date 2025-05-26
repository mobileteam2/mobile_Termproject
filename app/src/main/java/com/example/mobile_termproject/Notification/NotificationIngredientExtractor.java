package com.example.mobile_termproject.Notification;

import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.app.Notification;

public class NotificationIngredientExtractor {

    public String extractIngredient(StatusBarNotification sbn) {
        String packageName = sbn.getPackageName();
        Notification notification = sbn.getNotification();
        Bundle extras = notification.extras;

        String title = extras.getString("android.title", "");
        String text = extras.getString("android.text", "");
        String bigText = extras.getString("android.bigText", "");

        switch (packageName) {
            case "com.nhn.android.search": // 네이버
                return extractFromNaver(title, text, bigText);

            case "net.bucketplace": // 오늘의집
                return extractFromBucketplace(text);

            default:
                return ""; // 또는 "기타"
        }
    }

    private String extractFromNaver(String title, String text, String bigText) {
        // 예시: title = "안심밥상", text = "슈퍼POINT 총1.8kg ... 상품이 결제되었습니다."
        if (title != null && !title.isEmpty()) {
            return title + " / " + extractBeforeKeyword(bigText, "상품이");
        }
        return extractBeforeKeyword(bigText, "상품이");
    }

    private String extractFromBucketplace(String text) {
        // 예시: "파스타소스 600g 3병+면... 잘 받으셨나요?"
        return extractBeforeKeyword(text, "잘 받으셨나요");
    }

    private String extractBeforeKeyword(String source, String keyword) {
        if (source == null) return "";
        int idx = source.indexOf(keyword);
        if (idx > 0) {
            return source.substring(0, idx).trim();
        }
        return source;
    }
}

