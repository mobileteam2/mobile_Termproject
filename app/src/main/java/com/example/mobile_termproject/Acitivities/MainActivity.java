package com.example.mobile_termproject.Acitivities;

import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationManagerCompat;

import com.example.mobile_termproject.Data.FoodItem;
import com.example.mobile_termproject.FoodItemAdapter;
import com.example.mobile_termproject.Notification.NotificationListener;
import com.example.mobile_termproject.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {

    private ListView lvFoods;
    private FoodItemAdapter adapter;
    private List<FoodItem> foodList;
    private Button btnAddFood; // 추가 버튼
    private CollectionReference ingredientsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // ★ Firestore 경로 설정 (하드코딩된 UID 사용)
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        ingredientsRef = db.collection("users")
                .document("유저1")
                .collection("식재료 목록 하위컬렉션");

        Log.d("Firestore", "ingredientsRef 연결됨: " + ingredientsRef.getPath());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTopAndBottomBar();

        lvFoods = findViewById(R.id.lvFoods);

        // 1) 테스트용 임시 데이터
        foodList = new ArrayList<>();
        foodList.add(new FoodItem("사과", "과일", "2025-06-01"));
        foodList.add(new FoodItem("우유", "유제품", "2025-05-20"));
        foodList.add(new FoodItem("당근", "채소", "2025-06-10"));

        // 2) 어댑터 생성 및 연결
        adapter = new FoodItemAdapter(this, R.layout.item_food, foodList);
        lvFoods.setAdapter(adapter);

        btnAddFood   = findViewById(R.id.btnAddFood); // 추가 버튼도 바인딩

        // ★ 추가 버튼 클릭 시 팝업 메뉴 띄우기
        btnAddFood.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(MainActivity.this, view);
            popupMenu.getMenuInflater().inflate(R.menu.menu_add_options, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.menu_barcode) {
                    Intent intent = new Intent(getApplicationContext(), BarcodeAddActivity.class);
                    startActivity(intent);
                    return true;
                } else if (item.getItemId() == R.id.menu_manual) {
                    ManualAddActivity dialog = ManualAddActivity.newInstance();
                    dialog.show(getSupportFragmentManager(), "ManualAddDialog");
                    return true;
                }
                return false;
            });

            popupMenu.show();
        });

        // Firestore에서 데이터 불러오기 (선택 사항)
        //loadIngredientsFromFirestore();

        // 알림 권한 확인 및 요청
        if (!isNotificationPermissionGranted(this)) {
            new AlertDialog.Builder(this)
                    .setTitle("알림 권한 필요")
                    .setMessage("앱에서 기능을 사용하려면 알림 접근 권한이 필요합니다. 설정에서 권한을 켜주세요.")
                    .setPositiveButton("설정으로 이동", ( dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
                        startActivity(intent);
                    })
                    .setNegativeButton("취소", null)
                    .show();
        }

    }

    @Override
    protected void setTopAndBottomBar() {
        super.setTopAndBottomBar();
    }

    private void loadIngredientsFromFirestore() {
        ingredientsRef.get().addOnSuccessListener(querySnapshot -> {
            foodList.clear();
            for (var doc : querySnapshot) {
                String name = doc.getString("제품명");
                String category = doc.getString("카테고리");
                String expiration = doc.getString("기한");  // 예시: 기한 필드명

                foodList.add(new FoodItem(name, category, expiration));
            }
            adapter.notifyDataSetChanged();
        }).addOnFailureListener(e -> {
            Log.e("Firestore", "불러오기 실패", e);
        });
    }

    // 알림 권한이 부여되었는지 확인
    public static boolean isNotificationPermissionGranted(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            ComponentName componentName = new ComponentName(context, NotificationListener.class);
            return notificationManager.isNotificationListenerAccessGranted(componentName);
        } else {
            return NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.getPackageName());
        }
    }
}
