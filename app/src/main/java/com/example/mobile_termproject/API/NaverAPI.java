package com.example.mobile_termproject.API;


/*
NaverAPI 통합 클래스

Barcode:
    식품 이름 전달 ->
    NAVER API 로 전달 ->
    결과값 반환 : 식품 이름, 이미지, 카테고리 ->

    DB에 저장 ->
 */

import static com.example.mobile_termproject.API.ApiManager.CLIENT_ID;
import static com.example.mobile_termproject.API.ApiManager.CLIENT_SECRET;
import static com.example.mobile_termproject.API.ApiManager.NAVER_BASE_URL;

import com.example.mobile_termproject.Data.NaverReturnResult;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NaverAPI {
    private final OkHttpClient client = new OkHttpClient();

    public interface NaverCallback {
        void onSuccess(NaverReturnResult result);
        void onFailure(Exception e);
    }

    public void getInfoNaver(String foodName, NaverCallback callback){
        try {
            String encodedFoodName = URLEncoder.encode(foodName, "UTF-8");
            String apiUrl = NAVER_BASE_URL + encodedFoodName;

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

                        JSONObject item = items.getJSONObject(0);
                        String name = item.optString("title", "").replaceAll("<[^>]*>", "");
                        String image = item.optString("image", "");
                        String category1 = item.optString("category1", "");
                        String category2 = item.optString("category2", "");
                        String category3 = item.optString("category3", "");
                        String category4 = item.optString("category4", "");

                        // 필요한 데이터 객체 생성
                        NaverReturnResult result = new NaverReturnResult(
                                name, image,
                                category1, category2, category3, category4
                        );

                        // 성공 콜백
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
