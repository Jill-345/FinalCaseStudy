package com.example.finalcasestudy;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ItemFoundAdapter extends RecyclerView.Adapter<ItemFoundAdapter.ViewHolder> {

    private List<ItemFoundData> itemList;

    // Constructor
    public ItemFoundAdapter(List<ItemFoundData> itemList) {
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Use your existing XML layout here
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_item_found_frame, parent, false); // keep your layout
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItemFoundData item = itemList.get(position);
        holder.tvItemName.setText(item.getName());
        holder.tvDate.setText(item.getDate());

        // If you have image URLs, you can load them like this (optional):
        // Glide.with(holder.ivItemImage.getContext())
        //      .load(item.getImageUrl())
        //      .placeholder(R.drawable.plus_placeholder)
        //      .into(holder.ivItemImage);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvItemName, tvDate, tvMoreDetails;
        ImageView ivItemImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemName = itemView.findViewById(R.id.textview3);
            tvDate = itemView.findViewById(R.id.textview4);
            tvMoreDetails = itemView.findViewById(R.id.textview5);
            ivItemImage = itemView.findViewById(R.id.imageview2);
        }
    }
}
