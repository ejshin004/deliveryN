package com.deliveryn;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.renderscript.Sampler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private String email,nick;
    private EditText nickname;
    private Button complete;
    private GoogleSignInClient mGoogleSignInClient;
    private static final String TAG = "GoogleTest";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nickname);


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if(account != null){
            email = account.getEmail();
        }
        complete = (Button) findViewById(R.id.setNickNameComplete);

        complete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nickname = findViewById(R.id.setNickNameId);
                nick = nickname.getText().toString();
                if(email.getBytes().length <= 0 || nick.length() <= 0) {
                    Toast.makeText(SignupActivity.this, "회원가입 실패", Toast.LENGTH_SHORT).show();
                    Log.e(TAG,nick);
                }
                else {
                        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                        UserModel account = new UserModel();
                        account.setUid(firebaseUser.getUid());
                        account.setEmailId(email);
                        account.setNickname(nick);
                        account.setDiscounted_delivery_fee(0);

                        FirebaseDatabase.getInstance().getReference().child("users").child(firebaseUser.getUid()).setValue(account);

                        // update DisplayName
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(nick)
                                .build();

                        user.updateProfile(profileUpdates)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.d("TAG", "User profile updated.");
                                        }
                                    }
                                });

                        Toast.makeText(SignupActivity.this, "로그인 성공", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(SignupActivity.this, com.deliveryn.orderlist.MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
            }
        });
    }
}