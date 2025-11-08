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

public class ItemFoundAdapter extends RecyclerView.Adapter<ItemFoundAdapter.ViewHolder> {

    // Declare context and list of items
    private Context context;
    private List<ItemFoundData> itemList;

    // Adapter constructor to initialize context and item list
    public ItemFoundAdapter(Context context, List<ItemFoundData> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    // Inflate the layout for each RecyclerView item
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_item_found_frame, parent, false);
        return new ViewHolder(view);
    }

    // Binds data to each item in the RecyclerView
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get the current item from the list
        ItemFoundData item = itemList.get(position);

        // Set the item name and date text
        holder.tvItemName.setText(item.getName());
        holder.tvDate.setText(item.getDate());

        // Load item image using Picasso if image URL is valid
        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            Picasso.get()
                    .load(item.getImageUrl())                     // Load image from URL
                    .placeholder(R.drawable.plus_placeholder)     // Image shown while loading
                    .error(R.drawable.plus_placeholder)           // Image shown if loading fails
                    .fit()                                        // Scale image to fit the ImageView
                    .centerCrop()                                 // Crop image to fill the space
                    .into(holder.ivItemImage);                    // Set image into ImageView
        } else {
            holder.ivItemImage.setImageResource(R.drawable.plus_placeholder);
        }

        // When "More Details" text is clicked, it will open FoundDetailsActivity
        holder.tvMoreDetails.setOnClickListener(v -> {
            Intent intent = new Intent(context, FoundDetailsActivity.class);
            intent.putExtra("documentId", item.getDocumentId()); // Pass Firestore document ID
            context.startActivity(intent);                       // Start the details screen
        });
    }

    // Returns total number of items in the list
    @Override
    public int getItemCount() {
        return itemList.size();
    }

    // ViewHolder class holds references to the item layout views for faster access
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvItemName, tvDate, tvMoreDetails;
        ImageView ivItemImage;

        // Connects layout components to Java variables
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemName = itemView.findViewById(R.id.textview3);  // Displays item name
            tvDate = itemView.findViewById(R.id.textview4);      // Displays found date
            tvMoreDetails = itemView.findViewById(R.id.textview5); // "More Details" text
            ivItemImage = itemView.findViewById(R.id.imageview2);  // Displays item image
        }
    }
}
