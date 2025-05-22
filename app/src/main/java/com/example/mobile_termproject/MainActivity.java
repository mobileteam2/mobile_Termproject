package com.example.mobile_termproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ListView lvFoods;
    private FoodItemAdapter adapter;
    private List<FoodItem> foodList;

    private Button btnFridge, btnCommunity, btnRecipe, btnOther;
    private Button btnAddFood; // 추가 버튼

    private CollectionReference ingredientsRef;
    private Toolbar toolbar;
    private BottomNavigationView bottomNavigationView;

    private String TAGBebbug = "DEBUG";

    /**
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_menu_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

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
                    Intent intent = new Intent(MainActivity.this, BarcodeAddActivity.class);
                    startActivity(intent);
                    return true;
                } else if (item.getItemId() == R.id.menu_manual) {
                    Intent intent = new Intent(MainActivity.this, ManualAddActivity.class);
                    startActivity(intent);
                    return true;
                }
                return false;
            });

            popupMenu.show();
        });

        // Firestore에서 데이터 불러오기 (선택 사항)
        loadIngredientsFromFirestore();
    }

    private void setTopAndBottomBar(){
        toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);

        bottomNavigationView = findViewById(R.id.bottomAppBar);

        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int menuId = item.getItemId();
                if(menuId == R.id.bottomBar_myRefri){
                    // TODO: 내 냉장고 눌렀을 때 처리
                    Log.d(TAGBebbug, "냉장고 클릭");
                } else if(menuId == R.id.bottomBar_community){
                    // TODO: 커뮤니티 눌렀을 때 처리
                    Log.d(TAGBebbug, "커뮤니티 클릭");
                } else if(menuId == R.id.bottomBar_recipe){
                    // TODO: 레시피 추천 눌렀을 때 처리
                    Log.d(TAGBebbug, "레시피 클릭");
                } else if(menuId == R.id.bottomBar_others){
                    // TODO: 기타 눌렀을 때 처리
                    Log.d(TAGBebbug, "기타 클릭");
                } else {
                    Log.d(TAGBebbug, "그 외 경우");
                }
                return false;
            }
        });
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
}
