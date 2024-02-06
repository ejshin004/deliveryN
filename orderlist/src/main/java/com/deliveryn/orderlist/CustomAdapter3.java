package com.deliveryn.orderlist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.google.firebase.database.ServerValue;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

public class CustomAdapter3 extends ArrayAdapter {

    public CustomAdapter3(Context context, ArrayList report){
        super(context, android.R.layout.simple_dropdown_item_1line, report);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.listview_item_report, parent, false);
        }

        ReportModel reportVal = (ReportModel) getItem(position);

        TextView aboutReport = (TextView) convertView.findViewById(R.id.aboutReport);

        // TIMESTAMP를 변환함
        SimpleDateFormat datetimeFormat = new SimpleDateFormat("yyyy-MM-dd (HH:mm)");
        long convertDatetime = (long) reportVal.datetime;
        datetimeFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
        String datetime = datetimeFormat.format(convertDatetime);

        String repName = "신고자: " + reportVal.reporter_nickname + "\n";
        String repType = "신고유형: " + reportVal.report_type + "\n";
        String repDatetime = "신고일자: " + datetime;
        aboutReport.setText(repName + repType + repDatetime);

        return convertView;

    }


}
