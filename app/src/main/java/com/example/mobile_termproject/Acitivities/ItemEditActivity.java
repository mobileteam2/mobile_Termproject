package com.example.mobile_termproject.Acitivities;

import static com.example.mobile_termproject.Acitivities.BaseActivity.TAGdebug;
import static com.example.mobile_termproject.Acitivities.BaseActivity.db;
import static com.example.mobile_termproject.Acitivities.BaseActivity.user;
import static com.example.mobile_termproject.Acitivities.MainActivity.foodList;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.example.mobile_termproject.API.IngredientUtils;
import com.example.mobile_termproject.Data.FoodItem;
import com.example.mobile_termproject.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemEditActivity extends BottomSheetDialogFragment {
    private EditText etName, etExpiration;
    private Spinner spinnerCategory;
    private Button btnEdit, btnCacel;
    private ImageButton imgIngredient;

    private CollectionReference ingredientsRef;

    public static ItemEditActivity newInstance(String itemId, int position) {
        ItemEditActivity fragment = new ItemEditActivity();
        Bundle args = new Bundle();
        args.putString("item_id",itemId);
        args.putInt("position",position);
        fragment.setArguments(args);
        return fragment;
    }

    public interface OnItemEditCompleteListener {
        void onItemEdited();
    }

    private OnItemEditCompleteListener callBack;

    public void setOnItemEditCompleteListener(OnItemEditCompleteListener listener){
        this.callBack = listener;
    }


    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.item_edit, container, false);

        // View 바인딩
        etName = view.findViewById(R.id.etName);
        etExpiration = view.findViewById(R.id.etExpiration);
        spinnerCategory = view.findViewById(R.id.spinnerCategory);
        btnEdit = view.findViewById(R.id.btnEdit);
        btnCacel = view.findViewById(R.id.btnCancel);
        imgIngredient = view.findViewById(R.id.imgIngredient);

        String itemId = getArguments() != null ? getArguments().getString("item_id") : null;
        int position = getArguments() != null ? getArguments().getInt("position") : null;
        FoodItem item = foodList.get(position);

        etName.setText(item.getName());
        etExpiration.setText(item.getExpiration());

        String uid = user.getUid();
        ingredientsRef = db.collection("users")
                .document(uid)
                .collection("ingredients");

        CollectionReference categoryRef = db.collection("ingredients");

        categoryRef.get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                List<String> categoryList = new ArrayList<>();
                categoryList.add("카테고리");

                for (DocumentSnapshot doc : task.getResult()){
                    categoryList.add(doc.getId());
                }
                // 카테고리 Spinner 설정
                ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        categoryList
                );

                categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCategory.setAdapter(categoryAdapter);
            }
        });



        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = etName.getText().toString();
                String expiration = etExpiration.getText().toString();
                String category = spinnerCategory.getSelectedItem().toString();
                item.setName(name);
                item.setExpiration(expiration);
                item.setCategory(category);
                DocumentReference docRef = ingredientsRef.document(itemId);
                Map<String, Object> updates = new HashMap<>();
                updates.put("name",name);
                updates.put("category",category);
                updates.put("expiration",expiration);

                docRef.update(updates)
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAGdebug, "문서 업데이트 성공");

                            if(callBack != null){
                                callBack.onItemEdited();
                            }

                            dismiss();
                        })
                        .addOnFailureListener(e -> {
                            Log.d(TAGdebug, "문서 업데이트 실패: " + e.getMessage());
                        });

            }
        });

        btnCacel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        return view;
    }
}
