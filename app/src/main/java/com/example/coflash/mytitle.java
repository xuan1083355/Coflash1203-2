package com.example.coflash;

import android.app.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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


public class mytitle extends Activity {

    private ImageButton back;
    private RecyclerView RV_title;
    String uid;
    DatabaseReference myRef;
    public int[] title_num= {1,1,1,1,1};
    Context context = this,ncontext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mytitle);


        back = findViewById(R.id.back);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();//取得使用者資料
        if (user != null) {
            uid = user.getUid();//取得uid
        }
        //連線資料庫
        FirebaseDatabase database = FirebaseDatabase.getInstance();//連線數據庫
        myRef = database.getReference("DB");

        myRef.child("user").child(uid).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());//查詢使用者經驗
                } else {
                    if(task.getResult().child("alltitle")==null){
                        myRef.child("user").child(uid).child("alltitle").child("1").child("初心者").setValue("true");
                        myRef.child("user").child(uid).child("alltitle").child("2").child("勇者").setValue("false");
                        myRef.child("user").child(uid).child("alltitle").child("3").child("見習探險家").setValue("false");
                        myRef.child("user").child(uid).child("alltitle").child("4").child("大使").setValue("false");
                        myRef.child("user").child(uid).child("alltitle").child("5").child("冒險王").setValue("false");
                    }
                    int ex=Integer.parseInt(String.valueOf(task.getResult().child("experience").getValue()));
                    System.out.println("狀態3"+((String.valueOf(task.getResult().child("alltitle").child("3").child("見習探險家").getValue())).equals("false")));
                    if(ex>50 && ((String.valueOf(task.getResult().child("alltitle").child("2").child("勇者").getValue())).equals("false"))){//判別經驗是否達到稱號門檻並且使用者也未領取稱號
                        title_num[1]=2;//介面轉換成有button模式
                        System.out.println("觸發2");
                    }
                    if(ex>150 && ((String.valueOf(task.getResult().child("alltitle").child("3").child("見習探險家").getValue())).equals("false"))){//判別經驗是否達到稱號門檻並且使用者也未領取稱號
                        title_num[2]=2;//介面轉換成有button模式
                        System.out.println("觸發3");
                    }
                    if(ex>500 && ((String.valueOf(task.getResult().child("alltitle").child("4").child("大使").getValue())).equals("false"))){//判別經驗是否達到稱號門檻並且使用者也未領取稱號
                        title_num[3]=2;//介面轉換成有button模式
                    }
                    if(ex>1000 && ((String.valueOf(task.getResult().child("alltitle").child("5").child("冒險王").getValue())).equals("false"))){//判別經驗是否達到稱號門檻並且使用者也未領取稱號
                        title_num[5]=2;//介面轉換成有button模式
                    }
                }
                opentitle();
            }
        });
        myRef.child("user").child(uid).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());//查詢使用者經驗
                } else {
                    if(task.getResult().child("alltitle")==null){//如果資料庫沒有稱號資料，就新增
                        myRef.child("user").child(uid).child("alltitle").child("1").child("初心者").setValue("true");
                        myRef.child("user").child(uid).child("alltitle").child("2").child("勇者").setValue("false");
                        myRef.child("user").child(uid).child("alltitle").child("3").child("見習探險家").setValue("false");
                        myRef.child("user").child(uid).child("alltitle").child("4").child("大使").setValue("false");
                        myRef.child("user").child(uid).child("alltitle").child("5").child("冒險王").setValue("false");
                    }
                    int ex=Integer.parseInt(String.valueOf(task.getResult().child("experience").getValue()));
                    //篩選出使用者擁有的最大稱號並寫入資料庫title
                    if(ex>1000 && ((String.valueOf(task.getResult().child("alltitle").child("5").child("冒險王").getValue())).equals("true"))) {//判別經驗是否達到稱號門檻並且使用者已領取稱號
                        myRef.child("user").child(uid).child("title").setValue("冒險王");
                    }else if(ex>500 && ((String.valueOf(task.getResult().child("alltitle").child("4").child("大使").getValue())).equals("true"))){
                        myRef.child("user").child(uid).child("title").setValue("大使");
                    }else if(ex>150 && ((String.valueOf(task.getResult().child("alltitle").child("3").child("見習探險家").getValue())).equals("true"))){
                        myRef.child("user").child(uid).child("title").setValue("見習探險家");
                    }else if(ex>50 && ((String.valueOf(task.getResult().child("alltitle").child("2").child("勇者").getValue())).equals("true"))){
                        myRef.child("user").child(uid).child("title").setValue("勇者");
                    }else {

                    }
                }
            }
        });


        back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
    }
    public void opentitle(){//建立列表
        RV_title = (RecyclerView) findViewById(R.id.RV_title);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        RV_title.setLayoutManager(linearLayoutManager);

        MytitleAdapter mytitleadapter = new MytitleAdapter(context, title_num, R.layout.title_text_cardview, R.layout.title_button_cardview);
        RV_title.setAdapter(mytitleadapter);

        mytitleadapter.setOnItemClickListener(new MytitleAdapter.OnItemClickListener() {//按鈕且重跑頁面
            @Override
            public void onClick(View view, int position) {
                openMytitle();
            }
        });
    }
    private void openMytitle() {
        finish();
        Intent intent = new Intent(this, mytitle.class);
        startActivity(intent);
    }
    private void openMyinfo() {
        Intent intent = new Intent(this, Myinfo.class);
        startActivity(intent);
    }
}
