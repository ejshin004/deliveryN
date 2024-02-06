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

public class CustomAdapter1 extends RecyclerView.Adapter<CustomAdapter1.CustomViewHolder> {

    private ArrayList<OrderInfoModel> arrayList;
    private Context context;

    public CustomAdapter1(ArrayList<OrderInfoModel> arrayList, Context context) {
        this.arrayList = arrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item1, parent, false);
        CustomViewHolder holder = new CustomViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder holder, int position) {
        holder.tv_user.setText(arrayList.get(position).getNickname());
        holder.tv_menu.setText(arrayList.get(position).getMenu());
        holder.tv_option.setText(arrayList.get(position).getOption());
        holder.tv_price.setText(arrayList.get(position).getPrice());
    }

    @Override
    public int getItemCount() {
        return (arrayList != null ? arrayList.size() : 0);
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder {
        TextView tv_user;
        TextView tv_menu;
        TextView tv_option;
        TextView tv_price;

        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);
            this.tv_user = itemView.findViewById(R.id.tv_user);
            this.tv_menu = itemView.findViewById(R.id.tv_menu);
            this.tv_option = itemView.findViewById(R.id.tv_option);
            this.tv_price = itemView.findViewById(R.id.tv_price);

            this.tv_user.setTextColor(Color.BLACK);
            this.tv_menu.setTextColor(Color.BLACK);
            this.tv_option.setTextColor(Color.BLACK);
            this.tv_price.setTextColor(Color.BLACK);
        }
    }
}