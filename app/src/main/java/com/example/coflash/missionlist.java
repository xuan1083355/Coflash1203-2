package com.example.coflash;

import android.app.Activity;


import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class missionlist extends Activity {

    private ImageButton back;
    Button btn_collect, btn_post, btn_thumb;
    private TextView tv_collect, tv_post, tv_thumb;

    String uid;
    DatabaseReference myRef, myRef2, myRef3;

    boolean heart[] = new boolean[2];
    boolean plus[] = new boolean[2];
    boolean thumb[] = new boolean[2];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_missionlist);

        back = findViewById(R.id.back);
        btn_collect = findViewById(R.id.btn_collect);
        btn_post = findViewById(R.id.btn_post);
        btn_thumb = findViewById(R.id.btn_thumb);
        tv_collect = findViewById(R.id.tv_collect);
        tv_post = findViewById(R.id.tv_post);
        tv_thumb = findViewById(R.id.tv_thumb);


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();//取得使用者資料
        if (user != null) {
            uid = user.getUid();//取得uid
        }

        //連線資料庫
        FirebaseDatabase database = FirebaseDatabase.getInstance();//連線數據庫
        myRef = database.getReference("DB");
        myRef2 = myRef.child("user").child(uid).child("mission");
        myRef3 = myRef.child("user").child(uid);

        firebase_select();

        btn_collect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                myRef3.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        long res_diamond =  (long) snapshot.child("diamond").getValue() + 5;
                        long res_experience = (long) snapshot.child("experience").getValue() + 5;
                        myRef3.child("diamond").setValue(res_diamond);
                        myRef3.child("experience").setValue(res_experience);
                        myRef2.child("heart").child("isTake").setValue(true);
                        btn_collect.setEnabled(false);
                        btn_collect.setText("已領取");
                        btn_collect.setBackgroundResource(R.drawable.btn_round_grayyellow);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }
        });
        btn_post.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                myRef3.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        long res_diamond =  (long) snapshot.child("diamond").getValue() + 5;
                        long res_experience = (long) snapshot.child("experience").getValue() + 5;
                        myRef3.child("diamond").setValue(res_diamond);
                        myRef3.child("experience").setValue(res_experience);
                        myRef2.child("plus").child("isTake").setValue(true);
                        btn_post.setEnabled(false);
                        btn_post.setText("已領取");
                        btn_post.setBackgroundResource(R.drawable.btn_round_grayyellow);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }
        });
        btn_thumb.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                myRef3.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        long res_diamond =  (long) snapshot.child("diamond").getValue() + 5;
                        long res_experience = (long) snapshot.child("experience").getValue() + 5;
                        myRef3.child("diamond").setValue(res_diamond);
                        myRef3.child("experience").setValue(res_experience);
                        myRef2.child("thumb").child("isTake").setValue(true);
                        btn_thumb.setEnabled(false);
                        btn_thumb.setText("已領取");
                        btn_thumb.setBackgroundResource(R.drawable.btn_round_grayyellow);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    //讀取資料方法
    private void firebase_select() {
        myRef2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                heart[0] = (boolean) snapshot.child("heart").child("isFinish").getValue();
                heart[1] = (boolean) snapshot.child("heart").child("isTake").getValue();
                plus[0] = (boolean) snapshot.child("plus").child("isFinish").getValue();
                plus[1] = (boolean) snapshot.child("plus").child("isTake").getValue();
                thumb[0] = (boolean) snapshot.child("thumb").child("isFinish").getValue();
                thumb[1] = (boolean) snapshot.child("thumb").child("isTake").getValue();
                if(heart[0]){
                    tv_collect.setVisibility(View.GONE);
                    btn_collect.setVisibility(View.VISIBLE);
                    if(heart[1]){
                        btn_collect.setEnabled(false);
                        btn_collect.setText("已領取");
                        btn_collect.setBackgroundResource(R.drawable.btn_round_grayyellow);
                    }
                }

                if(plus[0]){
                    tv_post.setVisibility(View.GONE);
                    btn_post.setVisibility(View.VISIBLE);
                    if(plus[1]){
                        btn_post.setEnabled(false);
                        btn_post.setText("已領取");
                        btn_post.setBackgroundResource(R.drawable.btn_round_grayyellow);
                    }
                }

                if(thumb[0]){
                    tv_thumb.setVisibility(View.GONE);
                    btn_thumb.setVisibility(View.VISIBLE);
                    if(thumb[1]){
                        btn_thumb.setEnabled(false);
                        btn_thumb.setText("已領取");
                        btn_thumb.setBackgroundResource(R.drawable.btn_round_grayyellow);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }


}