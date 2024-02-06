package com.deliveryn.orderlist;


import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.recyclerview.widget.RecyclerView;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;


public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {
    private List<ChatData> mDataset;
    private String myNickName;
    public static RelativeLayout.LayoutParams mLayoutParams2;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView Textview_nickname;
        public TextView Textview_msg;
        public TextView Textview_time;
        public RelativeLayout msgLinear;


        public ViewHolder(View v) {
            super(v);
            // Define click listener for the ViewHolder's View
            msgLinear = v.findViewById(R.id.msg_relative);
            Textview_nickname = v.findViewById(R.id.TextView_nickname);
            Textview_msg = v.findViewById(R.id.TextView_msg);
            Textview_time = v.findViewById(R.id.TextView_time);
            mLayoutParams2 = (RelativeLayout.LayoutParams) Textview_time.getLayoutParams();
        }

    }

    public ChatAdapter(List<ChatData> myDataset, String myNickName) {
        mDataset = myDataset;
        this.myNickName = myNickName;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ChatAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Create a new view, which defines the UI of the list item
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_chat, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        ChatData chat = mDataset.get(position);

        viewHolder.Textview_nickname.setText(chat.getNickname());
        viewHolder.Textview_msg.setText(chat.getMsg());

        long unixTime = (long) chat.getTime();
        Date date = new Date(unixTime);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
        String time = simpleDateFormat.format(date);
        viewHolder.Textview_time.setText(time);

        if(chat.getNickname().equals(this.myNickName)){
            // viewHolder.Textview_msg.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
            viewHolder.Textview_nickname.setVisibility(View.GONE);

            RelativeLayout.LayoutParams mLayoutParams3 = (RelativeLayout.LayoutParams) viewHolder.Textview_msg.getLayoutParams();
            mLayoutParams3.addRule(RelativeLayout.RIGHT_OF,viewHolder.Textview_time.getId());
            viewHolder.msgLinear.setGravity(Gravity.END);

            setBgColor(Color.parseColor("#FEF01B"),viewHolder);
            Log.e("tag", "my");
        }
        else {
            mLayoutParams2.addRule(RelativeLayout.RIGHT_OF,viewHolder.Textview_msg.getId());
            viewHolder.Textview_nickname.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
            viewHolder.msgLinear.setGravity(Gravity.START);

            setBgColor(Color.parseColor("#FFFFFF"),viewHolder);
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset == null ? 0 : mDataset.size();
    }

    public ChatData getChat(int position) {
        return mDataset != null ? mDataset.get(position) : null;
    }

    public void addChat(ChatData chat) {
        mDataset.add(chat);
        notifyItemInserted(mDataset.size()-1);
    }

    public void setBgColor(@ColorInt int color, ViewHolder v){
        Drawable backgroundOff = v.Textview_msg.getBackground();
        backgroundOff.setTint(color);
        v.Textview_msg.setBackground(backgroundOff);
    }
}
