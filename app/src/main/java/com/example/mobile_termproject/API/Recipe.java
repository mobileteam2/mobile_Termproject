package com.example.mobile_termproject.API;

import static com.example.mobile_termproject.API.ApiManager.recipeAPI_KEY;

import android.util.Log;

import com.example.mobile_termproject.Data.RecipeResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class Recipe {
    private static final String BaseUrl = "http://openapi.foodsafetykorea.go.kr/api/";

    public RecipeResponse getRecipes(String foodName){
        RecipeResponse recipeResponse = null;

        try {
            String encoded = URLEncoder.encode(foodName, "UTF-8");
            String fullUrl = BaseUrl + recipeAPI_KEY + "/COOKRCP01/json/1/10/RCP_PARTS_DTLS=" + encoded;

            URL url = new URL(fullUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK){
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder strB = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null){
                    strB.append(line);
                }
                reader.close();

                Gson gson = new GsonBuilder().create();
                recipeResponse = gson.fromJson(strB.toString(), RecipeResponse.class);

            } else {
                Log.d("RECIPE", "API 응답 오류: "+ responseCode);
            }
        } catch (Exception e){
            Log.d("RECIPE", "ERROR: " + e.getMessage());
        }
        return recipeResponse;
    }
}
