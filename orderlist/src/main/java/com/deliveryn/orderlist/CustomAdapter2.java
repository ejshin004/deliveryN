package com.deliveryn.orderlist;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CustomAdapter2 extends RecyclerView.Adapter<CustomAdapter2.CustomViewHolder> {

    private ArrayList<FinalOrderPriceModel> arrayList;
    private Context context;

    public CustomAdapter2(ArrayList<FinalOrderPriceModel> arrayList, Context context) {
        this.arrayList = arrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item2, parent, false);
        CustomViewHolder holder = new CustomViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder holder, int position) {
        holder.user.setText(arrayList.get(position).getUser());
        holder.total_price.setText(arrayList.get(position).getTotal_price());
    }

    @Override
    public int getItemCount() {
        return (arrayList != null ? arrayList.size() : 0);
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder {
        TextView user;
        TextView total_price;

        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);
            this.user = itemView.findViewById(R.id.tv_total_user);
            this.total_price = itemView.findViewById(R.id.tv_total_price);

            this.user.setTextColor(Color.BLACK);
            this.total_price.setTextColor(Color.BLACK);
        }
    }
}