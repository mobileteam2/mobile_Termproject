package com.example.mobile_termproject.Notification;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.mobile_termproject.Data.Expiration;
import com.example.mobile_termproject.Data.FoodItem;
import com.example.mobile_termproject.R;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import android.widget.ImageView;
import com.bumptech.glide.Glide;

import java.util.HashMap;
import java.util.Map;

public class ConfirmIngredientActivity extends AppCompatActivity {
    private FoodItem foodItem;
    private String selectedStorageType = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_confirm_ingredient);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 전달받은 값 복원
        String name = getIntent().getStringExtra("name");
        String category = getIntent().getStringExtra("category");
        String frozen = getIntent().getStringExtra("frozen");
        String refrigerated = getIntent().getStringExtra("refrigerated");
        String room = getIntent().getStringExtra("room");
        String imageUrl = getIntent().getStringExtra("imageUrl");
        long timestamp = getIntent().getLongExtra("timestamp", 0);
        String imgUrl = getIntent().getStringExtra("imgUrl");

        if (name == null || category == null || frozen == null || refrigerated == null || room == null) {
            Toast.makeText(this, "식재료 정보 전달 실패", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // FoodItem 객체 재구성
        foodItem = new FoodItem();
        foodItem.setName(name);
        foodItem.setCategory(category);
        foodItem.setExpirationc(new Expiration(frozen, refrigerated, room));
        foodItem.setTimestamp(timestamp);
        foodItem.setImageUrl(imgUrl);

        // 레이아웃 요소 연결
        TextView nameText = findViewById(R.id.ingredientNameValue);
        TextView categoryText = findViewById(R.id.categoryValue);
        TextView roomDate = findViewById(R.id.textRoomDate);
        TextView refrigeratedDate = findViewById(R.id.textRefrigeratedDate);
        TextView frozenDate = findViewById(R.id.textFrozenDate);
        ImageView imageThumbnail = findViewById(R.id.imageThumbnail);

        // 이미지 URL이 유효한 경우에만 로드
        if (imgUrl != null && !imgUrl.isEmpty()) {
            Glide.with(this)
                    .load(imgUrl)
                    .placeholder(R.drawable.loading) // 로딩 중 표시할 기본 이미지
                    .error(R.drawable.error)       // 실패 시 표시할 이미지
                    .into(imageThumbnail);
        } else {
            imageThumbnail.setImageResource(R.drawable.error);
        }

        MaterialCardView cardRoom = findViewById(R.id.cardRoom);
        MaterialCardView cardRefrigerated = findViewById(R.id.cardRefrigerated);
        MaterialCardView cardFrozen = findViewById(R.id.cardFrozen);
        Button btnSave = findViewById(R.id.btnSave);
        Button btnCancel = findViewById(R.id.btnCancel);

        ImageView thumnail = findViewById(R.id.imageThumbnail);

        // 값 표시
        nameText.setText(name);
        categoryText.setText(category);
        roomDate.setText(room);
        refrigeratedDate.setText(refrigerated);
        frozenDate.setText(frozen);

        if(imageUrl != null && !imageUrl.trim().isEmpty()){
            Glide.with(ConfirmIngredientActivity.this)
                    .load(imageUrl)
                    .placeholder(R.drawable.edit)
                    .error(R.drawable.delete)
                    .into(thumnail);
        }

        // 카드 클릭 시 강조 처리
        cardRoom.setOnClickListener(v -> {
            highlightSelectedCard(cardRoom, cardRefrigerated, cardFrozen);
            selectedStorageType = "실온";
        });

        cardRefrigerated.setOnClickListener(v -> {
            highlightSelectedCard(cardRefrigerated, cardRoom, cardFrozen);
            selectedStorageType = "냉장";
        });

        cardFrozen.setOnClickListener(v -> {
            highlightSelectedCard(cardFrozen, cardRoom, cardRefrigerated);
            selectedStorageType = "냉동";
        });

        // 저장 버튼 클릭 시
        btnSave.setOnClickListener(v -> {
            if (selectedStorageType == null) {
                Toast.makeText(this, "보관 방식을 선택해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            String expirationValue;
            switch (selectedStorageType) {
                case "실온":
                    expirationValue = room;
                    break;
                case "냉장":
                    expirationValue = refrigerated;
                    break;
                case "냉동":
                    expirationValue = frozen;
                    break;
                default:
                    expirationValue = "";
            }

            foodItem.setExpiration(expirationValue);
            foodItem.setStoragetype(selectedStorageType);
            foodItem.setImageUrl(imageUrl);

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            db.collection("users").document(uid).collection("ingredients").add(foodItem)
                    .addOnSuccessListener(doc -> {
                        Toast.makeText(this, "저장 성공", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "저장 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });

            // 가격 추이 추적 리스트에 등록
            addToPriceWatchList(foodItem.getName());
        });

        btnCancel.setOnClickListener(v -> {
            Toast.makeText(this, "저장 취소", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    // 선택된 카드 강조하고 나머지는 기본 상태로 되돌리는 함수
    private void highlightSelectedCard(MaterialCardView selected, MaterialCardView... others) {
        selected.setStrokeWidth(6);
        selected.setStrokeColor(getColor(R.color.md_theme_light_secondary));
        for (MaterialCardView card : others) {
            card.setStrokeWidth(1);
            card.setStrokeColor(getColor(R.color.md_theme_light_primary));
        }
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
