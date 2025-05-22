package com.example.mobile_termproject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.mobile_termproject.Data.FoodItem;

import java.util.List;

public class FoodItemAdapter extends ArrayAdapter<FoodItem> {
    private int resourceLayout;
    private Context mContext;

    public FoodItemAdapter(Context context, int resource, List<FoodItem> items) {
        super(context, resource, items);
        this.mContext = context;
        this.resourceLayout = resource;
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

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            v = LayoutInflater.from(mContext).inflate(resourceLayout, parent, false);
        }

        FoodItem item = getItem(position);

        // 🔽 TextView 바인딩 추가
        ImageView tvImage = v.findViewById(R.id.imgIngredient);  // 이미지
        TextView tvName = v.findViewById(R.id.tvFoodName);       // 이름
        TextView tvCategory = v.findViewById(R.id.tvCategory);   // 카테고리
        TextView tvExpiration = v.findViewById(R.id.tvExpirationDate); // 유통기한

        Button btnEdit  = v.findViewById(R.id.btnEdit);
        Button btnDelete= v.findViewById(R.id.btnDelete);

        // 🔽 실제 값 적용
        tvName.setText(item.getName());
        tvCategory.setText("카테고리: " + item.getCategory());
        tvExpiration.setText("유통기한: " + item.getExpiration());

        // 🔽 예시 버튼 동작
        btnEdit.setOnClickListener(view ->
                Toast.makeText(mContext, item.getName() + " 수정 클릭", Toast.LENGTH_SHORT).show()
        );
        btnDelete.setOnClickListener(view ->
                Toast.makeText(mContext, item.getName() + " 삭제 클릭", Toast.LENGTH_SHORT).show()
        );

        return v;
    }
}
