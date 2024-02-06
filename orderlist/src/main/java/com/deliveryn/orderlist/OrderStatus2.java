package com.deliveryn.orderlist;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ChecksSdkIntAtLeast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.KeyEventDispatcher;
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
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OrderStatus2 extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<FinalOrderPriceModel> arrayList;
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    Button OrderCompletedBtn, reportBtn2;
    ImageButton bank_btn;   // go to bank application
    EditText report_nickname, report_reason;
    Spinner report_type;
    TextView tv_bank_name, tv_account_number, dMoneyView;
    ImageView iv_account_number_copy;
    private String bank_name, account_number, boss, order_completed;
    String roomId, report_nickname_value, report_radioBtn_value, type_value, report_reason_value, reporter;
    RadioGroup radioGroup;
    LinearLayout linearLayout_report_reason_visibility;
    View dialogView, bankDialogView;
    GridView gv;
    int flag = 0, discounted_delivery_fee, discounted_delivery_fee2, per_delivery_fee;

    Integer[] bank = { R.drawable.kakao_bank, R.drawable.nh_bank, R.drawable.kb_bank, R.drawable.hana_bank,
                        R.drawable.woori_bank, R.drawable.shinhan_bank, R.drawable.toss, R.drawable.ibk_bank }; // bank_logo_image array
    String[] bankNames = { "카카오", "NH농협", "KB국민", "하나", "우리", "신한", "토스", "IBK기업" }; // bank_name_string array
    private final String[] bankPackageNames = { "com.kakaobank.channel", "nh.smart.banking", "com.kbstar.kbbank", "com.hanabank.ebk.channel.android.cpb",
                        "com.wooribank.smart.npib", "com.shinhan.sbanking", "viva.republica.toss", "com.ibk.android.ionebank"}; // bank_package_id array
    private Intent moveApp;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status2);

        reportBtn2 = (Button) findViewById(R.id.reportBtn2);
        recyclerView = findViewById(R.id.recyclerView2);
        recyclerView.setHasFixedSize(true); //리사이클러뷰 기존 성능 강화
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        arrayList = new ArrayList<>(); //FinalOrderPriceModel 객체를 담을 어레이 리스트(어댑터쪽으로)

        tv_bank_name = (TextView) findViewById(R.id.tv_bank_name);
        tv_account_number = (TextView) findViewById(R.id.tv_account_number);
        iv_account_number_copy = (ImageView) findViewById(R.id.bank_btn);
        OrderCompletedBtn = (Button) findViewById(R.id.OrderCompletedBtn);

        bank_btn = (ImageButton) findViewById(R.id.bank_btn);   // go to bank application

        database = FirebaseDatabase.getInstance(); //파이어베이스 데이터베이스 연동

        Intent intent = getIntent();
        roomId = intent.getStringExtra("roomId");

        //은행 이름 가져오기
        database.getReference().child("orderlist").child(roomId).child("bank").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                }
                else {
                    bank_name = String.valueOf(task.getResult().getValue());
                    tv_bank_name.setText(bank_name);
                }
            }
        });

        //계좌번호 가져오기
        database.getReference().child("orderlist").child(roomId).child("account number").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                }
                else {
                    account_number = String.valueOf(task.getResult().getValue());
                    tv_account_number.setText(account_number);
                }
            }
        });

        //1인당 배달비 정보 가져오기
        database.getReference().child("orderlist").child(roomId).child("per delivery fee").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                }
                //1인당 배달비 성공적으로 가져왔을 경우
                else {
                    int per_delivery_fee = Integer.parseInt(String.valueOf(task.getResult().getValue())); //1인당 배달비 정보 저장

                    //사용자별 주문 금액 정보 가져오기
                    databaseReference = database.getReference("orderlist").child(roomId).child("users"); //DB 테이블 연결
                    databaseReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                            arrayList.clear();
                            for(DataSnapshot snapshot : datasnapshot.getChildren()) {
                                OrderInfoModel OrderInfoModel = snapshot.getValue(OrderInfoModel.class);

                                //사용자별 주문 금액
                                int per_price = Integer.parseInt(OrderInfoModel.getPrice());

                                //사용자별 닉네임
                                String nickname = OrderInfoModel.getNickname();

                                //총 가격 = 1인당 배달비 + 사용자별 주문 금액
                                int per_total_price = per_price + per_delivery_fee;

                                FinalOrderPriceModel FinalOrderPriceModel = new FinalOrderPriceModel();
                                FinalOrderPriceModel.setUser(nickname);
                                FinalOrderPriceModel.setTotal_price(Integer.toString(per_total_price));

                                arrayList.add(FinalOrderPriceModel);
                            }
                            adapter.notifyDataSetChanged();
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("OrderStatus2", String.valueOf(error.toException()));
                        }
                    });
                }
            }
        });

        adapter = new CustomAdapter2(arrayList, this);
        recyclerView.setAdapter(adapter); //리사이클러뷰에 어댑터 연결

        //계좌 번호 복사 클릭
        iv_account_number_copy.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction()==MotionEvent.ACTION_DOWN) { //눌렀을 때 동작
                    String account_bank_copy_text = tv_bank_name.getText().toString();
                    String account_num_copy_text = account_number.replaceAll("-", ""); //계좌번호에서 "-" 제거

                    //클립보드 사용 코드
                    ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    ClipData clipData = ClipData.newPlainText("ID", account_bank_copy_text+" "+account_num_copy_text); //클립보드에 ID라는 이름표로 account_number 값을 복사하여 저장
                    clipboardManager.setPrimaryClip(clipData);

                    //복사가 되었다면 토스트 메시지 노출
                    Toast.makeText(OrderStatus2.this, "계좌번호가 복사되었습니다.", Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });

        //주문 완료 버튼 클릭
        OrderCompletedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //방장 정보 불러오기
                database.getReference().child("orderlist").child(roomId).child("boss").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if (!task.isSuccessful()) {
                            Log.e("firebase", "Error getting data", task.getException());
                        }
                        else { //boss uid 가져오기 성공
                            boss = String.valueOf(task.getResult().getValue()); //boss uid를 변수 boss에 저장

                            //현재 사용자가 방장인지 확인
                            if(boss.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                // 주문 완료 버튼 눌렀었는지 확인
                                database.getReference().child("orderlist").child(roomId).child("orderCompleted").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                                        if (!task.isSuccessful()) {
                                            Log.e("firebase", "Error getting data", task.getException());
                                        }
                                        else {
                                            order_completed = String.valueOf(task.getResult().getValue());
                                            if(order_completed.equals("Y")) {
                                                Toast.makeText(OrderStatus2.this, "주문 완료 버튼은 한 번만 누를 수 있습니다.", Toast.LENGTH_SHORT).show();
                                            }
                                            else {
                                                //파이어베이스 실시간 데이터베이스에 주문 완료 여부 N->Y로 변경
                                                Map<String, Object> map = new HashMap<>();
                                                map.put("orderCompleted", "Y");
                                                FirebaseDatabase.getInstance().getReference().child("orderlist").child(roomId).updateChildren(map);

                                                //누적 할인 배달비 - 방장
                                                FirebaseDatabase.getInstance().getReference().child("users").child(boss).child("discounted_delivery_fee").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                                                        if (!task.isSuccessful()) {
                                                            Log.e("firebase", "Error getting data", task.getException());
                                                        } else {
                                                            discounted_delivery_fee = Integer.parseInt(String.valueOf(task.getResult().getValue())); //방장의 누적 할인 배달비 DB에서 가져옴

                                                            //1인당 배달비 정보 가져오기
                                                            database.getReference().child("orderlist").child(roomId).child("per delivery fee").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<DataSnapshot> task) {
                                                                    if (!task.isSuccessful()) {
                                                                        Log.e("firebase", "Error getting data", task.getException());
                                                                    }
                                                                    //1인당 배달비 성공적으로 가져왔을 경우
                                                                    else {
                                                                        int boss_per_delivery_fee = Integer.parseInt(String.valueOf(task.getResult().getValue()));
                                                                        discounted_delivery_fee = discounted_delivery_fee + boss_per_delivery_fee;
                                                                        FirebaseDatabase.getInstance().getReference().child("users").child(boss).child("discounted_delivery_fee").setValue(discounted_delivery_fee);

                                                                        //LayoutInflater 객체 생성
                                                                        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                                                        View view = inflater.inflate(R.layout.tab_layout, null);
                                                                        dMoneyView = (TextView) view.findViewById(R.id.discounted_delivery_fee);
                                                                        dMoneyView.setText(String.valueOf(discounted_delivery_fee)+"원");
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    }
                                                });

                                                // 참여자 - 푸시 알림 전송 & 누적 할인 배달비
                                                FirebaseDatabase.getInstance().getReference().child("orderlist").child(roomId).child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        Map<String, Boolean> participationUsers = (Map<String, Boolean>)snapshot.getValue();

                                                        if (participationUsers == null || participationUsers.isEmpty()) {
                                                            Toast.makeText(OrderStatus2.this, "참여자가 없습니다.", Toast.LENGTH_SHORT).show();
                                                        }

                                                        else {
                                                            //1인당 배달비 가져오기
                                                            database.getReference().child("orderlist").child(roomId).child("per delivery fee").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<DataSnapshot> task) {
                                                                    if (!task.isSuccessful()) {
                                                                        Log.e("firebase", "Error getting data", task.getException());
                                                                    }
                                                                    //1인당 배달비 성공적으로 가져왔을 경우
                                                                    else {
                                                                        per_delivery_fee = Integer.parseInt(String.valueOf(task.getResult().getValue()));
                                                                    }
                                                                }
                                                            });

                                                            for(String item: participationUsers.keySet()) {
                                                                //누적 할인 배달비 - 참여자
                                                                FirebaseDatabase.getInstance().getReference().child("users").child(item).child("discounted_delivery_fee").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                                                                        if (!task.isSuccessful()) {
                                                                            Log.e("firebase", "Error getting data", task.getException());
                                                                        } else {
                                                                            discounted_delivery_fee2 = 0;
                                                                            discounted_delivery_fee2 = Integer.parseInt(String.valueOf(task.getResult().getValue())); //참여자의 누적 할인 배달비 DB에서 가져옴
                                                                            discounted_delivery_fee2 = discounted_delivery_fee2 + per_delivery_fee;
                                                                            FirebaseDatabase.getInstance().getReference().child("users").child(item).child("discounted_delivery_fee").setValue(discounted_delivery_fee2);
                                                                        }
                                                                    }
                                                                });

                                                                //참여자들에게 푸시 알림 전송
                                                                database.getReference().child("users").child(item).child("pushToken").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                                                                        if (!task.isSuccessful()) {
                                                                            Log.e("firebase", "Error getting data", task.getException());
                                                                        }
                                                                        else {
                                                                            String pushToken = String.valueOf(task.getResult().getValue());
                                                                            sendFCM(pushToken);
                                                                            Toast.makeText(OrderStatus2.this, "참여자들에게 푸시 알림을 보냈습니다.", Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    }
                                                                });
                                                            }
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                    }
                                                });

                                            }
                                        }
                                    }
                                });
                            }

                            //현재 사용자가 방장이 아니면 주문하기 버튼 누를 수 없음
                            else {
                                Toast.makeText(OrderStatus2.this, "권한이 없습니다.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
            }
        });

        reportBtn2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialogView = (View) View.inflate(OrderStatus2.this, R.layout.report_dialog, null);
                androidx.appcompat.app.AlertDialog.Builder dlg = new AlertDialog.Builder(OrderStatus2.this, R.style.MyAlertDialogStyle);
                dlg.setTitle("           <신고할 사용자 정보 입력>");
                dlg.setView(dialogView);

                radioGroup = (RadioGroup) dialogView.findViewById(R.id.radioGroup);
                linearLayout_report_reason_visibility = (LinearLayout) dialogView.findViewById(R.id.linearLayout_report_reason);
                report_reason = (EditText) dialogView.findViewById(R.id.report_reason);

                // 신고 유형 - 기타 누르면 신고 사유 visible
                report_type = (Spinner) dialogView.findViewById(R.id.report_list);
                ArrayAdapter adapter = ArrayAdapter.createFromResource(OrderStatus2.this, R.array.report_spinner_array, android.R.layout.simple_spinner_dropdown_item);

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

                        Toast.makeText(OrderStatus2.this, "신고되었습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
                dlg.setNegativeButton("취소", null);
                dlg.show();
            }
        });

        // 은행 버튼 관련
        bank_btn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                bankDialogView = (View) View.inflate(OrderStatus2.this, R.layout.bank_list, null);
                AlertDialog.Builder bankDlg = new AlertDialog.Builder(OrderStatus2.this, R.style.MyAlertDialogStyle);
                bankDlg.setView(bankDialogView);

                gv = (GridView) bankDialogView.findViewById(R.id.gridBank);
                MyGridAdapter gridAdapter = new MyGridAdapter();
                gv.setAdapter(gridAdapter);

                bankDlg.setPositiveButton("확인", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(OrderStatus2.this, "송금 완료했습니다", Toast.LENGTH_SHORT).show();
                    }
                });
                bankDlg.setNegativeButton("취소", null);
                bankDlg.show();
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
        notificationModel.data.title = "주문 완료!";
        notificationModel.data.text = "방장이 주문을 했습니다.";

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

    public boolean getPackageList(String pkgName) {
        boolean isExist = false;

        PackageManager pkgManager = getPackageManager();
        List<ResolveInfo> mApps;
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        mApps = pkgManager.queryIntentActivities(mainIntent, 0);

        try {
            for (int i = 0; i < mApps.size(); i++) {
                if(mApps.get(i).activityInfo.packageName.startsWith(pkgName)) {
                    isExist = true;
                    break;
                }
            }
        } catch (Exception e) {
            isExist = false;
        }

        return isExist;
    }

    // GirdView Adapter class
    public class MyGridAdapter extends BaseAdapter {
        Context context;

        public int getCount() {
            return bank.length;
        }

        public Object getItem(int arg0) {
            return null;
        }

        public long getItemId(int arg0){
            return 0;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            context = parent.getContext();

            if(convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.grid_item, parent, false);
            }


            ImageView bankLogo = convertView.findViewById(R.id.bank_logo);
            TextView bankName = convertView.findViewById(R.id.bank_name);

            bankLogo.setImageResource(bank[position]);
            bankName.setText(bankNames[position]);

            final String bankPkg = bankPackageNames[position];

            convertView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {

                    if (getPackageList(bankPkg)){
                        moveApp = getPackageManager().getLaunchIntentForPackage(bankPkg);
                        moveApp.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    } else {
                        String url = "market://details?id=" + bankPkg;
                        moveApp = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    }
                    startActivity(moveApp);
                }
            });


            return convertView;
        }
    }
}