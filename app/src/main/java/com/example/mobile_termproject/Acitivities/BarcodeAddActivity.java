package com.example.mobile_termproject.Acitivities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.mobile_termproject.API.Barcode;
import com.example.mobile_termproject.API.NaverAPI;
import com.example.mobile_termproject.Data.Expiration;
import com.example.mobile_termproject.Data.FoodItem;
import com.example.mobile_termproject.Data.NaverReturnResult;
import com.example.mobile_termproject.Notification.ExpirationCalculator;
import com.example.mobile_termproject.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class BarcodeAddActivity extends BaseActivity {
    Barcode barcode = new Barcode();
    Button btnCamera, btnSend;
    ImageView imgCamera;
    NaverAPI api = new NaverAPI();
    private String foodName = null;
    private String barcodeValue = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_barcode);
        setTopAndBottomBar();

        imgCamera = findViewById(R.id.imgCamera);
        btnCamera = findViewById(R.id.btnCamera);
        btnSend = findViewById(R.id.btnImageSend);

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                barcode.launchCamera(BarcodeAddActivity.this, Boolean.TRUE);
                btnCamera.setText("다시 찍기");
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(() -> {
                    foodName = barcode.getInfo(barcodeValue);
                    Log.d(TAGdebug, "FOOD NAME: \n" + foodName);

                    Log.d(TAGdebug, "네이버 API 호출");
                    api.getInfoNaver(foodName, new NaverAPI.NaverCallback() {
                        @Override
                        public void onSuccess(NaverReturnResult result) {
                            Log.d(TAGdebug, "name : " + result.name);
                            Log.d(TAGdebug, "iamgeUrl : " + result.imageUrl);
                            Log.d(TAGdebug, "category : " + result.toString());
                            Map<String, Objects> item = new HashMap<>();
                            long timestamp = System.currentTimeMillis();
                            Map<String, String> expirationResult = ExpirationCalculator.calculateExpirationDates(result.toString(), timestamp);

                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            String uid = user.getUid();
                            CollectionReference ingredientsRef = db.collection("users").document(uid).collection("ingredients");

                            // FoodItem 객체 생성
                            FoodItem foodItem = new FoodItem();
                            foodItem.setName(result.name);
                            foodItem.setCategory(result.toString());
                            foodItem.setExpirationc(new Expiration(
                                    expirationResult.get("냉동"),
                                    expirationResult.get("냉장"),
                                    expirationResult.get("실온")
                            ));
                            foodItem.setTimestamp(timestamp);
                            foodItem.setImageUrl(result.imageUrl);

                            ingredientsRef.add(foodItem)
                                    .addOnSuccessListener(documentReference -> {
                                        Log.d(TAGdebug, "식재료 저장 성공: " + documentReference.getId());
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAGdebug, "식재료 저장 실패", e);
                                    });


                            Log.d("Category", expirationResult.toString());
                        }

                        @Override
                        public void onFailure(Exception e) {

                        }
                    });
                }).start();

            }
        });
        Log.d(TAGdebug,"btn load 완료");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == barcode.REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){
            Log.d(TAGdebug, "사진 보내기 성공");

            String imagePath = barcode.getPhotoFile().getAbsolutePath();
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);

            if (bitmap != null) {
                imgCamera.setImageBitmap(bitmap);
            } else {
                Toast.makeText(this, "이미지를 불러올 수 없습니다", Toast.LENGTH_SHORT).show();
            }
            barcode.detectBarcode(imagePath, getApplicationContext(), new Barcode.BarcodeResultListner() {
                @Override
                public void onSuccess(String returnBarcodeValue) {
                    barcodeValue = returnBarcodeValue;
                    Log.d(TAGdebug, "바코드 인식 성공: " + barcodeValue);
                }
                @Override
                public void onFailure(String errMsg) {
                    Log.d(TAGdebug, "바코드 인식 실패: " +  errMsg);
                }
            });


        }
    }

    @Override
    protected void setTopAndBottomBar() {
        super.setTopAndBottomBar();
    }
}
