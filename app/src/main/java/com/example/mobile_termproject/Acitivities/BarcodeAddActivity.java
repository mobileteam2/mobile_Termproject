package com.example.mobile_termproject.Acitivities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.mobile_termproject.API.Barcode;
import com.example.mobile_termproject.R;


public class BarcodeAddActivity extends BaseActivity {
    Barcode barcode = new Barcode();
    Button btnCamera, btnSend;
    ImageView imgCamera;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_barcode);
        setTopAndBottomBar();

        imgCamera = findViewById(R.id.imgCamera);
        btnCamera = findViewById(R.id.btnCamera);
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                barcode.launchCamera(BarcodeAddActivity.this, Boolean.TRUE);
                btnCamera.setText("다시 찍기");
            }
        });


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == barcode.REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){
            Log.d(TAGdebug, "사진 보내기 성공");

            String imagePath = barcode.getPhotoFile().getAbsolutePath();
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);

            if (bitmap != null) {
                imgCamera.setImageBitmap(bitmap);
            } else {
                Toast.makeText(this, "이미지를 불러올 수 없습니다", Toast.LENGTH_SHORT).show();
            }
            barcode.detectBarcode(imagePath, getApplicationContext(), new Barcode.BarcodeResultListner() {
                @Override
                public void onSuccess(String barcodeValue) {
                    Log.d(TAGdebug, "바코드 인식 성공: " + barcodeValue);
                    String result = barcode.getInfo(barcodeValue);
                    Log.d(TAGdebug, "Naver API RESULT: \n" + result);
                }
                @Override
                public void onFailure(String errMsg) {
                    Log.d(TAGdebug, "바코드 인식 실패: " +  errMsg);
                }
            });


        }
    }

    @Override
    protected void setTopAndBottomBar() {
        super.setTopAndBottomBar();
    }
}
