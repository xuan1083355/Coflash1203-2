package com.example.coflash;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class NoticeSetting extends Activity {

    private ImageButton back;
    private Switch switch_distance, switch_tag, switch_time;
    DatabaseReference myRef, myRef2;
    String uid;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_noticesetting);
        back = findViewById(R.id.back);
        switch_distance = findViewById(R.id.switch_distance);
        switch_tag = findViewById(R.id.switch_tag);
        switch_time = findViewById(R.id.switch_time);

        FirebaseDatabase database = FirebaseDatabase.getInstance();//連線數據庫
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser(); //取得使用者資料
        if (user != null) {
            uid = user.getUid();//取得uid
        }
        myRef = database.getReference("DB");
        myRef2 = myRef.child("user").child(uid).child("NoticeSetting");
        //11-8
        myRef2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child("distance").getValue() != null){
                    boolean distance = (boolean) snapshot.child("distance").getValue();
                    switch_distance.setChecked(distance);
                }
                else{
                    myRef2.child("distance").setValue(true);
                }
                if(snapshot.child("tag").getValue() != null){
                    boolean tag = (boolean) snapshot.child("tag").getValue();
                    switch_tag.setChecked(tag);
                }
                else{
                    myRef2.child("tag").setValue(true);
                }
                if(snapshot.child("time").getValue() != null){
                    boolean time = (boolean) snapshot.child("time").getValue();
                    switch_time.setChecked(time);
                }
                else{
                    myRef2.child("time").setValue(true);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        switch_distance.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                myRef2.child("distance").setValue(isChecked);
            }
        });

        switch_tag.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                myRef2.child("tag").setValue(isChecked);
            }
        });

        switch_time.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                myRef2.child("time").setValue(isChecked);
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

    }
    private void openMyinfo() {
        Intent intent = new Intent(this, Myinfo.class);
        startActivity(intent);
    }

}