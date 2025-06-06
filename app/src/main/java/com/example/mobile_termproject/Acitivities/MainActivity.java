package com.example.mobile_termproject.Acitivities;

import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.ListView;
import android.widget.PopupMenu;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationManagerCompat;

import com.example.mobile_termproject.Data.FoodItem;
import com.example.mobile_termproject.Data.FoodItemAdapter;
import com.example.mobile_termproject.Notification.NotificationListener;
import com.example.mobile_termproject.R;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {

    private ListView lvFoods;
    private FoodItemAdapter adapter;
    public static List<FoodItem> foodList = new ArrayList<>();
    private ExtendedFloatingActionButton btnAddFood; // 추가 버튼
    private CollectionReference ingredientsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String uid = user.getUid();
        ingredientsRef = db.collection("users")
                .document(uid)
                .collection("ingredients");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTopAndBottomBar();

        lvFoods = findViewById(R.id.lvFoods);

        // 2) 어댑터 생성 및 연결
        adapter = new FoodItemAdapter(this, R.layout.item_food, foodList, new FoodItemAdapter.onItemButtonClickListener() {
            @Override
            public void onEditButtonClick(int position) {
                FoodItem item = foodList.get(position);
                ItemEditActivity dialog = ItemEditActivity.newInstance(item.getId(), position);
                dialog.setOnItemEditCompleteListener(() -> {
                    adapter.notifyDataSetChanged();
                });
                dialog.show(getSupportFragmentManager(), "ItemEditDialog");
            }
            @Override
            public void onDeleteButtonClick(int position) {
                FoodItem item = foodList.get(position);
                String docId = item.getId();
                ingredientsRef.document(docId)
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            foodList.remove(position);
                            adapter.notifyDataSetChanged();
                        })
                        .addOnFailureListener(e -> {
                            Log.d(TAGdebug, "삭제 실패: " + e.getMessage());
                        });
            }
        });

        lvFoods.setAdapter(adapter);

        btnAddFood = findViewById(R.id.fab_add_item); // 추가 버튼도 바인딩

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
        loadIngredientsFromFirestore();

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
            if(!foodList.isEmpty()){
                foodList.clear();
            }
            for (var doc : querySnapshot) {
                String name = doc.getString("name");
                String category = doc.getString("category");
                String expiration = doc.getString("expiration");  // 예시: 기한 필드명
                String imageUrl = doc.getString("imageUrl");
                String id = doc.getId();
                foodList.add(new FoodItem(name, category, expiration, id, imageUrl));
            }
            adapter.notifyDataSetChanged();
        }).addOnFailureListener(e -> {
            Log.e("Firestore", "불러오기 실패", e);
            Log.d(TAGdebug, "로드 실패: " + e.getMessage());
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

    @Override
    protected void onResume() {
        super.onResume();
        loadIngredientsFromFirestore();
    }
}
