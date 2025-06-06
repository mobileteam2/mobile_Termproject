package com.example.mobile_termproject.Notification;

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
    private static final Map<String, int[]> shelfLifeMap = new HashMap<>();

    static {
        // {실온, 냉장, 냉동} 순서
        shelfLifeMap.put("우유", new int[]{2, 10, 30});
        shelfLifeMap.put("달걀", new int[]{7, 21, 60});
        shelfLifeMap.put("두부", new int[]{1, 7, 30});
        shelfLifeMap.put("고기", new int[]{1, 5, 90});
        shelfLifeMap.put("채소", new int[]{2, 4, 14});
        shelfLifeMap.put("과일", new int[]{3, 7, 30});
        shelfLifeMap.put("냉동식품", new int[]{1, 30, 90});
        // 기본값이 필요한 경우도 고려 가능
    }

    /**
     * timestampMillis: 알림 수신 시간 (epoch milliseconds)
     * category: 식재료 카테고리
     * @return 보관 방식별 유통기한 (yyyy-MM-dd 형식의 String Map)
     */
    public static Map<String, String> calculateExpirationDates(String category, long timestampMillis) {
        int[] days = shelfLifeMap.getOrDefault(category, new int[]{7, 30, 2});

        Map<String, String> result = new HashMap<>();
        result.put("실온", addDays(timestampMillis, days[0]));
        result.put("냉장", addDays(timestampMillis, days[1]));
        result.put("냉동", addDays(timestampMillis, days[2]));

        return result;
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

