package com.example.mobile_termproject.Acitivities;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobile_termproject.API.NaverAPI;
import com.example.mobile_termproject.Data.FoodItem;
import com.example.mobile_termproject.Data.PriceCheckAdapter;
import com.example.mobile_termproject.Data.NaverReturnResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import android.util.Log;

import com.example.mobile_termproject.R;

public class PriceCheckActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private PriceCheckAdapter adapter;
    private List<FoodItem> foodItems = new ArrayList<>();  // 추후 Firestore로부터 불러올 예정

    private Map<String, NaverReturnResult> naverResults = new HashMap<>();
    private NaverAPI naverAPI = new NaverAPI();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_price_check);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setTopAndBottomBar();
        bottomNavigationView.setSelectedItemId(R.id.bottomBar_price_check);

        recyclerView = findViewById(R.id.recyclerViewPriceCheck);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setPadding(16, 0, 16, 16);
        recyclerView.setClipToPadding(false);

        adapter = new PriceCheckAdapter(foodItems, naverResults);
        recyclerView.setAdapter(adapter);

        Log.d("DEBUG", "PriceCheckActivity 실행됨");

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Set<String> seenNames = new HashSet<>();
        db.collection("users")
                .document(uid)
                .collection("ingredients")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        FoodItem item = doc.toObject(FoodItem.class);
                        if (!seenNames.contains(item.getName())) {
                            seenNames.add(item.getName());
                            foodItems.add(item);
                        }
                    }
                    adapter.notifyDataSetChanged();

                    for (FoodItem item : foodItems) {
                        String foodName = item.getName();
                        naverAPI.getInfoNaver(foodName, new NaverAPI.NaverCallback() {
                            @Override
                            public void onSuccess(NaverReturnResult result) {
                                naverResults.put(foodName, result);
                                runOnUiThread(() -> adapter.notifyDataSetChanged());
                            }

                            @Override
                            public void onFailure(Exception e) {
                                naverResults.put(foodName, null);
                            }
                        });
                    }
                    Log.d("DEBUG", "불러온 식재료 수: " + foodItems.size());
                })
                .addOnFailureListener(e -> e.printStackTrace());
    }
}