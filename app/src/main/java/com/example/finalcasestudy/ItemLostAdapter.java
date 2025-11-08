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

public class ItemLostAdapter extends RecyclerView.Adapter<ItemLostAdapter.ViewHolder> {

    private Context context;
    private List<ItemLostData> itemList;

    // Constructor — takes in context (for UI navigation) and list of lost items
    public ItemLostAdapter(Context context, List<ItemLostData> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    // Inflates each item layout from XML and creates a ViewHolder
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for a single item card (activity_item_lost_frame.xml)
        View view = LayoutInflater.from(context).inflate(R.layout.activity_item_lost_frame, parent, false);
        return new ViewHolder(view);
    }

    // Binds data from ItemLostData to each item view
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItemLostData item = itemList.get(position);

        // Display the item name and date lost
        holder.tvItemName.setText(item.getName());
        holder.tvDate.setText(item.getDate());

        // Load the image using Picasso (handles image loading from URLs efficiently)
        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            Picasso.get()
                    .load(item.getImageUrl())
                    .placeholder(R.drawable.plus_placeholder)
                    .error(R.drawable.plus_placeholder)
                    .fit()
                    .centerCrop()
                    .into(holder.ivItemImage);
        } else {
            // If no image URL exists, use the placeholder
            holder.ivItemImage.setImageResource(R.drawable.plus_placeholder);
        }

        // When the "More Details" text is clicked, open the LostDetailsActivity
        holder.tvMoreDetails.setOnClickListener(v -> {
            Intent intent = new Intent(context, LostDetailsActivity.class);
            intent.putExtra("documentId", item.getDocumentId());
            context.startActivity(intent);
        });
    }

    // Returns how many items are in the list
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

            // Connect Java variables with their corresponding XML elements
            tvItemName = itemView.findViewById(R.id.textview3);
            tvDate = itemView.findViewById(R.id.textview4);
            tvMoreDetails = itemView.findViewById(R.id.textview5);
            ivItemImage = itemView.findViewById(R.id.imageview2);
        }
    }
}
