package com.example.coflash;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;
import java.util.ArrayList;
import android.app.SearchManager;

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

import androidx.appcompat.app.ActionBar;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

public class  Tag extends AppCompatActivity {

    private ImageButton home,tag,plus,collect,myinfo;
    private TextView allLabels,hotLabels;
    private String uid;
    private int count=0;
    private RecyclerView RV_classify,RV_tag;
    Context context = this,ncontext;
    List<String> classify_string=new ArrayList<>();
    List<String> tag_string=new ArrayList<>();
    public static String pos_classify;//上傳的東西(classify功能用)
    public static int status_classify;//參數狀態(classify功能用)
    DatabaseReference myRef2,myRef;
    public static String query_text;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag);

        //改變action bar顏色
        ActionBar actionBar;
        actionBar = getSupportActionBar();
        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#F3BA55"));
        actionBar.setBackgroundDrawable(colorDrawable);

        home = findViewById(R.id.under_home);
        tag = findViewById(R.id.under_tag);
        plus = findViewById(R.id.under_plus);
        collect = findViewById(R.id.under_collect);
        myinfo = findViewById(R.id.under_myinfo);
        allLabels = findViewById(R.id.allLabels);
        hotLabels = findViewById(R.id.hotLabels);

        //連線資料庫
        FirebaseDatabase database = FirebaseDatabase.getInstance();//連線數據庫
        myRef = database.getReference("DB");
        myRef2 = myRef.child("push");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();//取得使用者資料
        if (user != null) {
            uid = user.getUid();//取得uid
        }

        //橫向tag
        RV_classify=(RecyclerView)findViewById(R.id.RV_classify);
        //RV_classify.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        RV_classify.setLayoutManager(linearLayoutManager);
        classify_string.add("0");
        classify_string.add("1");
        classify_string.add("2");
        classify_string.add("3");
        classify_string.add("4");
        classify_string.add("5");
        classify_string.add("6");
        setup_classify();

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
        myRef.child("user").child(uid).child("tag_love").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    tag_string.add(String.valueOf(ds.getKey()));
                    count++;
                }
                tag_string.add("0");
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
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

        home.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                openHome();
            }
        });
        tag.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                openTag();
            }
        });
        plus.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                openPlus();
            }
        });
        collect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                openCollect();
            }
        });
        myinfo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                openMyinfo();
            }
        });
        allLabels.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                openAllLabels();
            }
        });
        hotLabels.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                openHotLabels();
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        MenuItem menuItem = menu.findItem(R.id.action_search);
        androidx.appcompat.widget.SearchView searchView = (androidx.appcompat.widget.SearchView) menuItem.getActionView();
        searchView.setQueryHint(getResources().getString(R.string.search_hint));
        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Tag.query_text=query;
                openSearch();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                /**調用RecyclerView內的Filter方法*/
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
        //return true;
    }
    public void setup_classify(){
        ClassifyAdapter classifyadapter=new ClassifyAdapter(context,classify_string,R.layout.classify_cardview,R.layout.tagsgroup_cardview);
        RV_classify.setAdapter(classifyadapter);

        classifyadapter.setOnItemClickListener(new ClassifyAdapter.OnItemClickListener() {
            @Override
            public void onClick(View view, int position) {
                record(position);
            }
        });
    }
    public void record(int position){
        switch(position){
            case 0:
                pos_classify="食";
                openClassify();
                break;
            case 1:
                pos_classify="衣";
                openClassify();
                break;
            case 2:
                pos_classify="住";
                openClassify();
                break;
            case 3:
                pos_classify="行";
                openClassify();
                break;
            case 4:
                pos_classify="育";
                openClassify();
                break;
            case 5:
                pos_classify="樂";
                openClassify();
                break;
            case 6:
                pos_classify="其它";
                openClassify();
                break;
            default:
                pos_classify="找不到";
        }
        Home.status_classify=5;
    }
    public void setup_tag(){
        List<Integer> tag_num=new ArrayList<>();
        tag_num.add(0);
        FollowTagAdapter followtagadapter=new FollowTagAdapter(context,"Tag",tag_num,tag_string,count,R.layout.tagtext_cardview,R.layout.show_follow_cardview);
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
    private void openSearch() {
        Intent intent = new Intent(this, SearchResultsActivity.class);
        startActivity(intent);
    }
    private void openHome(){
//        Intent intent=new Intent(this, Home.class);
        finish();
//        startActivity(intent);
    }
    private void openTag(){
//        Intent intent=new Intent(this,Tag.class);
//        startActivity(intent);
    }
    private void openPlus(){
        Intent intent=new Intent(this,Plus.class);
        finish();
        startActivity(intent);
    }
    private void openCollect(){
        Intent intent=new Intent(this,Collect.class);
        finish();
        startActivity(intent);
    }
    private void openMyinfo() {
        Intent intent = new Intent(this, Myinfo.class);
        finish();
        startActivity(intent);
    }
    private void openClassify() {
        Intent intent = new Intent(this, Classify.class);
        startActivity(intent);
    }
    private void openClassifyTag() {
        Intent intent = new Intent(this, ClassifyTag.class);
        startActivity(intent);
    }
    private void openAllLabels() {
        Intent intent = new Intent(this, AllLabels.class);
        startActivity(intent);
    }
    private void openHotLabels() {
        Intent intent = new Intent(this, HotLabels.class);
        startActivity(intent);
    }
}