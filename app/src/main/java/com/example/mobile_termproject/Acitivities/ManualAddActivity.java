package com.example.mobile_termproject.Acitivities;

import static com.example.mobile_termproject.Acitivities.BaseActivity.db;
import static com.example.mobile_termproject.Acitivities.BaseActivity.user;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
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
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.mobile_termproject.Data.Expiration;
import com.example.mobile_termproject.Data.FoodItem;
import com.example.mobile_termproject.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.CollectionReference;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * ManualAddActivity: BottomSheetDialogFragment로 수동으로 식재료 추가 화면 구현
 * - 카메라 호출과 촬영 결과를 ImageButton(imgIngredient)에 표시하도록 직접 구현하였습니다.
 * - FoodItem 객체에는 “file://” URI 형태의 imageUrl을 저장합니다.
 */
public class ManualAddActivity extends BottomSheetDialogFragment {
    private static final int REQUEST_IMAGE_CAPTURE = 1;       // 카메라 인텐트 요청 코드
    private static final int REQUEST_CAMERA_PERMISSION = 100; // 카메라 권한 요청 코드

    private EditText etName, etExpiration;
    private Spinner spinnerCategory;
    private Button btnAdd;
    private ImageButton imgIngredient;

    // 촬영한 사진을 저장할 임시 File
    private File photoFile;

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

        // 1) 뷰 바인딩
        etName         = view.findViewById(R.id.etName);
        etExpiration   = view.findViewById(R.id.etExpiration);
        spinnerCategory = view.findViewById(R.id.spinnerCategory);
        btnAdd         = view.findViewById(R.id.btnAdd);
        imgIngredient  = view.findViewById(R.id.imgIngredient);

        // 2) Firestore 컬렉션 참조 (현재 로그인된 유저 기준)
        String uid = user.getUid();
        ingredientsRef = db
                .collection("users")
                .document(uid)
                .collection("ingredients");

        // 3) 카테고리 Spinner 설정
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                new String[]{"카테고리", "육류", "유제품", "과일", "채소"}
        );
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        // 4) imgIngredient 클릭 시 카메라 실행
        imgIngredient.setOnClickListener(v -> {
            // 4-1) 먼저 카메라 권한이 있는지 확인
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED) {
                // 권한 요청
                requestPermissions(
                        new String[]{Manifest.permission.CAMERA},
                        REQUEST_CAMERA_PERMISSION
                );
            } else {
                // 이미 권한이 있으면 카메라 실행
                dispatchTakePictureIntent();
            }
        });

        // 5) “추가” 버튼 클릭 시 Firestore 저장
        btnAdd.setOnClickListener(v -> {
            String name            = etName.getText().toString().trim();
            String expirationInput = etExpiration.getText().toString().trim();
            String category        = spinnerCategory.getSelectedItem().toString();

            // 필수 입력 확인
            if (TextUtils.isEmpty(name)
                    || TextUtils.isEmpty(expirationInput)
                    || category.equals("카테고리")) {
                Toast.makeText(getContext(), "모든 필드를 정확히 입력하세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            // FoodItem 객체 생성
            FoodItem foodItem = new FoodItem();
            foodItem.setName(name);
            foodItem.setCategory(category);

            Expiration expObj = new Expiration(
                    expirationInput,   // frozen
                    expirationInput,   // refrigerated
                    expirationInput    // room
            );
            foodItem.setExpirationc(expObj);

            // photoFile이 null 아니면 “file://” URI 형태로 imageUrl 저장
            if (photoFile != null && photoFile.exists()) {
                String fileUriString = "file://" + photoFile.getAbsolutePath();
                foodItem.setImageUrl(fileUriString);
            }

            long now = System.currentTimeMillis();
            foodItem.setTimestamp(now);
            foodItem.setStoragetype("manual");

            // Firestore에 업로드
            ingredientsRef
                    .add(foodItem)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(getContext(), "식재료가 추가되었습니다!", Toast.LENGTH_SHORT).show();
                        dismiss();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "추가 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        return view;
    }

    // ————————— 카메라 인텐트 실행 메서드 —————————
    private void dispatchTakePictureIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // 결과를 받을 Receiver가 Fragment여야 onActivityResult가 호출됨
        if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
            try {
                // 임시 파일 생성
                photoFile = createImageFile();
            } catch (IOException e) {
                Toast.makeText(getContext(), "파일 생성 실패", Toast.LENGTH_SHORT).show();
                return;
            }
            // FileProvider를 통해 URI 생성 (authority는 반드시 "${applicationId}.fileprovider")
            Uri photoURI = FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getPackageName() + ".fileprovider",
                    photoFile
            );
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            // Fragment에서 호출해야 onActivityResult로 돌아옵니다.
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(getContext(), "카메라를 실행할 수 없습니다", Toast.LENGTH_SHORT).show();
        }
    }

    // ————————— 파일 생성 메서드 —————————
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "IMG_" + timeStamp;
        // 앱 내부 저장소(getFilesDir())에 저장
        File storageDir = requireContext().getFilesDir();
        return File.createTempFile(fileName, ".jpg", storageDir);
    }

    // ————————— 권한 요청 결과 처리 —————————
    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한이 허용되었으면 카메라 실행
                dispatchTakePictureIntent();
            } else {
                Toast.makeText(getContext(), "카메라 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ————————— 카메라 촬영 결과 받는 콜백 —————————
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == getActivity().RESULT_OK) {
            if (photoFile != null && photoFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                if (bitmap != null) {
                    imgIngredient.setImageBitmap(bitmap);
                } else {
                    Toast.makeText(getContext(), "이미지를 불러올 수 없습니다", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "촬영된 사진을 찾을 수 없습니다", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
