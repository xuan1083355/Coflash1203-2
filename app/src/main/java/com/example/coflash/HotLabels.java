package com.example.coflash;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.util.Log;

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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

public class HotLabels extends Activity {

    private ImageButton back;
    private TextView tag_num;
    private RecyclerView RV_tag;
    private String uid;
    int count=-1;
    Context context = this,ncontext;
    DatabaseReference myRef2,myRef;
    public List<String> tag_string=new ArrayList<>();
    public List<String> tag_string_sort=new ArrayList<>();
    public List<Integer> number=new ArrayList<>();
    public static String pos_classify;//上傳的東西(classify功能用)
    public static int status_classify;//參數狀態(classify功能用)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotlabels);
        back = findViewById(R.id.back);

        //連線資料庫
        FirebaseDatabase database = FirebaseDatabase.getInstance();//連線數據庫
        myRef = database.getReference("DB");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();//取得使用者資料
        if (user != null) {
            uid = user.getUid();//取得uid
        }

        //推播訊息RV_tag
        RV_tag=(RecyclerView)findViewById(R.id.RV_tag);
        RV_tag.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));

        myRef.child("tag").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    tag_string.add(String.valueOf(ds.getKey()));
                }
                sort_tag();
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
    public void sort_tag(){
        myRef.child("tag").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                } else {
                    for (int i = 0; i < tag_string.size(); i++) {
                        String num_suspend = tag_string.get(i);
                        int num = Integer.parseInt(String.valueOf(task.getResult().child(num_suspend).child("number of post").getValue()));//找到tag貼文數量
                        if (number.size() == 0) {
                            number.add(num);
                            tag_string_sort.add(num_suspend);
                        } else {
                            for(String s:tag_string_sort){
                                System.out.print(s+",");
                            }
                            for(int nn:number){
                                System.out.print(nn+",");
                            }
                            int a = number.size();
                            for (int j = a-1; j >= 0; j--) {
                                int n = j - 1;
                                if (j == 0) {
                                    n = 0;
                                }
                                if(num>number.get(a-1)){
                                    number.add(num);
                                    tag_string_sort.add(num_suspend);
                                    break;
                                }else if(num<number.get(0)){
                                    number.add(j, num);
                                    tag_string_sort.add(j, num_suspend);
                                    break;
                                }

                                if ((num >= number.get(n)) && (num <= number.get(j))) {
                                    number.add(j, num);
                                    tag_string_sort.add(j, num_suspend);
                                    break;
                                }
                            }
                        }
                    }
                }
                setup_tag();
            }
        });
    }
    public void setup_tag(){
        List<String> tag_string_return=new ArrayList<String>();
        List<Integer> tag_num=new ArrayList<>();
        for(int i=tag_string_sort.size()-1;i>=0;i--){
            tag_string_return.add(tag_string_sort.get(i));
        }
        for(int i=number.size()-1;i>=0;i--){
            tag_num.add(number.get(i));
        }
        FollowTagAdapter followtagadapter=new FollowTagAdapter(context,"Hot",tag_num,tag_string_return,count,R.layout.tagtext_cardview,R.layout.show_follow_cardview);
        RV_tag.setAdapter(followtagadapter);

        followtagadapter.setOnItemClickListener(new FollowTagAdapter.OnItemClickListener() {
            @Override
            public void onClick(View view, int position) {
                recordfollow(position,tag_string_return);
            }
        });
    }
    public void recordfollow(int position,List<String> tag_string_return){
        pos_classify=tag_string_return.get(position);
        openClassifyTag();
        Home.status_classify=9;
    }
    private void openClassifyTag() {
        Intent intent = new Intent(this, ClassifyTag.class);
        startActivity(intent);
    }
}
