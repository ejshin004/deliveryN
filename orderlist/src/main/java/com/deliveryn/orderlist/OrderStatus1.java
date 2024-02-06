package com.deliveryn.orderlist;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OrderStatus1 extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adater;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<OrderInfoModel> arrayList;
    private FirebaseDatabase database;
    Button orderBtn, reportBtn1;
    View dialogView;
    EditText edt_delivery_fee, edt_bank_name, edt_account_number, report_nickname, report_reason;
    Spinner report_type;
    String boss, roomId, report_nickname_value, report_radioBtn_value, type_value, report_reason_value, reporter;
    Integer people_num;
    Double delivery_fee;
    RadioGroup radioGroup;
    LinearLayout linearLayout_report_reason_visibility;
    int flag = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status1);

        orderBtn = (Button) findViewById(R.id.orderBtn);
        reportBtn1 = (Button) findViewById(R.id.reportBtn1);
        recyclerView = findViewById(R.id.recyclerView1);
        recyclerView.setHasFixedSize(true); //리사이클러뷰 기존 성능 강화
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        arrayList = new ArrayList<>(); //OrderInfoModel 객체를 담을 어레이 리스트(어댑터쪽으로)

        database = FirebaseDatabase.getInstance(); //파이어베이스 데이터베이스 연동

        Intent intent = getIntent();
        roomId = intent.getStringExtra("roomId");

        database.getReference("orderlist").child(roomId).child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                //파이어베이스 데이터베이스의 데이터를 받아오는 곳
                arrayList.clear(); //기존 배열 리스트가 존재하지 않게 초기화
                for (DataSnapshot snapshot : datasnapshot.getChildren()) { //반복문으로 데이터 List를 추출해냄
                    OrderInfoModel orderInfoModel = snapshot.getValue(OrderInfoModel.class); //만들어뒀던 OrderInfoModel 객체에 데이터를 담는다.
                    orderInfoModel.setMenu(snapshot.child("menu").getValue().toString());
                    orderInfoModel.setOption(snapshot.child("option").getValue().toString());
                    orderInfoModel.setPrice(snapshot.child("price").getValue().toString());
                    orderInfoModel.setNickname(snapshot.child("nickname").getValue().toString());

                    arrayList.add(orderInfoModel); //담은 데이터들을 배열 리스트에 넣고 리사이클러뷰로 보낼 준비
                }
                adater.notifyDataSetChanged(); //리스트 저장 및 새로고침
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        adater = new CustomAdapter1(arrayList, this);
        recyclerView.setAdapter(adater); //리사이클러뷰에 어댑터 연결

        //방장 정보 가져오기
        database.getReference().child("orderlist").child(roomId).child("boss").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                }
                else {
                    boss = String.valueOf(task.getResult().getValue());
                }
            }
        });

        orderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(boss.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                    //참여자 있는지 확인
                    FirebaseDatabase.getInstance().getReference().child("orderlist").child(roomId).child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Map<String, Boolean> participationUsers = (Map<String, Boolean>)snapshot.getValue();

                            if (participationUsers == null || participationUsers.isEmpty()) {
                                Toast.makeText(OrderStatus1.this, "참여자가 없습니다.", Toast.LENGTH_SHORT).show();
                            }

                            else {
                                dialogView = (View) View.inflate(OrderStatus1.this, R.layout.order_menu, null);
                                AlertDialog.Builder dlg = new AlertDialog.Builder(OrderStatus1.this, R.style.MyAlertDialogStyle);
                                dlg.setView(dialogView);

                                dlg.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        edt_delivery_fee = (EditText) dialogView.findViewById(R.id.edt_delivery_fee);
                                        edt_account_number = (EditText) dialogView.findViewById(R.id.edt_account_number);
                                        edt_bank_name = (EditText) dialogView.findViewById(R.id.edt_bank_name);

                                        Map<String, Object> map1 = new HashMap<>(); //배달비 DB에 입력
                                        map1.put("delivery fee", edt_delivery_fee.getText().toString());
                                        FirebaseDatabase.getInstance().getReference().child("orderlist").child(roomId).updateChildren(map1);

                                        Map<String, Object> map2 = new HashMap<>(); //은행 이름 DB에 입력
                                        map2.put("bank", edt_bank_name.getText().toString());
                                        FirebaseDatabase.getInstance().getReference().child("orderlist").child(roomId).updateChildren(map2);

                                        Map<String, Object> map3 = new HashMap<>(); //계좌번호 DB에 입력
                                        map3.put("account number", edt_account_number.getText().toString());
                                        FirebaseDatabase.getInstance().getReference().child("orderlist").child(roomId).updateChildren(map3);
                                        Toast.makeText(OrderStatus1.this, "입력 완료", Toast.LENGTH_SHORT).show();

                                        //파이어베이스 실시간 데이터베이스에 주문 여부 before->after로 변경
                                        Map<String, Object> map4 = new HashMap<>();
                                        map4.put("orderState", "after");
                                        FirebaseDatabase.getInstance().getReference().child("orderlist").child(roomId).updateChildren(map4);

                                        //총 배달비
                                        delivery_fee = Double.parseDouble(edt_delivery_fee.getText().toString());

                                        database.getReference().child("orderlist").child(roomId).child("orderNum").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                                if (!task.isSuccessful()) {
                                                    Log.e("firebase", "Error getting data", task.getException());
                                                }
                                                else {
                                                    people_num = Integer.parseInt(String.valueOf(task.getResult().getValue()));

                                                    //1인당 배달비(소수점 첫 번째 자리에서 올림)
                                                    int per_delivery_fee = (int) Math.ceil(delivery_fee / people_num);
                                                    Map<String, Object> map5 = new HashMap<>();
                                                    map5.put("per delivery fee", per_delivery_fee);
                                                    FirebaseDatabase.getInstance().getReference().child("orderlist").child(roomId).updateChildren(map5);
                                                }
                                            }
                                        });

                                        //주문하기 푸시 알림
                                        FirebaseDatabase.getInstance().getReference().child("orderlist").child(roomId).child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                Map<String, Boolean> participationUsers = (Map<String, Boolean>)snapshot.getValue();

                                                for(String item: participationUsers.keySet()) {
                                                    database.getReference().child("users").child(item).child("pushToken").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                                                            if (!task.isSuccessful()) {
                                                                Log.e("firebase", "Error getting data", task.getException());
                                                            }
                                                            else {
                                                                String pushToken = String.valueOf(task.getResult().getValue());
                                                                sendFCM(pushToken);
                                                            }
                                                        }
                                                    });
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });

                                        // 총 금액 나오는 주문 현황 액티비티로 전환
                                        Intent roomId_intent = new Intent(OrderStatus1.this, OrderStatus2.class);
                                        roomId_intent.putExtra("roomId", roomId);
                                        startActivity(roomId_intent);
                                        finish();
                                    }
                                });
                                dlg.setNegativeButton("취소", null);
                                dlg.show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }
                else {
                    Toast.makeText(OrderStatus1.this, "권한이 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        reportBtn1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialogView = (View) View.inflate(OrderStatus1.this, R.layout.report_dialog, null);
                androidx.appcompat.app.AlertDialog.Builder dlg = new AlertDialog.Builder(OrderStatus1.this, R.style.MyAlertDialogStyle);
                dlg.setTitle("           <신고할 사용자 정보 입력>");
                dlg.setView(dialogView);

                radioGroup = (RadioGroup) dialogView.findViewById(R.id.radioGroup);
                linearLayout_report_reason_visibility = (LinearLayout) dialogView.findViewById(R.id.linearLayout_report_reason);
                report_reason = (EditText) dialogView.findViewById(R.id.report_reason);

                // 신고 유형 - 기타 누르면 신고 사유 visible
                report_type = (Spinner) dialogView.findViewById(R.id.report_list);
                ArrayAdapter adapter = ArrayAdapter.createFromResource(OrderStatus1.this, R.array.report_spinner_array, android.R.layout.simple_spinner_dropdown_item);

                // 드롭다운 클릭 시 선택 창
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                // 스피너에 어댑터 설정
                report_type.setAdapter(adapter);

                // 스피너에서 선택 했을 경우 이벤트 처리
                report_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        // 신고 유형 - 기타 눌렀을 경우
                        if(report_type.getItemAtPosition(position).equals("기타") == true) {
                            flag = 1;
                            linearLayout_report_reason_visibility.setVisibility(View.VISIBLE);
                            report_reason.setText(null); // 신고 사유 EditText 초기화
                        }
                        else {
                            flag = 0;
                            linearLayout_report_reason_visibility.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        flag = 0;
                        type_value = report_type.getSelectedItem().toString();
                    }
                });

                radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        if (checkedId == R.id.report_radioBtn_boss) {
                            report_radioBtn_value = "방장";
                        } else{
                            report_radioBtn_value = "참여자";
                        }
                    }
                });

                dlg.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 신고할 닉네임
                        report_nickname = (EditText) dialogView.findViewById(R.id.report_nickname);
                        report_nickname_value = report_nickname.getText().toString();

                        // 신고 유형
                        type_value = report_type.getSelectedItem().toString();

                        // 신고하는 계정 닉네임(현재 사용자)
                        reporter = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();

                        // 현재 날짜 및 시간
                        Object datetime = ServerValue.TIMESTAMP;

                        // 신고 사유 있는 경우(= 신고 유형이 기타인 경우)
                        if(flag == 1) {
                            report_reason_value = report_reason.getText().toString();
                            insertReportWithReason(reporter, datetime, report_radioBtn_value, report_nickname_value, type_value, report_reason_value);
                        }

                        // 신고 사유 없는 경우(= 신고 유형이 기타가 아닌 경우)
                        else {
                            insertReportWithoutReason(reporter, datetime, report_radioBtn_value, report_nickname_value, type_value);
                        }

                        Toast.makeText(OrderStatus1.this, "신고되었습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
                dlg.setNegativeButton("취소", null);
                dlg.show();
            }
        });
    }

    public void insertReportWithReason (String reporter, Object datetime, String reported_type, String reported_nickname, String report_type, String specific) {
        ReportModel ReportModel = new ReportModel(reporter, datetime, reported_type, reported_nickname, report_type, specific);
        DatabaseReference pushRef = database.getReference("report").push();
        pushRef.setValue(ReportModel);
    }

    public void insertReportWithoutReason (String reporter, Object datetime, String reported_type, String reported_nickname, String report_type) {
        ReportModel ReportModel = new ReportModel(reporter, datetime, reported_type, reported_nickname, report_type);
        DatabaseReference pushRef = database.getReference("report").push();
        pushRef.setValue(ReportModel);
    }

    void sendFCM(String pushToken) {
        Gson gson = new Gson();

        NotificationModel notificationModel = new NotificationModel();
        notificationModel.to = pushToken;
        notificationModel.data.title = "입금 요청!";
        notificationModel.data.text = "방장이 주문을 하려고 합니다.";

        RequestBody requestBody = RequestBody.create(gson.toJson(notificationModel), MediaType.parse("application/json; charset=utf8"));

        Request request = new Request.Builder()
                .header("Content-Type", "application/json")
                //.addHeader("Authorization", "key=") //보안상 키 값 지움
                .url("https://fcm.googleapis.com/fcm/send")
                .post(requestBody)
                .build();

        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("push 알림: ","실패");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                //Log.e("push 알림: ",response.body().string());
            }
        });
    }
}