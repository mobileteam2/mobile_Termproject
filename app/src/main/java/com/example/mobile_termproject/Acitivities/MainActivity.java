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
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobile_termproject.Data.FoodItem;
import com.example.mobile_termproject.FoodRecyclerAdapter;
import com.example.mobile_termproject.Notification.NotificationListener;
import com.example.mobile_termproject.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {

    private RecyclerView rvFoods;
    private FoodRecyclerAdapter adapter;
    private List<FoodItem> foodList;
    private CollectionReference ingredientsRef;

    private FloatingActionButton btnAddFood;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Firestore 경로 설정
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        ingredientsRef = db.collection("users")
                .document("유저1")
                .collection("식재료 목록 하위컬렉션");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTopAndBottomBar();

        rvFoods = findViewById(R.id.rvFoods);
        rvFoods.setLayoutManager(new LinearLayoutManager(this));

        // 임시 데이터
        foodList = new ArrayList<>();
        foodList.add(new FoodItem("사과", "과일", "2025-06-01"));
        foodList.add(new FoodItem("우유", "유제품", "2025-05-20"));
        foodList.add(new FoodItem("당근", "채소", "2025-06-10"));

        adapter = new FoodRecyclerAdapter(foodList);
        rvFoods.setAdapter(adapter);

        btnAddFood = findViewById(R.id.btnAddFood);
        btnAddFood.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(MainActivity.this, view);
            popupMenu.getMenuInflater().inflate(R.menu.menu_add_options, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.menu_barcode) {
                    startActivity(new Intent(getApplicationContext(), BarcodeAddActivity.class));
                    return true;
                } else if (item.getItemId() == R.id.menu_manual) {
                    startActivity(new Intent(MainActivity.this, ManualAddActivity.class));
                    return true;
                }
                return false;
            });

            popupMenu.show();
        });

        if (!isNotificationPermissionGranted(this)) {
            new AlertDialog.Builder(this)
                    .setTitle("알림 권한 필요")
                    .setMessage("앱에서 기능을 사용하려면 알림 접근 권한이 필요합니다. 설정에서 권한을 켜주세요.")
                    .setPositiveButton("설정으로 이동", (dialog, which) -> {
                        startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
                    })
                    .setNegativeButton("취소", null)
                    .show();
        }
    }

    @Override
    protected void setTopAndBottomBar() {
        super.setTopAndBottomBar();
    }

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
