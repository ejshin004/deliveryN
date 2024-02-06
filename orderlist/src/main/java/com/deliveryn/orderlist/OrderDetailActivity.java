package com.deliveryn.orderlist;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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
import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OrderDetailActivity extends AppCompatActivity {
    OrderRoom selectedRoom;
    Button participateButton, orderStatusButton, chattingButton, removeRoomButton, completeButton, complete;
    View participateView, removeView;
    EditText delMenu, delOption, delPrice;
    //String menuVal, optVal;
    //int priceVal;
    String userId; // 로그인한 사용자 아이디를 받아와서 저장함
    String roomId, nickname, boss, order, bossName;
    int flag, people_num;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("orderlist");
    ChildEventListener mChild;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_room_page);

        getSelectedRoom();
        getBossName();
        setValues();
        participateButton = (Button) findViewById(R.id.participate);
        orderStatusButton = (Button) findViewById(R.id.ord_status);
        chattingButton = (Button) findViewById(R.id.chatting);
        removeRoomButton = (Button) findViewById(R.id.remove_room);
        completeButton = (Button) findViewById(R.id.complete_deliver);

        Intent intent = getIntent();
        roomId = intent.getStringExtra("roomId");

        // 삭제하기 버튼 클릭
        removeRoomButton.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v){
               removeView = (View) View.inflate(OrderDetailActivity.this, R.layout.remove_room, null);
               AlertDialog.Builder dlg = new AlertDialog.Builder(OrderDetailActivity.this, R.style.MyAlertDialogStyle);
               dlg.setTitle("주문 방 삭제");
               dlg.setView(removeView);
               dlg.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialogInterface, int i) {
                       FirebaseDatabase.getInstance().getReference().child("orderlist").child(roomId).child("boss").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                           @Override
                           public void onComplete(@NonNull Task<DataSnapshot> task) {
                               if(!task.isSuccessful()) { // 실패 (boss의 value를 가져옴)
                                   Log.e("firebase", "Error getting data", task.getException());
                               } else { // 성공 (boss의 value를 가져옴)
                                   boss = String.valueOf(task.getResult().getValue()); // boss uid를 변수 boss에 저장
                                   // 현재 로그인한 uid와 boss의 value(uid)를 비교 -> 방장이면 주문 방을 삭제할 수 있음
                                   if (boss.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) { // 방장이 삭제하기 버튼 클릭 -> 주문 방 삭제 가능
                                       FirebaseDatabase.getInstance().getReference().child("orderlist").child(roomId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                           @Override
                                           public void onSuccess(Void unused) {
                                               FirebaseDatabase.getInstance().getReference().child("Massages").child(roomId).removeValue();
                                               Toast.makeText(OrderDetailActivity.this, "주문 방 삭제 성공", Toast.LENGTH_SHORT).show();
                                               finish();
                                           };
                                       });
                                   } else { // 방장이 아닌 user가 삭제하기 버튼을 클릭 -> 주문 방 삭제 불가
                                       Toast.makeText(OrderDetailActivity.this, "삭제 권한이 없습니다.", Toast.LENGTH_SHORT).show();
                                   }

                               }
                           }
                       });
                   }
               });
               dlg.setNegativeButton("취소", null);
               dlg.show();
           }
        });

        // 참여하기 버튼 클릭
        participateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //방장 정보 가져오기
                FirebaseDatabase.getInstance().getReference().child("orderlist").child(roomId).child("boss").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if (!task.isSuccessful()) { //boss uid 가져오기 실패
                            Log.e("firebase", "Error getting data", task.getException());
                        } else { //boss uid 가져오기 성공
                            //참여하고 있는 user인지 체크
                            FirebaseDatabase.getInstance().getReference().child("orderlist").child(roomId).child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    flag = 0;
                                    if (snapshot.getValue() != null) {
                                        Map<String, Boolean> participationUsers = (Map<String, Boolean>) snapshot.getValue();
                                        if (participationUsers.keySet() != null) {
                                            for (String item : participationUsers.keySet()) {
                                                if (item.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                                    flag = 1; //방에 참여하고 있는 users에 자신의 uid가 있으면 flag를 1로 변경
                                                }
                                            }
                                        }
                                    }

                                    boss = String.valueOf(task.getResult().getValue()); // boss uid를 변수 boss에 저장

                                    // 방장이 참여하기 버튼 누른 경우
                                    if (boss.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                        Toast.makeText(OrderDetailActivity.this, "방장은 누를 수 없습니다.", Toast.LENGTH_SHORT).show();
                                    }

                                    // 방장이 아닌 user가 참여하기 버튼 누른 경우
                                    else {
                                        if (flag == 1) { // 이미 참여하고 있는 user인 경우
                                            Toast.makeText(OrderDetailActivity.this, "이미 참여하고 있습니다.", Toast.LENGTH_SHORT).show();
                                        } else { // 아직 참여하지 않은 uesr인 경우
                                            participateView = (View) View.inflate(OrderDetailActivity.this, R.layout.participate_user, null);
                                            androidx.appcompat.app.AlertDialog.Builder dlg = new AlertDialog.Builder(OrderDetailActivity.this, R.style.MyAlertDialogStyle);
                                            dlg.setView(participateView);

                                            dlg.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    delMenu = (EditText) participateView.findViewById(R.id.del_menu);
                                                    delOption = (EditText) participateView.findViewById(R.id.del_option);
                                                    delPrice = (EditText) participateView.findViewById(R.id.del_price);

                                                    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

                                                    FirebaseDatabase.getInstance().getReference().child("users").child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                            OrderInfoModel order = new OrderInfoModel();
                                                            UserModel user = dataSnapshot.getValue(UserModel.class);
                                                            nickname = user.getNickname();
                                                            order.setNickname(nickname);
                                                            order.setMenu(delMenu.getText().toString());
                                                            order.setOption(delOption.getText().toString());
                                                            order.setPrice(delPrice.getText().toString());
                                                            FirebaseDatabase.getInstance().getReference().child("orderlist").child(roomId).child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(order);
                                                            Toast.makeText(OrderDetailActivity.this, "입력 완료", Toast.LENGTH_SHORT).show();

                                                            // 인원 수 1 증가
                                                            database.getReference("orderlist").child(roomId).child("orderNum").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                    int value = (int) snapshot.getValue(Integer.class); //인원 수 가져옴
                                                                    value += 1; //인원 수 1 증가
                                                                    database.getReference("orderlist").child(roomId).child("orderNum").setValue(value); //저장
                                                                }

                                                                @Override
                                                                public void onCancelled(@NonNull DatabaseError error) {

                                                                }
                                                            });
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {
                                                            Toast.makeText(OrderDetailActivity.this, "error", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                }
                                            });
                                            dlg.setNegativeButton("취소", null);
                                            dlg.show();
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                }
                            });
                        }
                    }
                });
            }
        });

        // 채팅하기 버튼 클릭
        chattingButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                FirebaseDatabase.getInstance().getReference().child("orderlist").child(roomId).child("boss").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if (!task.isSuccessful()) { //boss uid 가져오기 실패
                            Log.e("firebase", "Error getting data", task.getException());
                        } else { //boss uid 가져오기 성공
                            myRef.child(roomId).child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    boolean flag = true;
                                    boss = String.valueOf(task.getResult().getValue()); // boss uid를 변수 boss에 저장
                                    if(boss.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                        Intent chatIntent = new Intent(OrderDetailActivity.this, ChatActivity.class);
                                        chatIntent.putExtra("roomId",roomId);
                                        flag = false;
                                        startActivity(chatIntent);
                                    }
                                    else {
                                        for (DataSnapshot child : snapshot.getChildren()){
                                            if(userId.equals(child.getKey())) {
                                                Intent chatIntent = new Intent(OrderDetailActivity.this, ChatActivity.class);
                                                chatIntent.putExtra("roomId",roomId);
                                                flag = false;
                                                startActivity(chatIntent);
                                                break;
                                            }
                                        }
                                    }
                                    // Log.e("chattt",snapshot.getChildren());
                                    if (flag){
                                        Toast.makeText(OrderDetailActivity.this, "권한이 없습니다!", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }

                    }
                });
            }
        });

        //주문 현황 버튼 클릭
        orderStatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 방장이 배달비까지 입력했는지 확인
                FirebaseDatabase.getInstance().getReference().child("orderlist").child(roomId).child("orderState").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if (!task.isSuccessful()) {
                            Log.e("firebase", "Error getting data", task.getException());
                        } else {
                            order = String.valueOf(task.getResult().getValue());
                            if (order.equals("before")) { // 방장이 배달비를 입력하지 않은 경우 OrderStatus1으로 이동
                                Intent roomId_intent = new Intent(OrderDetailActivity.this, OrderStatus1.class);
                                roomId_intent.putExtra("roomId", roomId);
                                startActivity(roomId_intent);
                            } else { // 방장이 배달비를 입력한 경우 OrderStatus2로 이동
                                Intent roomId_intent = new Intent(OrderDetailActivity.this, OrderStatus2.class);
                                roomId_intent.putExtra("roomId", roomId);
                                startActivity(roomId_intent);
                            }
                        }
                    }
                });
            }
        });

        //배달 완료 버튼 클릭
        completeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //방장 정보 가져오기
                FirebaseDatabase.getInstance().getReference().child("orderlist").child(roomId).child("boss").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if (!task.isSuccessful()) { //boss uid 가져오기 실패
                            Log.e("firebase", "Error getting data", task.getException());
                        } else { //boss uid 가져오기 성공
                            //참여하고 있는 user인지 체크
                            boss = String.valueOf(task.getResult().getValue()); // boss uid를 변수 boss에 저장
                            //배달 완료 푸시 알림
                            if (boss.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) { // 방장이 클릭한 경우
                                FirebaseDatabase.getInstance().getReference().child("orderlist").child(roomId).child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        Map<String, Boolean> participationUsers = (Map<String, Boolean>) snapshot.getValue();

                                        if (participationUsers == null || participationUsers.isEmpty()) {
                                            Toast.makeText(OrderDetailActivity.this, "참여자가 없습니다.", Toast.LENGTH_SHORT).show();
                                        }

                                        else {
                                            for (String item : participationUsers.keySet()) {
                                                database.getReference().child("users").child(item).child("pushToken").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                                                        if (!task.isSuccessful()) {
                                                            Log.e("firebase", "Error getting data", task.getException());
                                                        } else {
                                                            String pushToken = String.valueOf(task.getResult().getValue());
                                                            sendFCM(pushToken);
                                                            Toast.makeText(OrderDetailActivity.this, "참여자들에게 푸시 알림을 보냈습니다.", Toast.LENGTH_SHORT).show();
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
                            } else { // 방장이 아닌 user가 클릭한 경우
                                Toast.makeText(OrderDetailActivity.this, "권한이 없습니다.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
            }
        });
    }

    private void setValues() {
        TextView restName = (TextView) findViewById(R.id.r_name);
        TextView resCategory = (TextView) findViewById(R.id.r_category);
        TextView ordTime = (TextView) findViewById(R.id.od_time);
        TextView deliverLocation = (TextView) findViewById(R.id.del_loc);
        TextView deliverLink = (TextView) findViewById(R.id.del_link);
        deliverLink.setAutoLinkMask(Linkify.WEB_URLS); // 가게 링크 연결
        
        restName.setText(selectedRoom.resName);
        resCategory.setText("카테고리: " + selectedRoom.resCategory);
        ordTime.setText("주문 시간: " + selectedRoom.deliverTime);
        deliverLocation.setText("배달 장소: " + selectedRoom.deliverLocation);
        deliverLink.setText("가게 링크: " + selectedRoom.deliverLink);
    }

    private void getSelectedRoom() {
        Intent intent = getIntent();
        String id = intent.getStringExtra("roomId");
        String resName = intent.getStringExtra("resName");
        String resCategory = intent.getStringExtra("resCategory");
        String deliverTime = intent.getStringExtra("deliverTime");
        String deliverLocation = intent.getStringExtra("deliverLocation");
        String deliverLink = intent.getStringExtra("deliverLink");
        selectedRoom = new OrderRoom(id, resName, resCategory, deliverTime, deliverLocation, deliverLink);
        userId = intent.getStringExtra("participate");
    }

    private void getBossName() {
        FirebaseDatabase.getInstance().getReference().child("orderlist").child(selectedRoom.roomId).child("boss").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()){ // boss의 데이터 값 가져오기 실패
                    Log.e("Firebase", "Error getting data", task.getException());
                } else { // boss의 데이터 값 가져오기 성공
                    boss = String.valueOf(task.getResult().getValue()); // boss uid를 변수 boss에 저장
                    Log.d("boss id: ", boss);
                    FirebaseDatabase.getInstance().getReference().child("users").child(boss).child("nickname").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            if (!task.isSuccessful()) { // 실패
                                Log.e("Firebase", "Error getting data", task.getException());
                            } else { // 성공
                                bossName = String.valueOf(task.getResult().getValue());
                                Log.d("boss Nick Name: ", bossName);
                                TextView bossNickName = (TextView) findViewById(R.id.boss);
                                bossNickName.setText("방장: " + bossName);
                            }
                        }
                    });
                }
            }
        });
    }

    /*
    private void insertParticipate(String userId, String delMenu, String delOption, int delPrice) {
        participateUser user = new participateUser(userId, delMenu, delOption, delPrice);
        myRef.child(selectedRoom.roomId).child("participate").push().setValue(user);
    }
    */

    public class participateUser {
        private String participant, delMenu, delOption;
        private int delPrice;

        public participateUser() { } // Default Constructor
        public participateUser(String participant, String delMenu, String delOption, int delPrice) {
            this.participant = participant;
            this.delMenu = delMenu;
            this.delOption = delOption;
            this.delPrice = delPrice;
        }

        public String getParticipant() { return participant; }
        public String getDelMenu() { return delMenu; }
        public String getDelOption() { return delOption; }
        public int getDelPrice() { return delPrice; }
    }

    void sendFCM(String pushToken) {
        Gson gson = new Gson();

        NotificationModel notificationModel = new NotificationModel();
        notificationModel.to = pushToken;
        notificationModel.data.title = "배달 완료!";
        notificationModel.data.text = "배달 장소로 와주시길 바랍니다.";

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
