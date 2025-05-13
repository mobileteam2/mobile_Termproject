package com.example.mobile_termproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mobile_termproject.Barcode.Barcode;

public class MainActivity extends AppCompatActivity {
    Barcode barcode = new Barcode();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        barcode.launchCamera(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        barcode.onRequestPermissionsResult(this,requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == barcode.REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){

            String imagePath = barcode.getPhotoFile().getAbsolutePath();
            barcode.detectBarcode(imagePath, this, new Barcode.BarcodeResultListner() {
                @Override
                public void onSuccess(String barcodeValue) {
                    Log.d("BARCODE", "바코드 인식 성공: " + barcodeValue);
                }

                @Override
                public void onFailure(String errMsg) {
                    Log.d("BARCODE", "바코드 인식 실패: " +  errMsg);
                }
            });
        }
    }
}