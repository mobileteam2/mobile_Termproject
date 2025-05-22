package com.example.mobile_termproject.API;

import android.util.Log;
import android.widget.ArrayAdapter;

import androidx.annotation.Nullable;

import com.example.mobile_termproject.Data.FoodItem;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IngredientUtils {

    // ✅ 실시간 리스너 (수정 완료 - docId 포함)
    public static ListenerRegistration listenToIngredients(
            CollectionReference ref,
            List<FoodItem> list,
            ArrayAdapter<FoodItem> adapter
    ) {
        return ref.addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                Log.e("IngredientUtils", "실시간 업데이트 실패", e);
                return;
            }

            list.clear();
            for (DocumentSnapshot doc : snapshots) {
                String name = doc.getString("제품명");
                String category = doc.getString("카테고리");
                String expiration = doc.getString("기한");
                String docId = doc.getId(); // ✅ 문서 ID 저장

                list.add(new FoodItem(name, category, expiration, docId)); // ✅ docId 포함한 생성자 사용
            }
            adapter.notifyDataSetChanged();
        });
    }

    // ✅ 일회성 전체 불러오기
    public static void loadAllIngredients(
            CollectionReference ref,
            List<FoodItem> list,
            ArrayAdapter<FoodItem> adapter
    ) {
        ref.get()
                .addOnSuccessListener(snapshot -> {
                    list.clear();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        String name = doc.getString("제품명");
                        String category = doc.getString("카테고리");
                        String expiration = doc.getString("기한");
                        String docId = doc.getId(); // ✅ 문서 ID 저장

                        list.add(new FoodItem(name, category, expiration, docId)); // ✅ docId 포함한 생성자 사용
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e("IngredientUtils", "불러오기 실패", e));
    }

    // ✅ 삭제
    public static void deleteIngredient(
            CollectionReference ref,
            String docId,
            Runnable onSuccess,
            Runnable onFailure
    ) {
        ref.document(docId).delete()
                .addOnSuccessListener(unused -> onSuccess.run())
                .addOnFailureListener(e -> onFailure.run());
    }

    // ✅ Firestore 실패 콜백 인터페이스
    public interface OnFirestoreError {
        void onError(Exception e);
    }

    // ✅ 문서 ID 자동 생성 방식 저장
    public static void addIngredient(
            CollectionReference ref,
            String name,
            String category,
            String expiration,
            @Nullable String imageUri,
            Runnable onSuccess,
            OnFirestoreError onFailure
    ) {
        ref.get().addOnSuccessListener(snapshot -> {
            int nextId = snapshot.size() + 1;
            String docId = "식재료" + nextId;

            Map<String, Object> data = new HashMap<>();
            data.put("제품명", name);
            data.put("카테고리", category);
            data.put("기한", expiration);
            if (imageUri != null) {
                data.put("이미지", imageUri);
            }

            ref.document(docId).set(data)
                    .addOnSuccessListener(unused -> onSuccess.run())
                    .addOnFailureListener(e -> onFailure.onError(e));
        }).addOnFailureListener(e -> onFailure.onError(e));
    }
}
