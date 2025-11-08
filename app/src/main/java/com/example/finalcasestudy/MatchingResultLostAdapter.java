package com.example.finalcasestudy;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class MatchingResultLostAdapter extends RecyclerView.Adapter<MatchingResultLostAdapter.ViewHolder> {

    // Declare context and list of items
    private Context context;
    private List<MatchingResultLostData> itemList;

    // Adapter constructor to initialize context and item list
    public MatchingResultLostAdapter(Context context, List<MatchingResultLostData> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    // Inflate the layout for each RecyclerView item
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_matching_result_lost_frame, parent, false);
        return new ViewHolder(view);
    }

    // Bind data to each RecyclerView item
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MatchingResultLostData item = itemList.get(position);

        // Set item name and date
        holder.tvItemName.setText(item.getItemName());
        holder.tvDate.setText(item.getDate());

        // Load item image using Picasso if image URL is valid
        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            Picasso.get()
                    .load(item.getImageUrl())
                    .placeholder(R.drawable.plus_placeholder)
                    .error(R.drawable.plus_placeholder)
                    .fit()
                    .centerCrop()
                    .into(holder.ivItemImage);
        } else {
            holder.ivItemImage.setImageResource(R.drawable.plus_placeholder);
        }

        // Handle more details textview
        holder.tvMoreDetails.setOnClickListener(v -> {
            Intent intent = new Intent(context, LostDetailsActivity.class);
            intent.putExtra("documentId", item.getDocumentId());
            context.startActivity(intent);
        });
    }

    // Return total number of items in the list
    @Override
    public int getItemCount() {
        return itemList.size();
    }

    // ViewHolder class to hold item views
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvItemName, tvDate, tvMoreDetails;
        ImageView ivItemImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize views from layout
            tvItemName = itemView.findViewById(R.id.textview3);
            tvDate = itemView.findViewById(R.id.textview4);
            tvMoreDetails = itemView.findViewById(R.id.textview5);
            ivItemImage = itemView.findViewById(R.id.imageview2);
        }
    }
}
