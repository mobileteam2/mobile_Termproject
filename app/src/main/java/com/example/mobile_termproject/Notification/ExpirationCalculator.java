package com.example.mobile_termproject.Notification;

import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.text.SimpleDateFormat;

/*

        사용법
        long timestamp = 1747181071000L;
        String category = "우유";

        Map<String, String> result = ExpirationCalculator.calculateExpirationDates(category, timestamp);


        이때 results 값은
        {
          "실온": "2025-05-16"
          "냉장": "2025-05-24",
          "냉동": "2025-06-13",
        }

 */
public class ExpirationCalculator {

    // 카테고리별 보관방식에 따른 유통기한 일수 (냉장, 냉동, 실온)
    public interface ExpirationCallback {
        void onResult(Map<String, String> expirationDates);
    }

    /**
     * timestampMillis: 알림 수신 시간 (epoch milliseconds)
     * category: 식재료 카테고리
     * @return 보관 방식별 유통기한 (yyyy-MM-dd 형식의 String Map)
     */
    public static void calculateExpirationDates(String category, long timestampMillis, ExpirationCallback callback) {
        /*
         기존 코드
        int[] days = shelfLifeMap.getOrDefault(category, new int[]{7, 30, 2});

        Map<String, String> result = new HashMap<>();
        result.put("실온", addDays(timestampMillis, days[0]));
        result.put("냉장", addDays(timestampMillis, days[1]));
        result.put("냉동", addDays(timestampMillis, days[2]));
         */

        Map<String, String> result = new HashMap<>();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("ingredients").document(category).get().addOnCompleteListener(task -> {


            if (task.isSuccessful()) {
                DocumentSnapshot doc = task.getResult();
                if (doc != null && doc.exists()) {
                    Long roomDays = doc.getLong("실온");
                    Long coolDays = doc.getLong("냉장");
                    Long freezeDays = doc.getLong("냉동");

                    if (roomDays != null)
                        result.put("실온", addDays(timestampMillis, roomDays.intValue()));
                    if (coolDays != null)
                        result.put("냉장", addDays(timestampMillis, coolDays.intValue()));
                    if (freezeDays != null)
                        result.put("냉동", addDays(timestampMillis, freezeDays.intValue()));
                } else {
                    Log.w("Firestore", "No document for category: " + category);
                }
            } else {
                Log.e("Firestore", "get failed", task.getException());
            }

            Log.d("Firestore", "유통기한 불러오기 성공 : " + result.get("실온") + result.get("냉장") + result.get("냉동"));
            callback.onResult(result); // 한 군데에서 결과 반환
        });
    }

    // 밀리초 기준 날짜에 일수 추가 후 yyyy-MM-dd 문자열 반환
    private static String addDays(long millis, int daysToAdd) {
        Calendar calendar = Calendar.getInstance(java.util.TimeZone.getTimeZone("Asia/Seoul"));
        calendar.setTimeInMillis(millis);
        calendar.set(Calendar.HOUR_OF_DAY, 0);  // 시간 초기화
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.DAY_OF_YEAR, daysToAdd);

        Date newDate = calendar.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setTimeZone(java.util.TimeZone.getTimeZone("Asia/Seoul"));
        return sdf.format(newDate);
    }
}

