package com.example.mobile_termproject.Notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.mobile_termproject.API.NaverAPI;
import com.example.mobile_termproject.Data.Expiration;
import com.example.mobile_termproject.Data.FoodItem;
import com.example.mobile_termproject.Data.NaverReturnResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

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

        // 알림에서 식재료명 추출
        String ingredientName = extractor.extractIngredient(sbn);
        /*
        result 값 예시

        예시: title = "안심밥상", text = "슈퍼POINT 총1.8kg ... 상품이 결제되었습니다." 인 경우
        -> "안심밥상 / 슈퍼POINT 총1.8kg"

        예시: "파스타소스 600g 3병+면... 잘 받으셨나요?" 인 경우
        -> "파스타소스 600g 3병+면"
         */


        // 네이버 API 활용하여 식재료명에서 카테고리 변환
        NaverAPI fetcher = new NaverAPI();
        fetcher.getInfoNaver(ingredientName, new NaverAPI.NaverCallback() {
            @Override
            public void onSuccess(NaverReturnResult result) {
                String category = result.getFinalCategory();

                long timestamp = sbn.getPostTime();
                // 유통기한 계산
                Map<String, String> expirationResult = ExpirationCalculator.calculateExpirationDates(category, timestamp);

                /*


                식재료명 ingredientName,
                카테고리 category,
                유통기한 expirationResult
                    ㄴ 아래와 같이 구성
                        {
                          "실온": "2025-05-16"
                          "냉장": "2025-05-24",
                          "냉동": "2025-06-13",
                        }


                식재료명, 카테고리, 유통기한을 DB에 저장하는 코드 필요


                */

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                String uid = user.getUid();
                CollectionReference ingredientsRef = db.collection("users").document(uid).collection("ingredients");

                // FoodItem 객체 생성
                FoodItem foodItem = new FoodItem();
                foodItem.setName(ingredientName);
                foodItem.setCategory(category);
                foodItem.setExpirationc(new Expiration(
                        expirationResult.get("냉동"),
                        expirationResult.get("냉장"),
                        expirationResult.get("실온")
                ));
                foodItem.setTimestamp(timestamp);

                // Firestore에 저장
                ingredientsRef.add(foodItem)
                        .addOnSuccessListener(documentReference -> {
                            Log.d(TAG, "식재료 저장 성공: " + documentReference.getId());
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "식재료 저장 실패", e);
                        });


                Log.d("Category", expirationResult.toString());
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("CategoryError", e.getMessage());
            }
        });
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
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "channel_id")
                .setContentTitle("알림 서비스 연결 끊김")
                .setContentText("앱을 다시 실행해주세요.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        // Android 8.0 이상은 채널 필수
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("channel_id", "알림 끊김", NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);
        }
        manager.notify(1001, builder.build());

    }

}
