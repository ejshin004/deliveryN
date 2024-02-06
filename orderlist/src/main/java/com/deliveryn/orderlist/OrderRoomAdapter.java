package com.deliveryn.orderlist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class OrderRoomAdapter extends ArrayAdapter<OrderRoom> {

    public OrderRoomAdapter(Context context, int resource, List<OrderRoom> orderRoomList){
        super(context, resource, orderRoomList);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        OrderRoom odRoom = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.order_room_page, parent, false);
        }

        TextView restName = convertView.findViewById(R.id.r_name);
        TextView resCategory = convertView.findViewById(R.id.r_category);
        TextView ordTime = convertView.findViewById(R.id.od_time);
        TextView deliverLocation = convertView.findViewById(R.id.del_loc);
        TextView deliverLink = convertView.findViewById(R.id.del_link);

        restName.setText(odRoom.resName);
        resCategory.setText(odRoom.resCategory);
        ordTime.setText(odRoom.deliverTime);
        deliverLocation.setText(odRoom.deliverLocation);
        deliverLink.setText(odRoom.deliverLink);

        return convertView;
    }
}
