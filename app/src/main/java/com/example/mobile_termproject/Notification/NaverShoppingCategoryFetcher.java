package com.example.mobile_termproject.Notification;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/*

        사용법

        NaverShoppingCategoryFetcher fetcher = new NaverShoppingCategoryFetcher();
        fetcher.getCategories("우유", new NaverShoppingCategoryFetcher.CategoryCallback() {
        @Override
        public void onSuccess(NaverCategoryResult result) {

            여기에 result를 활용하는 코드 기입

            Log.d("Category", result.toString());
        }

        @Override
        public void onFailure(Exception e) {
            Log.e("CategoryError", e.getMessage());
        }
});


 */

public class NaverShoppingCategoryFetcher {

    private final String CLIENT_ID = "hVnMr1Cg0gzWRqQAvPOs";
    private final String CLIENT_SECRET = "P090ouj2Xu";
    private final OkHttpClient client = new OkHttpClient();

    public interface CategoryCallback {
        void onSuccess(NaverCategoryResult result);
        void onFailure(Exception e);
    }

    public void getCategories(String query, CategoryCallback callback) {
        try {
            String encodedQuery = URLEncoder.encode(query, "UTF-8");
            String apiUrl = "https://openapi.naver.com/v1/search/shop.json?query=" + encodedQuery;

            Request request = new Request.Builder()
                    .url(apiUrl)
                    .addHeader("X-Naver-Client-Id", CLIENT_ID)
                    .addHeader("X-Naver-Client-Secret", CLIENT_SECRET)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onFailure(e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        callback.onFailure(new Exception("응답 실패: " + response.code()));
                        return;
                    }

                    try {
                        JSONObject json = new JSONObject(response.body().string());
                        JSONArray items = json.getJSONArray("items");
                        if (items.length() == 0) {
                            callback.onFailure(new Exception("검색 결과 없음"));
                            return;
                        }

                        JSONObject first = items.getJSONObject(0);
                        NaverCategoryResult result = new NaverCategoryResult(
                                first.optString("category1", ""),
                                first.optString("category2", ""),
                                first.optString("category3", ""),
                                first.optString("category4", "")
                        );
                        callback.onSuccess(result);
                    } catch (Exception e) {
                        callback.onFailure(e);
                    }
                }
            });

        } catch (Exception e) {
            callback.onFailure(e);
        }
    }
}

