package com.example.finalcasestudy;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ItemLostAdapter extends RecyclerView.Adapter<ItemLostAdapter.ViewHolder> {

    private Context context;
    private List<ItemLostData> itemList;

    public ItemLostAdapter(Context context, List<ItemLostData> itemList) {
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_item_lost_frame, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItemLostData item = itemList.get(position);
        holder.tvItemName.setText(item.getName());
        holder.tvDate.setText(item.getDate());
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
