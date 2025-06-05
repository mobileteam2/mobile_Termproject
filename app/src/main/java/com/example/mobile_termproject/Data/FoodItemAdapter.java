package com.example.mobile_termproject.Data;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.mobile_termproject.Acitivities.BarcodeAddActivity;
import com.example.mobile_termproject.Acitivities.ManualAddActivity;
import com.example.mobile_termproject.R;

import java.util.List;

public class FoodItemAdapter extends ArrayAdapter<FoodItem> {
    private int resourceLayout;
    private Context mContext;
    public ImageButton btnEdit, btnDelete;
    public ImageView tvImage;
    public TextView tvName, tvCategory, tvExpiration;
    private onItemEditButtonClickListener listener;
    public interface onItemEditButtonClickListener {
        void onEditButtonClick(int position);
    }

    public FoodItemAdapter(Context context, int resource, List<FoodItem> items, onItemEditButtonClickListener listener) {
        super(context, resource, items);
        this.mContext = context;
        this.resourceLayout = resource;
        this.listener = listener;
    }



    @Override
    public int getCount() {
        return super.getCount();
    }

    @Override
    public int getPosition(@Nullable FoodItem item) {
        return super.getPosition(item);
    }

    @Nullable
    @Override
    public FoodItem getItem(int position) {
        return super.getItem(position);
    }

    public void setInfo(
            String name,
            String category,
            String expiration
    ){
        tvName.setText(name);
        tvCategory.setText(category);
        tvExpiration.setText(expiration);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            v = LayoutInflater.from(mContext).inflate(resourceLayout, parent, false);
        }

        FoodItem item = getItem(position);

        // 🔽 TextView 바인딩 추가
        tvImage = v.findViewById(R.id.imgIngredient);  // 이미지
        tvName = v.findViewById(R.id.tvFoodName);       // 이름
        tvCategory = v.findViewById(R.id.tvCategory);   // 카테고리
        tvExpiration = v.findViewById(R.id.tvExpirationDate); // 유통기한

        btnEdit = v.findViewById(R.id.btnEdit);
        btnDelete= v.findViewById(R.id.btnDelete);

        // 🔽 실제 값 적용
        /*
        tvName.setText(item.getName());
        tvCategory.setText(item.getCategory());
        tvExpiration.setText(item.getExpiration() + "까지");
        */
        setInfo(
                item.getName(),
                item.getCategory(),
                item.getExpiration() + "까지"
        );

        // 🔽 예시 버튼 동작
        btnEdit.setOnClickListener(view -> {
            if(listener != null){
                listener.onEditButtonClick(position);
                }
            }

        );
        btnDelete.setOnClickListener(view ->
                Toast.makeText(mContext, item.getName() + " 삭제 클릭", Toast.LENGTH_SHORT).show()
        );

        return v;
    }
}
