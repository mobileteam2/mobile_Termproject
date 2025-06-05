package com.example.mobile_termproject.Acitivities;
import static androidx.core.app.ActivityCompat.startActivityForResult;
import static androidx.core.content.ContentProviderCompat.requireContext;
import static androidx.test.InstrumentationRegistry.getContext;

import android.content.Intent;
import android.net.Uri;
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
import com.example.mobile_termproject.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ManualAddActivity extends BottomSheetDialogFragment {
    private EditText etName, etExpiration;
    private Spinner spinnerCategory;
    private Button btnAdd;
    private ImageButton imgIngredient;

    private Uri photoUri; // 촬영된 이미지 URI
    private static final int REQUEST_IMAGE_CAPTURE = 1; // 카메라 요청 코드

    private CollectionReference ingredientsRef;

    public static ManualAddActivity newInstance() {
        return new ManualAddActivity();
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.manual_add, container, false);

        // View 바인딩
        etName = view.findViewById(R.id.etName);
        etExpiration = view.findViewById(R.id.etExpiration);
        spinnerCategory = view.findViewById(R.id.spinnerCategory);
        btnAdd = view.findViewById(R.id.btnAdd);
        imgIngredient = view.findViewById(R.id.imgIngredient);




        // Firestore 참조 (유저1 하드코딩)
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        ingredientsRef = db.collection("users")
                .document("유저1")
                .collection("식재료 목록 하위컬렉션");




        // 카테고리 Spinner 설정
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                new String[]{"카테고리", "육류", "유제품", "과일", "채소"}
        );
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);






        // 이미지버튼 클릭 시 카메라 실행 >>> 내부저장할지 아닐지 정해야함.
        imgIngredient.setOnClickListener(v -> {

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
                File photoFile;
                try {
                    photoFile = createImageFile();
                } catch (IOException e) {
                    Toast.makeText(getContext(), "파일 생성 실패", Toast.LENGTH_SHORT).show();
                    return;
                }
                photoUri = FileProvider.getUriForFile(
                        requireContext(),
                        requireContext().getPackageName() + ".provider",
                        photoFile
                );
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
            }
        });





        btnAdd.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String expiration = etExpiration.getText().toString().trim();
            String category = spinnerCategory.getSelectedItem().toString();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(expiration) || category.equals("카테고리")) {
                Toast.makeText(getContext(), "모든 필드를 정확히 입력하세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            String imageUriStr = (photoUri != null) ? photoUri.toString() : null;

            IngredientUtils.addIngredient(
                    ingredientsRef,
                    name,
                    category,
                    expiration,
                    photoUri != null ? photoUri.toString() : null,
                    () -> {
                        Toast.makeText(getContext(), "식재료가 추가되었습니다!", Toast.LENGTH_SHORT).show();
                        dismiss();
                    },
                    e -> Toast.makeText(getContext(), "추가 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show()
            );
        });

        return view;
    }

    // 파일 저장용 임시 이미지 파일 생성
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "IMG_" + timeStamp;
        File storageDir = requireContext().getFilesDir(); // 내부 저장소 경로
        return new File(storageDir, fileName + ".jpg");
    }

    // 사진 촬영 후 호출되는 메서드 (이미지 버튼에 이미지 표시)
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == getActivity().RESULT_OK) {
            imgIngredient.setImageURI(photoUri);
        }
    }
}
