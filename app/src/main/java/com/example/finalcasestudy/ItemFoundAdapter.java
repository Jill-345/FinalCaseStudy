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

    private Context context;
    private List<ItemFoundData> itemList;

    public ItemFoundAdapter(Context context, List<ItemFoundData> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_item_found_frame, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItemFoundData item = itemList.get(position);
        holder.tvItemName.setText(item.getName());
        holder.tvDate.setText(item.getDate());

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

        // ✅ Click “More Details” to open FoundDetailsActivity
        holder.tvMoreDetails.setOnClickListener(v -> {
            Intent intent = new Intent(context, FoundDetailsActivity.class);
            intent.putExtra("documentId", item.getDocumentId());
            context.startActivity(intent);
        });
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
            tvMoreDetails = itemView.findViewById(R.id.textview5); // make sure exists in layout
            ivItemImage = itemView.findViewById(R.id.imageview2);
        }
    }
}
