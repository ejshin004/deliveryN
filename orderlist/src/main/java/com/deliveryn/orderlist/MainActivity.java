package com.deliveryn.orderlist;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends TabActivity {
    ImageButton create_button;
    View dialogView;
    TextView dMoneyView;
    String categoryValue, timeValue, nameValue, locValue, linkValue, hourValue, minValue;
    Spinner sCategory;
    TimePicker ordTime;
    EditText sName, ordLoc, ordLink;
    ListView bList, aList;
    String userId, dMoney;


    CustomAdapter beforeAdapter, afterAdapter;
    public static ArrayList<OrderRoom> beforeList = new ArrayList<OrderRoom>();
    public static ArrayList<OrderRoom> afterList = new ArrayList<OrderRoom>();

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    DatabaseReference myRef = database.getReference("orderlist");
    DatabaseReference users = database.getReference("users");  //추가
    ChildEventListener mChild;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tab_layout);

        //Intent intent = getIntent();
        //userId = intent.getStringExtra("userId");
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        getFirebaseMessagingToken(); //pushToken 발급

        TabHost tabHost = getTabHost();
        create_button = (ImageButton) findViewById(R.id.create);

        TabHost.TabSpec tabOrderBefore = tabHost.newTabSpec("Before").setIndicator("주문 전");
        tabOrderBefore.setContent(R.id.OrderBefore);
        tabHost.addTab(tabOrderBefore);

        TabHost.TabSpec tabOrderAfter = tabHost.newTabSpec("After").setIndicator("주문 후");
        tabOrderAfter.setContent(R.id.OrderAfter);
        tabHost.addTab(tabOrderAfter);

        tabHost.setCurrentTab(0);

        bList = (ListView) findViewById(R.id.listViewBefore);
        aList = (ListView) findViewById(R.id.listViewAfter);

        initDB();

        beforeAdapter = new CustomAdapter(this, new ArrayList<OrderRoom>());
        bList.setAdapter(beforeAdapter);
        afterAdapter = new CustomAdapter(this, new ArrayList<OrderRoom>());
        aList.setAdapter(afterAdapter);



        create_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialogView = (View) View.inflate(MainActivity.this, R.layout.create_order, null);
                AlertDialog.Builder dlg = new AlertDialog.Builder(MainActivity.this, R.style.MyAlertDialogStyle);
                dlg.setTitle("주문 방 생성");
                dlg.setView(dialogView);
                dlg.setPositiveButton("생성하기",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                sName = (EditText) dialogView.findViewById(R.id.s_name); // 가게명
                                nameValue = sName.getText().toString();
                                ordTime = (TimePicker) dialogView.findViewById(R.id.ord_time); // 배달 시간
                                hourValue = Integer.toString(ordTime.getHour()); // 수정
                                minValue = Integer.toString(ordTime.getMinute()); // 수정
                                timeValue = hourValue + ":" + minValue;
                                ordLoc = (EditText) dialogView.findViewById(R.id.location); // 배달 위치
                                locValue = ordLoc.getText().toString();
                                sCategory = (Spinner) dialogView.findViewById(R.id.category_list); // 카테고리
                                categoryValue = sCategory.getSelectedItem().toString();
                                ordLink = (EditText) dialogView.findViewById(R.id.delivery_link); // 배달 링크
                                linkValue = ordLink.getText().toString();

                                insertOrderRoom(nameValue, timeValue, locValue, categoryValue, linkValue);
                            }
                        });
                dlg.setNegativeButton("취소", null);
                dlg.show();
            }
        });

        // 할인 받은 배달비 금액 띄우기
        users.child(currentUser.getUid()).child("discounted_delivery_fee").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()){
                    Log.e("firebase", "Error getting data", task.getException());
                } else{
                    dMoney = String.valueOf(task.getResult().getValue());
                    dMoneyView = findViewById(R.id.discounted_delivery_fee);
                    dMoneyView.setText(dMoney+"원");
                }
            }
        });


        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                afterAdapter.clear();
                beforeAdapter.clear();

                for (DataSnapshot post: snapshot.getChildren()) {
                    String roomKey = post.child("roomId").getValue().toString();
                    String rName = post.child("resName").getValue().toString();
                    String rCategory = post.child("resCategory").getValue().toString();
                    String dTime = post.child("deliverTime").getValue().toString();
                    String dLocation = post.child("deliverLocation").getValue().toString();
                    String dLink = post.child("deliverLink").getValue().toString();
                    String oState = post.child("orderState").getValue().toString();

                    // 주문 방 자동 삭제 함수 사용 (3시간이 지나면 자동 삭제)
                    boolean pass = removeOrderRoom(roomKey, dTime, oState);
                    if (pass) { continue; }

                    OrderRoom oRoomVal = new OrderRoom(roomKey, rName, rCategory, dTime, dLocation, dLink, oState);

                    if (oState.equals("before")){
                        beforeList.add(oRoomVal);
                        beforeAdapter.add(oRoomVal);
                    } else {
                        afterList.add(oRoomVal);
                        afterAdapter.add(oRoomVal);
                    }
                }
                beforeAdapter.notifyDataSetChanged();
                afterAdapter.notifyDataSetChanged();
                bList.setSelection(beforeAdapter.getCount() - 1);
                aList.setSelection(afterAdapter.getCount() - 1);

            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });

        // 탭 화면에 처음 들어갔을 때 주문 전 탭이 선택되어 있음 -> 주문 전 목록을 연결
        setUpOnClickListener(bList);
        // 탭 메뉴 변경 이벤트 발생 시 연결되는 listView를 다르게 함
        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener(){
            @Override
            public void onTabChanged(String tabId){
                String tabStr = tabId;
                if (tabStr.equals("Before")) {
                    setUpOnClickListener(bList);
                } else {
                    setUpOnClickListener(aList);
                }
            }
        });

    }

    public void insertOrderRoom (String nm, String tm, String loc, String cg, String ln) {
        OrderRoom orderRoom = new OrderRoom(nm, tm, loc, cg, ln);
        //myRef.push().setValue(orderRoom);
        DatabaseReference pushRef = myRef.push();
        String roomId = pushRef.getKey();
        orderRoom.setId(roomId);
        orderRoom.setBoss(userId);
        myRef.child(roomId).setValue(orderRoom);
    }

    // 주문 방 자동 삭제 함수
    private boolean removeOrderRoom (String roomId, String time, String state){
        LocalTime now = LocalTime.now();
        int nHour, hour;
        int afterHour; // 주문 후 목록에서 주문 방 자동 삭제 기준 시간 (3시간 설정)

        nHour = now.getHour();
        if(time.indexOf(":") == 1){ // hour의 값이 1자리인 경우
            hour = Integer.parseInt(time.substring(0, 1));
        } else { // hour의 값이 2자리인 경우
            hour = Integer.parseInt(time.substring(0, 2));
        }


        if (hour + 3 < 24){
            afterHour = hour + 3;
        } else {
            afterHour = hour + 3 - 24;
        }

        if (state.equals("after") && nHour >= afterHour && nHour < hour){
            // 주문 방 완료 목록 중에서 배달 시간에서 3시간이 경과한 후의 주문 방을 자동 삭제함
            myRef.child(roomId).removeValue();
            FirebaseDatabase.getInstance().getReference().child("Massages").child(roomId).removeValue();
            return true;
        }

        return false;
    }

    private void setUpOnClickListener(ListView currentList) {
        // 상세 페이지 이벤트
        currentList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                OrderRoom selectRoom = (OrderRoom) currentList.getItemAtPosition(position);
                Intent showDetail = new Intent(getApplicationContext(), OrderDetailActivity.class);
                showDetail.putExtra("roomId", selectRoom.roomId);
                showDetail.putExtra("resName", selectRoom.resName);
                showDetail.putExtra("resCategory", selectRoom.resCategory);
                showDetail.putExtra("deliverTime", selectRoom.deliverTime);
                showDetail.putExtra("deliverLocation", selectRoom.deliverLocation);
                showDetail.putExtra("deliverLink", selectRoom.deliverLink);
                showDetail.putExtra("participate", userId);
                startActivity(showDetail);
            }
        });
    }

    private void initDB() {
        mChild = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        myRef.addChildEventListener(mChild);
    }

    void getFirebaseMessagingToken ( ) {
        FirebaseMessaging.getInstance ().getToken ()
                .addOnCompleteListener ( task -> {
                    if (!task.isSuccessful ()) {
                        //Could not get FirebaseMessagingToken
                        return;
                    }
                    if (null != task.getResult ()) {
                        //Got FirebaseMessagingToken
                        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        String token = Objects.requireNonNull ( task.getResult () );
                        Map<String, Object> map = new HashMap<>();
                        map.put("pushToken", token);
                        FirebaseDatabase.getInstance().getReference().child("users").child(uid).updateChildren(map);
                    }
                } );
    }

}