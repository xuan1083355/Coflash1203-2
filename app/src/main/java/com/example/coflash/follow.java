package com.example.coflash;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.coflash.ui.main.Upload;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class follow extends Activity {

    private ImageButton back;
    private String uid;
    private int count=-1;
    private RecyclerView RV_tag;
    Context context = this,ncontext;
    List<String> classify_string=new ArrayList<>();
    List<String> tag_string=new ArrayList<>();
    public static String pos_classify;//上傳的東西(classify功能用)
    public static int status_classify;//參數狀態(classify功能用)
    DatabaseReference myRef2,myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow);

        back = findViewById(R.id.back);

        //連線資料庫
        FirebaseDatabase database = FirebaseDatabase.getInstance();//連線數據庫
        myRef = database.getReference("DB");
        myRef2 = myRef.child("push");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();//取得使用者資料
        if (user != null) {
            uid = user.getUid();//取得uid
        }

        //推播訊息RV_tag
        RV_tag=(RecyclerView)findViewById(R.id.RV_tag);
        LinearLayoutManager tag_layoutmanager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false){
            @Override
            public void onMeasure(@NonNull RecyclerView.Recycler recycler, @NonNull RecyclerView.State state, int widthSpec, int heightSpec) {
                int count = state.getItemCount();
                if(count > 0){
                    View view = recycler.getViewForPosition(0);
                    if(view != null){
                        measureChild(view, widthSpec, heightSpec);
                        int measureHeight = view.getMeasuredHeight();
                        int showHeight = measureHeight * state.getItemCount();
//                        if(state.getItemCount() >= 5){
//                            showHeight = measureHeight * 15;
//                        }
                        setMeasuredDimension(widthSpec, showHeight);
                    }
                }
                else{
                    super.onMeasure(recycler, state, widthSpec, heightSpec);
                }
            }
            @Override
            public boolean isAutoMeasureEnabled() {
                return false;
            }
        };
        RV_tag.setLayoutManager(tag_layoutmanager);
        RV_tag.setNestedScrollingEnabled(false);
        myRef.child("user").child(uid).child("tag").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    tag_string.add(String.valueOf(ds.getKey()));
                }
                setup_tag();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
    }
    public void setup_tag(){
        List<Integer> tag_num=new ArrayList<>();
        tag_num.add(0);
        FollowTagAdapter followtagadapter=new FollowTagAdapter(context,"follow",tag_num,tag_string,count,R.layout.tagtext_cardview,R.layout.show_follow_cardview);
        RV_tag.setAdapter(followtagadapter);

        followtagadapter.setOnItemClickListener(new FollowTagAdapter.OnItemClickListener() {
            @Override
            public void onClick(View view, int position) {
                recordfollow(position);
            }
        });
    }
    public void recordfollow(int position){
        pos_classify=tag_string.get(position);
        openClassifyTag();
        Home.status_classify=5;
    }
    private void openClassifyTag() {
        Intent intent = new Intent(this, ClassifyTag.class);
        startActivity(intent);
    }
}
