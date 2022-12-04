package com.example.coflash;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.example.coflash.ui.main.Upload;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.security.SecureRandom;

import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.Calendar;
import java.util.Date;

public class Everyday extends Activity{
    private ImageView left,right,paper;
    private TextView sentence,tv_take;
    private ImageButton close;
    RelativeLayout RL_reward;
    DatabaseReference myRef;
    Uri uri;
    String uid;
    private FirebaseDatabase db;
    SecureRandom randomNumbers=new SecureRandom();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_everyday);

        left=(ImageView)findViewById(R.id.left);
        right = (ImageView)findViewById(R.id.right);
        paper = (ImageView)findViewById(R.id.paper);
        sentence = findViewById(R.id.sentence);
        close = (ImageButton)findViewById(R.id.close);
        tv_take = (TextView) findViewById(R.id.tv_take);
        RL_reward = (RelativeLayout) findViewById(R.id.RL_reward);
        byXml();
        System.out.println("時間1");


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();//取得使用者資料
        if (user != null) {
            uid = user.getUid();//取得uid
        }
        //連線資料庫
        FirebaseDatabase database = FirebaseDatabase.getInstance();//連線數據庫
        myRef = database.getReference("DB");
        firebase_update_date(myRef);
        int n=randomNumbers.nextInt(14)+1;
        String index=Integer.toString(n);

        myRef.child("sentence").child(index).addListenerForSingleValueEvent(new ValueEventListener() {//顯示的字
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                System.out.println("輸出的字"+snapshot.getValue());
                sentence.setText(String.valueOf(snapshot.getValue()));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                openClose();
            }
        });
    }
    private void byXml(){//動畫
        Animation animation_right=AnimationUtils.loadAnimation(this,R.anim.translate_right);
        animation_right.setFillAfter(true);
        right.startAnimation(animation_right);

        Animation animation_left=AnimationUtils.loadAnimation(this,R.anim.translate_left);
        animation_left.setFillAfter(true);
        left.startAnimation(animation_left);
    }
    private void firebase_update_date(DatabaseReference db) {
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try{
                    boolean isTake = (boolean) snapshot.child("user").child(uid).child("mission").child("everyday").child("isTake").getValue();
                    if(!isTake){
                        myRef.child("user").child(uid).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                            @Override
                            public void onComplete(Task<DataSnapshot> task) {
                                if (!task.isSuccessful()) {
                                    Log.e("firebase", "Error getting data", task.getException());
                                } else {
                                    int diamond=Integer.parseInt(String.valueOf(task.getResult().child("diamond").getValue()));//成功找到user_name
                                    myRef.child("user").child(uid).child("diamond").setValue(diamond+5);
                                    int experience=Integer.parseInt(String.valueOf(task.getResult().child("experience").getValue()));//成功找到user_name
                                    myRef.child("user").child(uid).child("experience").setValue(experience+5);
                                    myRef.child("user").child(uid).child("mission").child("everyday").child("isTake").setValue(true);
                                }
                            }
                        });
                    }else{
                        RL_reward.setVisibility(View.GONE);
                        tv_take.setVisibility(View.VISIBLE);
                    }
                }
                catch (Exception e){
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
    private static int differentDays(Date date1, Date date2){
        SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd");
        Calendar cal_past = Calendar.getInstance();
        Calendar cal_now = Calendar.getInstance();
        cal_past.setTime(date1);
        cal_now.setTime(date2);
        int day_past = cal_past.get(Calendar.DAY_OF_YEAR);
        int day_now = cal_now.get(Calendar.DAY_OF_YEAR);
        int year_past = cal_past.get(Calendar.YEAR);
        int year_now = cal_now.get(Calendar.YEAR);
        if (year_past < year_now){
            int timeDistance = 1;
            return timeDistance;
        }
        else if(year_past > year_now){
            int timeDistance = -1;
            return timeDistance;
        }else{
            return day_now-day_past;
        }
    }
    private void openClose() {
        Intent intent = new Intent(this, Home.class);
        startActivity(intent);
    }
}