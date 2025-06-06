package com.example.mobile_termproject.API;

import static com.example.mobile_termproject.API.ApiManager.CLIENT_ID;
import static com.example.mobile_termproject.API.ApiManager.CLIENT_SECRET;
import static com.example.mobile_termproject.API.ApiManager.NAVER_BASE_URL;

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
import com.example.mobile_termproject.Acitivities.BaseActivity;
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

import javax.net.ssl.HttpsURLConnection;

public class Barcode {
    /*
    바코드 관련 helper 클래스.
     */
    public static final int REQUEST_PERMISSION_CAMERA = 1;
    public static final int REQUEST_IMAGE_CAPTURE = 10;
    private Uri photoUri;
    private File photoFile;

    String TAGDebug = BaseActivity.TAGdebug;
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
        if(ContextCompat.checkSelfPermission(activity,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(activity,
                    new String[]{android.Manifest.permission.CAMERA},
                    REQUEST_PERMISSION_CAMERA);
            Log.d(TAGDebug, "권한 체크 완료");
        } else {
            Log.d(TAGDebug, "권한 존재");
        }
    }
    public void launchCamera(Activity activity) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        checkCameraPermission(activity);
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            // != null 인 경우 -> 실행할 카메라 앱 존재. 없을 경우 null 반환
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
                Log.d(TAGDebug, "ERROR: " + e.getMessage());
            }
        }
    }

    public void launchCamera(Activity activity, Boolean isTest){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        checkCameraPermission(activity);
        if (isTest) {
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

                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);

                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                activity.startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(activity, "파일 생성 오류: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.d(TAGDebug, "ERROR: " + e.getMessage());
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
                .addOnFailureListener(e ->{
                    listner.onFailure("fail to read barcode: " + e.getMessage());
                });
    }

    public interface BarcodeResultListner {
        void onSuccess(String barcodeValue);
        void onFailure(String errMsg);
    }

    public String getInfo(String barcodeValue){
        String result = "";
        String urlStr ="";
        int responseCode = 0;
        try {
            urlStr = ApiManager.FLASKIP + "/getInfo?barcode=" + barcodeValue;
            URL url = new URL(urlStr);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", "Android-App");
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            responseCode = connection.getResponseCode();



            if (responseCode == HttpsURLConnection.HTTP_OK) {
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
            result = "요청 실패: " + e;
        }
        Log.d(TAGDebug, "요청 URL: " + urlStr);
        Log.d(TAGDebug, "응답 코드: " + responseCode);
        Log.d(TAGDebug, "서버 응답 내용: " + result);
        return result;
    }

}
