package com.example.mobile_termproject.Acitivities;

import android.app.AlertDialog;
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

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;

import com.example.mobile_termproject.Data.FoodItem;
import com.example.mobile_termproject.Data.FoodItemAdapter;
import com.example.mobile_termproject.Notification.NotificationListener;
import com.example.mobile_termproject.R;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * MainActivity: Firestore의 실시간 리스너를 걸어두어,
 * 데이터가 변경될 때마다 리스트뷰를 자동 갱신합니다.
 */
public class MainActivity extends BaseActivity {

    private ListView lvFoods;
    private FoodItemAdapter adapter;
    public static List<FoodItem> foodList = new ArrayList<>();
    private ExtendedFloatingActionButton btnAddFood;
    private CollectionReference ingredientsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 1) Firestore 컬렉션 참조 설정
        String uid = user.getUid();
        ingredientsRef = db
                .collection("users")
                .document(uid)
                .collection("ingredients");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTopAndBottomBar();

        // 2) UI 바인딩
        lvFoods = findViewById(R.id.lvFoods);
        btnAddFood = findViewById(R.id.fab_add_item);

        // 3) 어댑터 생성 및 ListView 연결
        adapter = new FoodItemAdapter(
                this,
                R.layout.item_food,
                foodList,
                new FoodItemAdapter.onItemButtonClickListener() {
                    @Override
                    public void onEditButtonClick(int position) {
                        FoodItem item = foodList.get(position);
                        ItemEditActivity dialog =
                                ItemEditActivity.newInstance(item.getId(), position);
                        dialog.setOnItemEditCompleteListener(() -> {
                            adapter.notifyDataSetChanged();
                        });
                        dialog.show(getSupportFragmentManager(), "ItemEditDialog");
                    }
                    @Override
                    public void onDeleteButtonClick(int position) {
                        FoodItem item = foodList.get(position);
                        String docId = item.getId();
                        ingredientsRef
                                .document(docId)
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    foodList.remove(position);
                                    adapter.notifyDataSetChanged();
                                })
                                .addOnFailureListener(e -> {
                                    Log.d(TAGdebug, "삭제 실패: " + e.getMessage());
                                });
                    }
                }
        );
        lvFoods.setAdapter(adapter);

        // 4) 추가 버튼 클릭 시 팝업 메뉴 띄우기
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

        // 5) Firestore 실시간 리스너 등록
        ingredientsRef.addSnapshotListener((QuerySnapshot snapshots, FirebaseFirestoreException error) -> {
            if (error != null) {
                Log.e("MainActivity", "리스너 오류", error);
                return;
            }
            if (snapshots != null) {
                foodList.clear();
                for (QueryDocumentSnapshot doc : snapshots) {
                    String name = doc.getString("name");
                    String category = doc.getString("category");
                    // expiration을 Expiration 객체로 쓰는 경우라면 toObject(FoodItem.class) 사용 가능
                    String expiration = doc.getString("expiration");
                    String imageUrl = doc.getString("imageUrl");
                    String id = doc.getId();

                    foodList.add(new FoodItem(name, category, expiration, id, imageUrl));
                }
                adapter.notifyDataSetChanged();
            }
        });

        // 6) 알림 권한 확인 및 요청
        if (!isNotificationPermissionGranted(this)) {
            new AlertDialog.Builder(this)
                    .setTitle("알림 권한 필요")
                    .setMessage("앱에서 기능을 사용하려면 알림 접근 권한이 필요합니다. 설정에서 권한을 켜주세요.")
                    .setPositiveButton("설정으로 이동", (dialog, which) -> {
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

    // 더 이상 onResume에서 Firestore를 다시 불러올 필요가 없습니다.
    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * 알림 권한이 부여되었는지 확인
     */
    public static boolean isNotificationPermissionGranted(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            ComponentName componentName = new ComponentName(context, NotificationListener.class);
            return notificationManager.isNotificationListenerAccessGranted(componentName);
        } else {
            return NotificationManagerCompat
                    .getEnabledListenerPackages(context)
                    .contains(context.getPackageName());
        }
    }
}
