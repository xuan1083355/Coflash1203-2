package com.example.coflash;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ImageView;


import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;

public class AddDiamond extends Activity {
    ListView listView;
    TextView diamond_number;
    ImageButton back;
    int[] amount = {50, 100, 150, 200, 500, 800, 1600, 3300};
    String[] addvalue = {"(首次儲值+50)", "(首次儲值+100)", "(首次儲值+150)", "(首次儲值+200)",
            "(首次儲值+500)", "(首次儲值+800)", "(首次儲值+1600)", "(首次儲值+3300)"};
    String[] price = {"NT$30", "NT$60", "NT$90", "NT$120", "NT$300", "NT$480", "NT$960", "NT$1920"};
    String uid;
    DatabaseReference myRef;
    long diamonds;
    boolean first_addvalue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adddiamond);

        listView = findViewById(R.id.listview);
        back = findViewById(R.id.back);
        diamond_number = findViewById(R.id.diamond_number);

        back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            uid = user.getUid();//取得uid
        }
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference("DB");
        myRef.child("user").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                diamonds = (long) snapshot.child("diamond").getValue();
                diamond_number.setText(String.valueOf(diamonds));
                try {
                    first_addvalue = (boolean)snapshot.child("first_addvalue").getValue();
                }
                catch (Exception e){
                    myRef.child("user").child(uid).child("first_addvalue").setValue(true);
                    first_addvalue = true;
                }
                DaimondAdapter daimondAdapter = new DaimondAdapter(AddDiamond.this, amount, addvalue, price, diamonds, first_addvalue, uid);
                listView.setAdapter(daimondAdapter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}