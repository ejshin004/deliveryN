package com.deliveryn.orderlist;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class CustomAdapter extends ArrayAdapter {
    public CustomAdapter(Context context, ArrayList users){
        super(context, android.R.layout.simple_dropdown_item_1line, users);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.listview_item, parent, false);
        }

        OrderRoom odRoom = (OrderRoom) getItem(position);

        TextView restuarantName = (TextView) convertView.findViewById(R.id.restuarnt);
        TextView timeAndLocation = (TextView) convertView.findViewById(R.id.time_and_location);
        ImageView foodImage = (ImageView) convertView.findViewById(R.id.food_image_view);
        restuarantName.setText(odRoom.resName);
        String tl = odRoom.deliverTime + " / " + odRoom.deliverLocation;
        timeAndLocation.setText(tl);

        switch (odRoom.resCategory){
            case "돈가스/회/일식":
                foodImage.setImageResource(R.drawable.sushi);
                break;
            case "중식":
                foodImage.setImageResource(R.drawable.dumpling);
                break;
            case "치킨":
                foodImage.setImageResource(R.drawable.chicken);
                break;
            case "백반/죽/국수":
                foodImage.setImageResource(R.drawable.rice);
                break;
            case "카페/디저트":
                foodImage.setImageResource(R.drawable.cake);
                break;
            case "분식":
                foodImage.setImageResource(R.drawable.ramen);
                break;
            case "찜/탕/찌개":
                foodImage.setImageResource(R.drawable.pot);
                break;
            case "양식":
                foodImage.setImageResource(R.drawable.pasta);
                break;
            case "고기/구이":
                foodImage.setImageResource(R.drawable.meat);
                break;
            case "족발/보쌈":
                foodImage.setImageResource(R.drawable.edit);
                break;
            case "아시안":
                foodImage.setImageResource(R.drawable.asian);
                break;
            case "패스트푸드":
                foodImage.setImageResource(R.drawable.burger);
                break;
            case "도시락":
                foodImage.setImageResource(R.drawable.meal);
                break;
            default:
                Log.e("error", odRoom.resCategory);
                break;
        }

        return convertView;

    }
}
