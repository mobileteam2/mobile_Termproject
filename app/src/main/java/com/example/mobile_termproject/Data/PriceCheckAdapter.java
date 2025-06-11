package com.example.mobile_termproject.Data;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mobile_termproject.Data.FoodItem;
import com.example.mobile_termproject.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.List;
import java.util.Map;

public class PriceCheckAdapter extends RecyclerView.Adapter<PriceCheckAdapter.ViewHolder> {
    private List<FoodItem> foodItems;
    private Map<String, NaverReturnResult> naverResults;

    public PriceCheckAdapter(List<FoodItem> foodItems, Map<String, NaverReturnResult> naverResults) {
        this.foodItems = foodItems;
        this.naverResults = naverResults;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_price_check, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FoodItem item = foodItems.get(position);
        Log.d("DEBUG", "바인딩 중인 아이템: " + item.getName());

        NaverReturnResult result = naverResults.get(item.getName());
        if (result != null) {
            holder.tvPrice.setText("현재가: " + result.getFormattedPrice());
            holder.tvName.setText(result.getName());
        } else {
            holder.tvPrice.setText("가격 정보 없음");
        }
        // Glide를 통한 이미지 로딩
        Glide.with(holder.itemView.getContext())
                .load(item.getImageUrl())
                .placeholder(R.drawable.loading)  // 미리 준비한 placeholder
                .into(holder.ivImage);

        holder.itemView.setOnClickListener(v -> {
            if (result != null) {
                Context context = v.getContext();
                Intent intent = new Intent(context, com.example.mobile_termproject.Acitivities.PriceDetailActivity.class);
                intent.putExtra("productName", result.getName());
                intent.putExtra("formattedPrice", result.getFormattedPrice());
                intent.putExtra("imageUrl", result.getImageUrl());
                intent.putExtra("productUrl", result.getProductUrl());
                intent.putExtra("itemName", item.getName());  // price_history 조회용
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return foodItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice;
        ImageView ivImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvFoodName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            ivImage = itemView.findViewById(R.id.ivImage);
        }
    }
}
