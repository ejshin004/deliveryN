package com.deliveryn.orderlist;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TimeZone;

public class AdminActivity extends AppCompatActivity {

    ListView reportList;
    TextView reporterName, reportedName, reportDT, reportType, reportSpec;
    View specReportDialog;

    CustomAdapter3 reportAdapter;
    public static ArrayList<ReportModel> reportArrayList = new ArrayList<ReportModel>();


    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("report");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        reportList = (ListView) findViewById(R.id.reported);

        reportAdapter = new CustomAdapter3(this, new ArrayList<ReportModel>());
        reportList.setAdapter(reportAdapter);

        // 이미지 버튼 go_back : 로그인 화면으로 돌아가기 구현

        // 신고하기 데이터베이스에서 정보 가져오기

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                reportAdapter.clear();

                for (DataSnapshot post: snapshot.getChildren()) {
                    ReportModel reportVal;

                    String reporter = post.child("reporter_nickname").getValue().toString();
                    String reported = post.child("reported_nickname").getValue().toString();
                    Object reportDateTime = post.child("datetime").getValue();
                    String reportedUserType = post.child("reported_user_type").getValue().toString();
                    String type = post.child("report_type").getValue().toString();
                    if (post.child("specific").getValue() != null) {
                        String specific = post.child("specific").getValue().toString();
                        reportVal = new ReportModel(reporter, reportDateTime, reportedUserType, reported, type, specific);
                    } else {
                        reportVal = new ReportModel(reporter, reportDateTime, reportedUserType, reported, type);

                    }

                    reportArrayList.add(reportVal);
                    reportAdapter.add(reportVal);
                }
                reportAdapter.notifyDataSetChanged();
                reportList.setSelection(reportAdapter.getCount() - 1);
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // 리스트 내의 항목 선택 시 상세 항목 표시 (안드로이드 대화 상자 이용)
        reportList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                ReportModel reportVal = (ReportModel) reportList.getItemAtPosition(position);
                specReportDialog = (View) view.inflate(AdminActivity.this, R.layout.spec_report_dialog, null);
                AlertDialog.Builder dlg = new AlertDialog.Builder(AdminActivity.this, R.style.MyAlertDialogStyle);
                dlg.setTitle("<신고 상세 페이지>");
                dlg.setView(specReportDialog);

                reporterName = (TextView) specReportDialog.findViewById(R.id.reporterName);
                reportedName = (TextView) specReportDialog.findViewById(R.id.reportedName);
                reportDT = (TextView) specReportDialog.findViewById(R.id.reportDateTime);
                reportType = (TextView) specReportDialog.findViewById(R.id.reportType);
                reportSpec = (TextView) specReportDialog.findViewById(R.id.reportSpecific);

                // TIMESTAMP를 변환함
                SimpleDateFormat datetimeFormat = new SimpleDateFormat("yyyy-MM-dd (HH:mm)");
                long convertDatetime = (long) reportVal.datetime;
                datetimeFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
                String datetime = datetimeFormat.format(convertDatetime);


                reporterName.setText("신고자: " + reportVal.reporter_nickname);
                reportedName.setText("신고당한 자: " + reportVal.reported_nickname + " (" + reportVal.reported_user_type + ")");
                reportDT.setText("신고일자: " + datetime);
                reportType.setText("신고 유형: " + reportVal.report_type);
                if (reportVal.specific == null) {
                    reportSpec.setText("신고 세부사유: 없음");
                } else {
                    reportSpec.setText("신고 세부사유: " + reportVal.specific);
                }


                dlg.setPositiveButton("확인", null);
                dlg.show();

            }
        });
    }

}