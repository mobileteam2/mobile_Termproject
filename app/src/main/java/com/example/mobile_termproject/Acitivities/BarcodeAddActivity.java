package com.example.mobile_termproject.Acitivities;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.mobile_termproject.API.Barcode;
import com.example.mobile_termproject.API.NaverAPI;
import com.example.mobile_termproject.Data.Expiration;
import com.example.mobile_termproject.Data.FoodItem;
import com.example.mobile_termproject.Data.NaverReturnResult;
import com.example.mobile_termproject.Notification.ConfirmIngredientActivity;
import com.example.mobile_termproject.Notification.ExpirationCalculator;
import com.example.mobile_termproject.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
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
    private String frozen, refrigerated, room;
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
                barcode.launchCamera(BarcodeAddActivity.this);
                btnCamera.setText(R.string.add_barcode_activity_resend_btn);
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(() -> {
                    foodName = barcode.getInfo(barcodeValue);
                    Log.d(TAGdebug, "FOOD NAME: \n" + foodName);

                    if(foodName == null){
                        Toast.makeText(BarcodeAddActivity.this, "서버 응답 오류", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Log.d(TAGdebug, "네이버 API 호출");
                    api.getInfoNaver(foodName, new NaverAPI.NaverCallback() {
                        @Override
                        public void onSuccess(NaverReturnResult result) {
                            long timestamp = System.currentTimeMillis();
                            String category = result.getFinalCategory();
                            String imageUrl = result.getImageUrl();

                            FoodItem foodItem = new FoodItem();
                            foodItem.setName(foodName);
                            foodItem.setCategory(category);
                            foodItem.setImageUrl(imageUrl);

                            Log.d(TAGdebug, "imageUrl : " + foodItem.getImageUrl());
                            Log.d(TAGdebug, "category : " + result.toString());


                            ExpirationCalculator.calculateExpirationDates(category, timestamp, r -> {
                                frozen = r.get("냉동");
                                refrigerated = r.get("냉장");
                                room = r.get("실온");
                                // FoodItem 임시 생성
                                foodItem.setExpirationc(new Expiration(
                                        frozen,
                                        refrigerated,
                                        room
                                ));
                                foodItem.setTimestamp(timestamp);

                                // 사용자에게 앱 알림 발송 (FoodItem 객체를 intent로 전달)
                                sendUserNotification(foodItem);
                                Log.d(TAGdebug, "앱 알림 발송 : " + frozen + " / " + refrigerated + " / " + room);

                                Intent intent = new Intent(BarcodeAddActivity.this, ConfirmIngredientActivity.class);
                                intent.putExtra("name", foodItem.getName());
                                intent.putExtra("category", foodItem.getCategory());
                                intent.putExtra("frozen", frozen);
                                intent.putExtra("refrigerated", refrigerated);
                                intent.putExtra("room", room);
                                intent.putExtra("timestamp", foodItem.getTimestamp());
                                intent.putExtra("imageUrl", foodItem.getImageUrl());

                                startActivity(intent);
                            });

                            finish();
                            // 가격 추이 추적 리스트에 등록
                            addToPriceWatchList(foodItem.getName());
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Log.d(TAGdebug, "NAVER API 실패: " + e.getMessage());
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
            File latestPhotoFile = barcode.getPhotoFile();
            String imagePath = null;
            Bitmap bitmap = null;
            if(latestPhotoFile != null){
                imagePath = latestPhotoFile.getAbsolutePath();
                bitmap = BitmapFactory.decodeFile(imagePath);
            }
            Log.d(TAGdebug, "이미지 경로: "+imagePath);
            Log.d(TAGdebug, "파일 존재 여부: " +latestPhotoFile.exists());
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

    public void sendUserNotification(FoodItem item) {
        Log.d(TAGdebug, "발송 전 확인 : " + item.getExpirationc().getRoom() + " / " + item.getExpirationc().getRefrigerated() + " / " + item.getExpirationc().getFrozen());

        Intent intent = new Intent(this, ConfirmIngredientActivity.class);
        intent.putExtra("name", item.getName());
        intent.putExtra("category", item.getCategory());
        intent.putExtra("frozen", item.getExpirationc().getFrozen());
        intent.putExtra("refrigerated", item.getExpirationc().getRefrigerated());
        intent.putExtra("room", item.getExpirationc().getRoom());
        intent.putExtra("timestamp", item.getTimestamp());

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "confirm_channel")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
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

    private void addToPriceWatchList(String foodName) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String safeName = foodName.replaceAll("[.#$/\\[\\]]", "_");

        Map<String, Object> data = new HashMap<>();
        data.put("name", foodName);
        data.put("lastChecked", null); // 최초 등록이므로 null

        db.collection("price_watch_list")
                .document(safeName)
                .set(data)
                .addOnSuccessListener(unused -> Log.d("PriceWatchList", "추가됨: " + foodName))
                .addOnFailureListener(e -> Log.e("PriceWatchList", "추가 실패", e));
    }
}
