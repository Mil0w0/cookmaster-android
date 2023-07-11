package com.example.cookmaster_at_home_app;

import android.content.Context;
import android.content.Intent;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class RewardsAdapter extends RecyclerView.Adapter<RewardsAdapter.MyViewHolder> {

    private final List<Item> itemList;
    private final Context context;
    public RewardsAdapter(List<Item> itemList, Context context) {
        this.itemList = itemList;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Item item = itemList.get(position);
        holder.itemName.setText(item.getName());
        String url_image = "https://becomeacookmaster.live/assets/images/shop-items/" + item.getImage();
        Picasso.get().load(url_image).into(holder.itemImage);
        if (item.getStock() == 0){
            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation(0);
            ColorMatrixColorFilter filter_color = new ColorMatrixColorFilter(matrix);
            holder.itemImage.setColorFilter(filter_color);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (item.getStock() == 0){
                    Toast.makeText(context, "This item is out of stock!", Toast.LENGTH_SHORT).show();
                } else {
                    Bundle bundle = ((RewardsActivity) context).getIntent().getExtras();
                    int user_id = bundle.getInt("user_id", -1);
                    int subscriptionId = bundle.getInt("subscription_id", -1);
                    int auto_reconnect = bundle.getInt("auto_reconnect", -1);
                    int fidelitypoints = bundle.getInt("fidelitypoints", -1);

                    Intent intent = new Intent(context, ItemActivity.class);
                    intent.putExtra("item_id", item.getId());
                    intent.putExtra("user_id", user_id);
                    intent.putExtra("subscription_id", subscriptionId);
                    intent.putExtra("auto_reconnect", auto_reconnect);
                    intent.putExtra("fidelitypoints", fidelitypoints);

                    context.startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layout;
        TextView itemName;
        ImageView itemImage;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.item_layout);
            itemName = itemView.findViewById(R.id.item_name);
            itemImage = itemView.findViewById(R.id.item_image);
        }
    }
}
