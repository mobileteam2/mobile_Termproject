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
                
            case "com.example.myapplication": // 테스트 앱
                return extractFromNaver(title, text, bigText);
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
        if (source == null || keyword == null) return "";

        int keywordIndex = source.indexOf(keyword);
        if (keywordIndex != -1) {
            // keyword 앞부분 추출
            String before = source.substring(0, keywordIndex).trim();

            // ... 또는 … 제거
            before = before.replace("...", "").replace("…", "").trim();

            return before;
        }

        // keyword가 없으면 전체 텍스트 정리해서 반환
        return source.replace("...", "").replace("…", "").trim();
    }
}

