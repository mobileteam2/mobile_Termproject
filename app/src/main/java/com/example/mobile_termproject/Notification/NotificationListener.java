package com.example.mobile_termproject.Notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.mobile_termproject.API.NaverAPI;
import com.example.mobile_termproject.Data.Expiration;
import com.example.mobile_termproject.Data.FoodItem;
import com.example.mobile_termproject.Data.NaverReturnResult;
import com.example.mobile_termproject.R;

public class NotificationListener extends NotificationListenerService {

    private static final String TAG = "NotificationListener";
    private final NotificationIngredientExtractor extractor = new NotificationIngredientExtractor();

    // 허용할 패키지 리스트
    private static final String[] TARGET_PACKAGES = {
            "com.nhn.android.search",     // 예: 네이버 쇼핑
            "net.bucketplace",             // 예: 오늘의 집
            "com.example.myapplication"  // 테스트 앱
    };

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);

        Notification notification = sbn.getNotification();
        if (notification == null) return;

        if (!isTargetPackage(sbn.getPackageName())) {
            Log.d(TAG, "필터링된 패키지: " + sbn.getPackageName());
            return; // 대상 패키지가 아니면 무시
        }

        Log.d(TAG, "알림 수신됨: " + sbn.getPackageName());
        Log.d(TAG, "알림 제목: " + sbn.getNotification().extras.getString("android.title", ""));
        Log.d(TAG, "알림 내용: " + sbn.getNotification().extras.getString("android.bigText", ""));


        // 알림에서 식재료명 추출
        String ingredientName = extractor.extractIngredient(sbn);
        /*
        result 값 예시

        예시: title = "안심밥상", text = "슈퍼POINT 총1.8kg ... 상품이 결제되었습니다." 인 경우
        -> "안심밥상 / 슈퍼POINT 총1.8kg"

        예시: "파스타소스 600g 3병+면... 잘 받으셨나요?" 인 경우
        -> "파스타소스 600g 3병+면"
         */
        Log.d(TAG, "식재료명 추출함 : " + ingredientName);

        // 네이버 API 활용하여 식재료명에서 카테고리 변환
        NaverAPI fetcher = new NaverAPI();
        fetcher.getInfoNaver(ingredientName, new NaverAPI.NaverCallback() {
            @Override
            public void onSuccess(NaverReturnResult result) {
                String category = result.getFinalCategory();
                Log.d(TAG, "카테고리 추출 성공 : " + result.getFinalCategory());
                long timestamp = sbn.getPostTime();

                ExpirationCalculator.calculateExpirationDates(category, timestamp, r -> {
                    // FoodItem 임시 생성
                    FoodItem foodItem = new FoodItem();
                    foodItem.setName(ingredientName);
                    foodItem.setCategory(category);
                    foodItem.setExpirationc(new Expiration(
                            r.get("냉동"),
                            r.get("냉장"),
                            r.get("실온")
                    ));
                    foodItem.setTimestamp(timestamp);
                    foodItem.setImageUrl(result.getImageUrl());
                    // 사용자에게 앱 알림 발송 (FoodItem 객체를 intent로 전달)
                    sendUserNotification(foodItem);
                    Log.d(TAG, "앱 알림 발송 : " + r.get("실온") + " / " + r.get("냉장") + " / " + r.get("냉동"));
                });
            }
            @Override
            public void onFailure(Exception e) {
                Log.e("CategoryError",  e.getMessage());
            }
        });
    }

    public void sendUserNotification(FoodItem item) {
        Log.d(TAG, "발송 전 확인 : " + item.getExpirationc().getRoom() + " / " + item.getExpirationc().getRefrigerated() + " / " + item.getExpirationc().getFrozen());
        Intent intent = new Intent(this, ConfirmIngredientActivity.class);
        intent.putExtra("name", item.getName());
        intent.putExtra("category", item.getCategory());
        intent.putExtra("frozen", item.getExpirationc().getFrozen());
        intent.putExtra("refrigerated", item.getExpirationc().getRefrigerated());
        intent.putExtra("room", item.getExpirationc().getRoom());
        intent.putExtra("timestamp", item.getTimestamp());
        intent.putExtra("imgUrl", item.getImageUrl());

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "confirm_channel")
                .setSmallIcon(R.drawable.app_logo)
                .setContentTitle("새 식재료 감지됨")
                .setContentText(item.getName() + " 정보를 확인하세요")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("confirm_channel", "식재료 확인", NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);
        }

        manager.notify((int) System.currentTimeMillis(), builder.build());
    }



    private boolean isTargetPackage(String packageName) {
        for (String target : TARGET_PACKAGES) {
            if (target.equals(packageName)) return true;
        }
        return false;
    }

    @Override
    public void onListenerDisconnected() {
        super.onListenerDisconnected();

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "disconnect_channel")
                .setSmallIcon(R.drawable.app_logo)
                .setContentTitle("연결 종료됨")
                .setContentText("알림 리스너가 끊어졌습니다.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        // Android 8.0 이상은 채널 생성 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (manager.getNotificationChannel("disconnect_channel") == null) {
                NotificationChannel channel = new NotificationChannel("disconnect_channel", "알림 서비스 상태", NotificationManager.IMPORTANCE_HIGH);
                manager.createNotificationChannel(channel);
            }
        }

        manager.notify(1001, builder.build());
    }



}
