package com.example.mobile_termproject.API;

import static com.example.mobile_termproject.API.ApiManager.clientId;
import static com.example.mobile_termproject.API.ApiManager.clientSecret;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import android.Manifest;
import android.util.Log;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;

public class Barcode {
    /*
    바코드 관련 helper 클래스.
     */
    public static final int REQUEST_PERMISSION_CAMERA = 1;
    public static final int REQUEST_IMAGE_CAPTURE = 10;
    private Uri photoUri;
    private File photoFile;
    public Barcode(){}

    public File getPhotoFile(){
        return photoFile;
    }

    public void onRequestPermissionsResult(Activity activity, int requestCode,
                                           String[] permission, int[] grantResults){
        if(requestCode == REQUEST_PERMISSION_CAMERA){
            if(grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED){
                launchCamera(activity);
            } else{
                Toast.makeText(activity, "카메라 권한 필요", Toast.LENGTH_SHORT).show();
            }
        }
    }
    public void checkCameraPermission(Activity activity){
        if(ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(activity,
                    new String[]{android.Manifest.permission.CAMERA},
                    REQUEST_PERMISSION_CAMERA);
        }
    }
    public void launchCamera(Activity activity) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        checkCameraPermission(activity);
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            try {
                File storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                if (storageDir != null && !storageDir.exists()) {
                    storageDir.mkdirs();
                }

                photoFile = new File(storageDir, "image.png");
                photoUri = FileProvider.getUriForFile(
                        activity,
                        activity.getPackageName() + ".fileprovider",
                        photoFile
                );

                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                activity.startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(activity, "파일 생성 오류: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.d("DEBUG", "ERROR: " + e.getMessage());
            }
        }
    }

    public void detectBarcode(
            String imagePath,
            Context context,
            BarcodeResultListner listner
    ){
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        if(bitmap == null){
            return;
        }
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        BarcodeScanner scanner = BarcodeScanning.getClient();

        scanner.process(image)
                .addOnSuccessListener(barcodes -> {
                    if (barcodes.isEmpty()){
                        listner.onFailure("can' find barcode");
                    } else {
                        listner.onSuccess(barcodes.get(0).getRawValue());
                    }
                })
                .addOnFailureListener(e ->
                        listner.onFailure("fail to read barcode: " + e.getMessage()));
    }

    public interface BarcodeResultListner {
        void onSuccess(String barcodeValue);
        void onFailure(String errMsg);
    }

    public String getInfo(String barcodeValue){
        String result = "";
        try {
            String urlStr = ApiManager.BarcodeURL +  "/get_product_info?barcode=" + barcodeValue;
            URL url = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String returnStr = reader.readLine();
                reader.close();
                if (returnStr != null){
                    result = returnStr;
                } else{
                    result = "NULL";
                }
            } else {
                result = "서버 응답 오류: " + responseCode;
            }
        } catch (Exception e) {
            e.printStackTrace();
            result = "요청 실패: " + e.getMessage();
        }
        return result;
    }

    public String getExtraInfoWithNaver(String foodName){
        String result = "";
        try {
            String encoded = URLEncoder.encode(foodName, "UTF-8");
            String apiURL = "https://openapi.naver.com/v1/search/shop.json?query=" + foodName;

            URL url = new URL(apiURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("X-Naver-Client-Id", clientId);
            connection.setRequestProperty("X-Naver-Client-Secret", clientSecret);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK){
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null){
                    response.append(line);
                }
                reader.close();

                JSONObject json = new JSONObject(response.toString());
                JSONArray items = json.getJSONArray("items");

                if(items.length() > 0){
                    JSONObject item = items.getJSONObject(0);
                    String title = item.getString("title").replaceAll("<[^>]*>", "");
                    String image = item.getString("iamge");
                    String category = "";
                    ArrayList<String> categories = new ArrayList<>();
                    for (int i = 0; i < 4; i++){
                        String key = "category"+(i+1);
                        if(item.has(key) && !item.getString(key).isEmpty()){
                            categories.set(i, item.getString(key));
                        }
                    }
                    /*
                        DB 데이터 삽입
                     */

                    result = "food name: " + title + "\nImage: " + image + "\nCategory: " + categories.get(0);
                } else {
                    result = "검색 결과 없음.";
                }
            } else {
                result = "네이버 API error: " + responseCode;
            }
        } catch (Exception e){
            result = "요청 실패: " + e.getMessage();
        }
        return result;
    }

}
