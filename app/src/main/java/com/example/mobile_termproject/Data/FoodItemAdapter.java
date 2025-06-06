package com.example.mobile_termproject.Data;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.mobile_termproject.R;

import java.util.List;

public class FoodItemAdapter extends ArrayAdapter<FoodItem> {
    private final int resourceLayout;
    private final Context mContext;
    private final onItemButtonClickListener listener;

    public ImageButton btnEdit, btnDelete;
    public ImageView tvImage;
    public TextView tvName, tvCategory, tvExpiration;

    public interface onItemButtonClickListener {
        void onEditButtonClick(int position);
        void onDeleteButtonClick(int position);
    }

    public FoodItemAdapter(
            Context context,
            int resource,
            List<FoodItem> items,
            onItemButtonClickListener listener
    ) {
        super(context, resource, items);
        this.mContext = context;
        this.resourceLayout = resource;
        this.listener = listener;
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

        final FoodItem item = getItem(position);
        if (item == null) {
            return v;
        }

        // 1) 뷰 바인딩
        tvImage    = v.findViewById(R.id.imgFood);
        tvName     = v.findViewById(R.id.tvFoodName);
        tvCategory = v.findViewById(R.id.tvCategory);
        tvExpiration = v.findViewById(R.id.tvExpirationDate);
        btnEdit    = v.findViewById(R.id.btnEdit);
        btnDelete  = v.findViewById(R.id.btnDelete);

        // 2) 텍스트 값 적용
        tvName.setText(item.getName());
        tvCategory.setText(item.getCategory());
        // FoodItem에 저장된 expiration이 String(예: "2025-06-07") 형태라고 가정
        tvExpiration.setText(item.getExpiration() + "까지");

        // 3) 이미지 로딩: imageUrl이 “file://...” 형태로 저장되어 있다고 가정
        String imageUrl = item.getImageUrl();
        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            Glide.with(mContext)
                    .load(imageUrl)                  // file:// 경로든 URL이든 Glide가 자동으로 처리
                    .placeholder(R.drawable.loading) // 로딩 중 대체 이미지
                    .error(R.drawable.error)         // 로딩 실패 시 대체 이미지
                    .into(tvImage);
        } else {
            // imageUrl이 없으면 기본 에러 이미지를 띄워 줌
            tvImage.setImageResource(R.drawable.error);
        }

        // 4) 편집 버튼 리스너
        btnEdit.setOnClickListener(view -> {
            if (listener != null) {
                listener.onEditButtonClick(position);
            }
        });

        // 5) 삭제 버튼 리스너
        btnDelete.setOnClickListener(view -> {
            if (listener != null) {
                listener.onDeleteButtonClick(position);
            }
        });

        return v;
    }
}
