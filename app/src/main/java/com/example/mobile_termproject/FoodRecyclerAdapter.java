package com.example.mobile_termproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobile_termproject.Data.FoodItem;

import java.util.List;

public class FoodRecyclerAdapter extends RecyclerView.Adapter<FoodRecyclerAdapter.FoodViewHolder> {

    private final List<FoodItem> foodList;

    public FoodRecyclerAdapter(List<FoodItem> foodList) {
        this.foodList = foodList;
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_food, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        FoodItem item = foodList.get(position);

        holder.tvName.setText(item.getName());
        holder.tvCategory.setText("카테고리: " + item.getCategory());
        holder.tvExpiration.setText("유통기한: " + item.getExpiration());

        holder.btnEdit.setOnClickListener(v ->
                Toast.makeText(holder.itemView.getContext(), item.getName() + " 수정 클릭", Toast.LENGTH_SHORT).show()
        );
        holder.btnDelete.setOnClickListener(v ->
                Toast.makeText(holder.itemView.getContext(), item.getName() + " 삭제 클릭", Toast.LENGTH_SHORT).show()
        );
    }

    @Override
    public int getItemCount() {
        return foodList.size();
    }

    static class FoodViewHolder extends RecyclerView.ViewHolder {
        ImageView tvImage;
        TextView tvName, tvCategory, tvExpiration;
        Button btnEdit, btnDelete;

        public FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            tvImage = itemView.findViewById(R.id.imgIngredient);
            tvName = itemView.findViewById(R.id.tvFoodName);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvExpiration = itemView.findViewById(R.id.tvExpirationDate);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
