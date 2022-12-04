package com.example.coflash;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Button;
import android.app.SearchManager;
import android.net.Uri;
import android.widget.Toast;
import android.util.Log;
import android.util.DisplayMetrics;
import android.graphics.Paint;
import android.graphics.Canvas;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.bumptech.glide.Glide;
import com.example.coflash.ui.main.Upload;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.StorageReference;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

public class SearchResultsActivity extends AppCompatActivity{

    private RecyclerView RV,RV_search;
    private FirebaseDatabase db;
    private Button tagsGroup;
    Context context = this,ncontext;
    int x_last = 0;
    private Object List;
    TextView id,tv1,tv2,tv3; //記得宣告，不然layout會找不到，字就顯示不出來
    ImageView imageView,good_view,bad_view;
    //讀取圖片
    Uri uri;
    String data_list,uid;
    StorageReference storageReference,pic_storage;
    int PICK_CONTACT_REQUEST=1;
    DatabaseReference myRef, myRef2, myRef3;
    public static final float ALPHA_FULL = 1.0f;
    String plusid;
    List<String> classify_string=new ArrayList<>();
    public static String pos_classify;//上傳的東西(classify功能用)
    public static int status_classify;//參數狀態(classify功能用)

    ImageAdapter imageAdapter;

    //1026 地址 78~83
    LatLng latLng, currentLatLng;
    String str1;
    //Geocoder geocoder;
    private static final int ACTIVITY_REPORT = 1000;
    private static final int ACTIVITY_REPORT_2 = 500;
    //1110
    private CountDownTimer countDownTimer; //推播時長倒數器
    private PopupWindow popupWindow;
    private ImageButton heart_popup; //1003
    private ImageView photo,good_bold,bad_bold;    //1003
    private TextView push_title,user_name,addr,push_context,user_title,goodnumber,badnumber,countdownTime, push_distance; //1021
    private String goodnum,badnum;//取得貼文讚數、倒讚數
    private List<Map<String,Object>> items=new ArrayList<Map<String,Object>>();
    private RecyclerView tagRv;
    private PopupWindowAdapter popupWindowAdapter;
    private TextView activity_date; //活動日期(不含時間)
    private TextView activity_wholeTime;  //活動日期+時間
    private static int leavePosition;  //紀錄懸浮視窗最後停留的貼文位置

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        //改變action bar顏色
        ActionBar actionBar;
        actionBar = getSupportActionBar();
        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#F3BA55"));
        actionBar.setBackgroundDrawable(colorDrawable);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //連線資料庫
        FirebaseDatabase database = FirebaseDatabase.getInstance();//連線數據庫
        myRef = database.getReference("DB");
        myRef2 = myRef.child("push");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();//取得使用者資料
        if (user != null) {
            uid = user.getUid();//取得uid
        }

        //橫向tag
        RV_search=(RecyclerView)findViewById(R.id.RV_classify);
        //RV_classify.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        RV_search.setLayoutManager(linearLayoutManager);
        myRef.child("tag").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String s=String.valueOf(ds.getKey());
                    String data_text=s.toLowerCase();//資料庫的title
                    String[] data_text_split=data_text.split("");
                    String query_text=Tag.query_text.toLowerCase();//query的title
                    String[] query_text_split=query_text.split("");
                    for(int i=0;i<data_text_split.length;i++){//abc
                        if(data_text_split[i].equals(query_text_split[0])){//b
                            int k=i;//1
                            int j=0;
                            boolean status=true;
                            while((k<data_text_split.length) && (j<query_text_split.length)) {
                                if(data_text_split[k].equals(query_text_split[j])){
                                    k++;
                                    j++;
                                }else{
                                    status=false;
                                    break;
                                }
                                if(j>=query_text_split.length){//5
                                    break;
                                }
                                if(k>=data_text_split.length){//5
                                    status=false;
                                    break;
                                }
                            }
                            if(status==true){
                                classify_string.add(s);
                                break;
                            }
                        }
                    }
                }
                setup_classify();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        //推播訊息RV
        RV=(RecyclerView)findViewById(R.id.RV);
        RV.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));

        id=(TextView)findViewById(R.id.id);
        tv1=(TextView)findViewById(R.id.tv1);
        tv3=(TextView)findViewById(R.id.tv3);
        imageView=(ImageView)findViewById(R.id.imageView);
        good_view=(ImageView)findViewById(R.id.good);
        bad_view=(ImageView)findViewById(R.id.bad);

        //heart_view=(ImageView)findViewById(R.id.heart);

        firebase_select(myRef);

        /*back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });*/
        setup_classify();
        initPopupWindow(); //1110
    }
    //初始化PopupWindow(懸浮視窗)
    private void initPopupWindow() {
        View view = LayoutInflater.from(context) .inflate(R.layout.popupwindow_layout, null);
        popupWindow = new PopupWindow(view);
        popupWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        heart_popup=(ImageButton)view.findViewById(R.id.heart_popup) ;  //1003
        good_bold=(ImageView)view.findViewById(R.id.good_bold);
        bad_bold=(ImageView)view.findViewById(R.id.bad_bold);
        photo=(ImageView) view.findViewById(R.id.photo);
        user_name=(TextView) view.findViewById(R.id.user_name);
        addr=(TextView)view.findViewById(R.id.addr);
        push_context=(TextView) view.findViewById(R.id.push_context);
        push_title= (TextView) view.findViewById(R.id.push_title);
        tagRv=(RecyclerView) view.findViewById(R.id.tagRv);
        user_title=(TextView) view.findViewById(R.id.user_title);
        goodnumber=(TextView)view.findViewById(R.id.goodnumber); //1021
        badnumber=(TextView) view.findViewById(R.id.badnumber);
        countdownTime=(TextView)view.findViewById(R.id.countdownTime);
        push_distance = (TextView)view.findViewById(R.id.push_distance);
        activity_date=(TextView) view.findViewById(R.id.activity_date);
//        activity_wholeTime=(TextView) view.findViewById(R.id.activity_wholeTime);

        popupWindow.setTouchable(true);
        popupWindow.setFocusable(true); //點一下消失，聚焦於popupWindow的操作
        popupWindow.setOutsideTouchable(true);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {  //popupWindow關閉後，背景顏色恢復
            @Override
            public void onDismiss() {
                darkenBackground(1f);
                RV.setAdapter(imageAdapter);   //懸浮式窗關閉後，重新更新RV的資料，讓在懸浮視窗點擊的愛心、讚同步更新到頁面  //1110
                recyclerViewAction(RV, items, imageAdapter);
                ((LinearLayoutManager)RV.getLayoutManager()).scrollToPositionWithOffset(leavePosition,0); //回到剛剛停留的貼文位置
            }
        });


    }

    //彈出PopupWindow後讓背景變暗
    private void darkenBackground(float bgcolor){
        WindowManager.LayoutParams lp=this.getWindow().getAttributes();
        lp.alpha=bgcolor;
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        this.getWindow().setAttributes(lp);
    }

    List<String> tags=new ArrayList<>();    //tag list
    String tagStr;
    private  float downX,downY=0;
    private float moveX,moveY=0;
    private  long currentMs,moveTime=0;
    private float lastX=0,lastY=0; //1003

    //懸浮窗出現
    private void showPopupWindow(int position){
        darkenBackground(0.5f); //背景變暗
        try {
            items.get(position);
            Glide.with(context).load(items.get(position).get("url")).centerCrop().into(photo);
            //Glide.with(context).load(items.get(position).get("url")).apply(RequestOptions.bitmapTransform(new RoundedCorners(30))).into(photo);
            push_title.setText(items.get(position).get("title").toString());    //推播標題
            user_name.setText(items.get(position).get("name").toString()); //使用者名稱
            addr.setText(items.get(position).get("pushplace").toString());  //推播地點
            push_context.setText(items.get(position).get("push_context").toString());   //推播內文
            user_title.setText(items.get(position).get("user_title").toString());
            push_distance.setText(items.get(position).get("distance").toString());
            //tag
            tags = new ArrayList<>();
            tagStr = items.get(position).get("tag").toString();
            for (String s : tagStr.split("#")) {
                if (!(s.equals("")) && !(s.equals(" "))) //不為空時
                    tags.add(s.trim());
            }
            //1025 325~329
            String activityDate = items.get(position).get("activityStartDate") + "～" + items.get(position).get("activityEndDate");
            if (items.get(position).get("activityStartDate").equals(items.get(position).get("activityEndDate")))
                activityDate = items.get(position).get("activityStartDate") + " 一日快閃限定"; //1026珵
            activity_date.setText(activityDate);

//            activity_wholeTime.setText("活動時間:\n" + items.get(position).get("activityWholeTime").toString());

            popupWindowAdapter = new PopupWindowAdapter(context, tags);
            tagRv.setAdapter(popupWindowAdapter);
            popupWindowAdapter.setOnItemClickListener(new PopupWindowAdapter.OnItemClickListener() {
                @Override
                public void onClick(View view, String tagtext) {
                    recordwindowtag(tagtext);
                }
            });

            String plusid = (String) items.get(position).get("id");
            //檢查是不是已經收藏
            myRef.child("user").child(uid).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(Task<DataSnapshot> task) {
                    if (!task.isSuccessful()) {
                        Log.e("firebase", "Error getting data", task.getException());
                    } else {
                        if (task.getResult().child("collect_id").child(plusid).getValue() != null) {//代表之前這個uid收藏了這則貼文
                            Glide.with(SearchResultsActivity.this).load(R.drawable.heart_popupcolor).into(heart_popup);
                        } else {//未收藏
                            Glide.with(SearchResultsActivity.this).load(R.drawable.heart_popup).into(heart_popup);
                        }
                    }
                }
            });
            //1024
            final long[] push_length = {(long) items.get(position).get("push_Length")};
            final long[] push_timepoint = {(long) items.get(position).get("push_Timepoint")};
            final long[] push_endtime = {(long) push_timepoint[0] + push_length[0]};
            Date dt = new Date();
            countdown(push_timepoint[0], dt.getTime(),push_endtime[0]);

            //plusid-貼文id；確認是按讚、倒讚、還是沒按過
            myRef.child("push").child(plusid).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(Task<DataSnapshot> task) {
                    if (!task.isSuccessful()) {
                        Log.e("firebase", "Error getting data", task.getException());
                    } else {  //三種情形:按讚、倒讚、沒按過讚&倒讚
                        //1021
                        goodnum = String.valueOf(task.getResult().child("goodnumber").getValue());//取得貼文讚的數量
                        badnum = String.valueOf(task.getResult().child("badnumber").getValue());//取得貼文讚的數量
                        //貼文按讚數
                        if (!goodnum.equals("null") && Integer.parseInt(goodnum) >= 0)
                            goodnumber.setText(goodnum);
                        else
                            goodnumber.setText("0");
                        //貼文倒讚數
                        if (!badnum.equals("null") && Integer.parseInt(badnum) >= 0)
                            badnumber.setText(badnum);
                        else
                            badnumber.setText("0");

                        if (task.getResult().child("baduid").child(uid).getValue() != null) {//這個uid之前已按過倒讚了
                            Glide.with(SearchResultsActivity.this).load(R.drawable.good_bold).into(good_bold);
                            Glide.with(SearchResultsActivity.this).load(R.drawable.bad_color).into(bad_bold);
                        } else if (task.getResult().child("gooduid").child(uid).getValue() != null) {  //按讚
                            Glide.with(SearchResultsActivity.this).load(R.drawable.good_color).into(good_bold);
                            Glide.with(SearchResultsActivity.this).load(R.drawable.bad_bold).into(bad_bold);
                        } else {    //沒有按過讚&倒讚
                            Glide.with(SearchResultsActivity.this).load(R.drawable.bad_bold).into(bad_bold);
                            Glide.with(SearchResultsActivity.this).load(R.drawable.good_bold).into(good_bold);
                        }
                    }
                }
            });
            //點擊按讚
            good_bold.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    isAwesome(plusid);
                }
            });
            //點擊按倒讚
            bad_bold.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    isNotAwesome(plusid);
                }
            });
            //點擊收藏
            heart_popup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    myRef.child("user").child(uid).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(Task<DataSnapshot> task) {
                            if (!task.isSuccessful()) {
                                Log.e("firebase", "Error getting data", task.getException());
                            } else {
                                if (task.getResult().child("collect_id").child(plusid).getValue() != null) {//代表之前這個uid收藏了這則貼文
                                    Glide.with(SearchResultsActivity.this).load(R.drawable.heart_popup).into(heart_popup);
                                    myRef.child("user").child(uid).child("collect_id").child(plusid).removeValue();//移除紀錄
                                } else {//未收藏
                                    Glide.with(SearchResultsActivity.this).load(R.drawable.heart_popupcolor).into(heart_popup);
                                    myRef.child("user").child(uid).child("collect_id").child(plusid).setValue("0");//存進資料庫
                                    myRef.child("user").child(uid).child("mission").child("heart").child("isFinish").setValue(true);
                                }
                            }
                        }
                    });
                }
            });
            popupWindow.setTouchInterceptor(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:   //手指的初次觸摸
                            downX = event.getX();
                            downY = event.getY();
                            currentMs = System.currentTimeMillis();
                            lastX = event.getX();
                            lastY = event.getY();
                            break;
                        case MotionEvent.ACTION_MOVE:   //手指滑動
                            downX = event.getX();
                            downY = event.getY();
                            moveX += Math.abs(event.getRawX() - downX);//x軸移動距離
                            moveY += Math.abs(event.getRawY() - downY);//y軸移動距離
                            break;
                        case MotionEvent.ACTION_UP: //抬起
                            moveTime = System.currentTimeMillis() - currentMs;

                            //判斷是滑動還是點擊操作、判斷是否繼續傳遞信號
                            if (moveTime < 300 && moveX < 20 && moveY < 20) {   //點擊事件
                                return false;
                            } else {  //TODO:tag滑動和頁面滑動會一起判斷
                                //滑動事件
                                int nextPosition;
                                if (event.getY() - lastY < -90) {   //從下到上滑(上滑)    //position從0開始
                                    ((LinearLayoutManager) RV.getLayoutManager()).scrollToPositionWithOffset((Math.abs(position + 1)) % items.size(), 0); //定位到指定項如果該項可以置頂就將其置頂顯示
                                    leavePosition=(Math.abs(position + 1)) % items.size();
                                    showPopupWindow((Math.abs(position + 1)) % items.size());
                                    nextPosition = (Math.abs(position + 1)) % items.size();  //1021
                                    push_length[0] = (long) items.get(nextPosition).get("push_Length");
                                    push_timepoint[0] = (long) items.get(nextPosition).get("push_Timepoint");
                                    push_endtime[0] = push_timepoint[0] + push_length[0];
                                    countdown(push_timepoint[0], dt.getTime(),push_endtime[0]);
                                } else if (event.getY() - lastY > 90) {
                                    ((LinearLayoutManager) RV.getLayoutManager()).scrollToPositionWithOffset((Math.abs(position - 1 + items.size())) % items.size(), 0);
                                    leavePosition=(Math.abs(position - 1 + items.size())) % items.size();
                                    showPopupWindow((Math.abs(position - 1 + items.size())) % items.size());
                                    nextPosition = (Math.abs(position - 1 + items.size())) % items.size();  //1021
                                    push_length[0] = (long) items.get(nextPosition).get("push_Length");
                                    push_timepoint[0] = (long) items.get(nextPosition).get("push_Timepoint");
                                    push_endtime[0] = push_timepoint[0] + push_length[0];  //推播結束時間點=推播開始時間+推播時長
                                    countdown(push_timepoint[0], dt.getTime(),push_endtime[0]);
                                } else if (event.getX() - lastX > 0) { //從左到右滑(右滑)    //按讚
                                    Glide.with(SearchResultsActivity.this).load(R.drawable.bad_bold).into(bad_bold);
                                    isAwesome(plusid);

                                } else if (event.getX() - lastX < 0) {
                                    //按倒讚
                                    Glide.with(SearchResultsActivity.this).load(R.drawable.good_bold).into(good_bold);
                                    isNotAwesome(plusid);
                                }
                                moveY = 0;
                                moveX = 0;
                                return true;
                            }
                        default:
                            break;
                    }
                    return false;
                }
            });
        }catch(Exception e){
            System.out.println("我的推播介面error");
            System.out.println(e.getStackTrace());
        }

    }
    //1031 推播時長倒數
    public void countdown(long start_time,long now_time,long milliseconds) {  //(推播開始時間,現在時間,推播結束時間)

        boolean start=true;
        if (countDownTimer != null)   //若已有在倒數的，取消舊有的倒數器
            countDownTimer.cancel();
        System.out.println("push1:" + milliseconds);
        System.out.println("push2:" + now_time);

        if (now_time >= start_time){  //現在時間>=推播開始時間(開始推播)
            start=true;
            milliseconds = milliseconds - now_time;   //推播結束時間-現在時間
        }
        else {      //現在時間<推播開始時間
            start=false;
            milliseconds = start_time - now_time;;  //推播開始時間-現在時間
        }

        long finalMilliseconds = milliseconds;
        boolean finalStart = start;

        countDownTimer=new CountDownTimer(finalMilliseconds, 1000) {
            @Override
            public void onTick(long l) {  //每次倒數變動
                long seconds = (l / 1000) % 60 ;
                long minutes = (l / (1000*60)) % 60;
                // long hours   = (l / (1000*60*60)) % 24;
                long hours   = (l / (1000*60*60));
                String timestr=String.format("%02d:%02d:%02d",hours,minutes,seconds);  // 格式化字串，整數，長度2，不足部分左邊補0
                if(l>0 && finalStart ==true)
                    countdownTime.setText("倒數"+timestr+"\n"+"貼文將一閃即逝");
                else if(l>0 && finalStart==false)
                    countdownTime.setText(timestr+"後\n"+"貼文即將發布");
                else
                    countdownTime.setText("推播已結束!");
            }
            @Override
            public void onFinish() {
                countdownTime.setText("推播已結束!");
            }
        }.start();
    }
    //popupwindow按讚
    public void isAwesome(String plusid){
        myRef.child("push").child(plusid).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                } else {
                    String numbertext = String.valueOf(task.getResult().child("goodnumber").getValue());//取得貼文讚的數量

                    if(task.getResult().child("baduid").child(uid).getValue() != null){//這個uid之前已按過倒讚了，收回倒讚
                        Glide.with(SearchResultsActivity.this).load(R.drawable.bad_bold).into(bad_bold);
                        Glide.with(SearchResultsActivity.this).load(R.drawable.good_bold).into(good_bold);
                        //要在bad那邊-1和移除uid
                        myRef.child("push").child(plusid).child("baduid").child(uid).removeValue();
                        String badnumbertext = String.valueOf(task.getResult().child("badnumber").getValue());//取得貼文倒讚的數量
                        int num = Integer.parseInt(badnumbertext);//轉數字型態
                        num--;//加1
                        String afterbadnumbertext = Integer.toString(num);//轉文字型態
                        if(Integer.parseInt(afterbadnumbertext)>=0)
                            badnumber.setText(afterbadnumbertext);
                        myRef.child("push").child(plusid).child("badnumber").setValue(afterbadnumbertext);//存進資料庫
                    }
                    //已按讚又按第二下，收回讚
                    myRef.child("push").child(plusid).child("gooduid").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(Task<DataSnapshot> task) {
                            if (!task.isSuccessful()) {
                                Log.e("firebase", "Error getting data", task.getException());
                            } else {
                                if (task.getResult().child(uid).getValue() != null) { //代表之前已按讚，要-1
                                    Glide.with(SearchResultsActivity.this).load(R.drawable.good_bold).into(good_bold);
                                    myRef.child("push").child(plusid).child("gooduid").child(uid).removeValue();
                                    int number = Integer.parseInt(numbertext);//轉數字型態
                                    number--;//加1
                                    String afternumbertext = Integer.toString(number);//轉文字型態
                                    myRef.child("push").child(plusid).child("goodnumber").setValue(afternumbertext);//存進資料庫
                                    if(Integer.parseInt(afternumbertext)>=0)
                                        goodnumber.setText(afternumbertext);
                                } else { //之前未按讚，要+1
                                    Glide.with(SearchResultsActivity.this).load(R.drawable.good_color).into(good_bold);
                                    myRef.child("push").child(plusid).child("gooduid").child(uid).setValue(0);
                                    myRef.child("user").child(uid).child("mission").child("thumb").child("isFinish").setValue(true);
                                    if (numbertext != "null") {
                                        int number = Integer.parseInt(numbertext);//轉數字型態
                                        number++;//加1
                                        String afternumbertext = Integer.toString(number);//轉文字型態
                                        myRef.child("push").child(plusid).child("goodnumber").setValue(afternumbertext);//存進資料庫
                                        if(Integer.parseInt(afternumbertext)>=0)
                                            goodnumber.setText(afternumbertext);
                                    } else {
                                        int number = 1;
                                        String afternumbertext = Integer.toString(number);//轉文字型態
                                        myRef.child("push").child(plusid).child("goodnumber").setValue(afternumbertext);//存進資料庫
                                        if(Integer.parseInt(afternumbertext)>=0)
                                            goodnumber.setText(afternumbertext);
                                    }
                                }
                            }
                            // imageadapter.notifyItemChanged(viewHolder.getAdapterPosition());//可以用來只更新單一物件item//讓圖片回來原位置
                        }
                    });
                }
            }
        });
    }
    //popupwindow按倒讚
    public void isNotAwesome(String plusid){
        myRef.child("push").child(plusid).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                } else {
                    String numbertext = String.valueOf(task.getResult().child("badnumber").getValue());//取得貼文倒讚的數量
                    if(task.getResult().child("gooduid").child(uid).getValue() != null){//這個uid之前已按過讚了
                        Glide.with(SearchResultsActivity.this).load(R.drawable.good_bold).into(good_bold);  //收回之前按讚的
                        //要在good那邊-1和移除uid
                        myRef.child("push").child(plusid).child("gooduid").child(uid).removeValue();
                        String goodnumbertext = String.valueOf(task.getResult().child("goodnumber").getValue());//取得貼文讚的數量
                        int numm = Integer.parseInt(goodnumbertext);//轉數字型態
                        numm--;//減1
                        String aftergoodnumbertext = Integer.toString(numm);//轉文字型態
                        if(Integer.parseInt(aftergoodnumbertext)>=0)
                            goodnumber.setText(aftergoodnumbertext); //1024
                        myRef.child("push").child(plusid).child("goodnumber").setValue(aftergoodnumbertext);//存進資料庫
                    }
                    myRef.child("push").child(plusid).child("baduid").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {//連結到資料庫的baduid裡面
                        @Override
                        public void onComplete(Task<DataSnapshot> task) {
                            if (!task.isSuccessful()) {
                                Log.e("firebase", "Error getting data", task.getException());
                            } else {
                                if (task.getResult().child(uid).getValue() != null) {//代表之前這個uid已按過倒讚，要-1
                                    Glide.with(SearchResultsActivity.this).load(R.drawable.bad_bold).into(bad_bold);
                                    myRef.child("push").child(plusid).child("baduid").child(uid).removeValue();
                                    int number = Integer.parseInt(numbertext);//轉數字型態
                                    number--;//加1
                                    String afternumbertext = Integer.toString(number);//轉文字型態
                                    //1024
                                    if(Integer.parseInt(afternumbertext)>=0)
                                        badnumber.setText(afternumbertext);
                                    myRef.child("push").child(plusid).child("badnumber").setValue(afternumbertext);//存進資料庫
                                } else {//之前未按過倒讚，要+1
                                    myRef.child("push").child(plusid).child("baduid").child(uid).setValue(0);
                                    Glide.with(SearchResultsActivity.this).load(R.drawable.bad_color).into(bad_bold);
                                    if (numbertext != "null") {//是否以前有其他uid按過倒讚
                                        int number = Integer.parseInt(numbertext);//轉數字型態
                                        number++;//加1
                                        String afternumbertext = Integer.toString(number);//轉文字型態
                                        //1024
                                        if(Integer.parseInt(afternumbertext)>=0)
                                            badnumber.setText(afternumbertext);
                                        myRef.child("push").child(plusid).child("badnumber").setValue(afternumbertext);//存進資料庫
                                    } else {
                                        int number = 1;
                                        String afternumbertext = Integer.toString(number);//轉文字型態
                                        //1024
                                        if(Integer.parseInt(afternumbertext)>=0)
                                            badnumber.setText(afternumbertext);
                                        myRef.child("push").child(plusid).child("badnumber").setValue(afternumbertext);//存進資料庫
                                    }
                                }
                            }
                            // imageadapter.notifyItemChanged(viewHolder.getAdapterPosition());//可以用來只更新單一物件item//讓圖片回來原位置
                        }
                    });
                }
            }
        });
    }
    public void recordwindowtag(String tagtext){//設置tag跳頁
        pos_classify=tagtext;
        Home.status_classify=8;//用來讓ClassifyTag辨別是哪頁打開的tag頁，所以參數統一設置在Home中
        openClassifyTag();
    }
    public void setup_classify(){
        SearchAdapter searchadapter=new SearchAdapter(context,classify_string,R.layout.search_cardview);
        RV_search.setAdapter(searchadapter);

        searchadapter.setOnItemClickListener(new SearchAdapter.OnItemClickListener() {
            @Override
            public void onClick(View view, int position) {
                record(position);
            }
        });
    }
    public void record(int position){
        pos_classify=classify_string.get(position);
        openClassifyTag();
        Home.status_classify=7;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        MenuItem menuItem = menu.findItem(R.id.action_search);
        androidx.appcompat.widget.SearchView searchView = (androidx.appcompat.widget.SearchView) menuItem.getActionView();
        searchView.setQueryHint(getResources().getString(R.string.search_hint));
        menuItem.expandActionView();
        searchView.setQuery(Tag.query_text, false);
        searchView.clearFocus();
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
    List list;
    public List<Map<String, Object>> getItem() {
        return list;
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        //讓父佈局不攔截
        RV.requestDisallowInterceptTouchEvent(true);
        return super.dispatchTouchEvent(ev);
    }
    //讀取資料方法
    /*private void firebase_select(DatabaseReference db) {
        final List<Map<String,Object>> items=new ArrayList<Map<String,Object>>();
        final List<Myinfo.PlusString> plus=new ArrayList<>();
        final List<Upload> uploadString=new ArrayList<>();
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Myinfo.PlusString push_data = ds.getValue(Myinfo.PlusString.class);
                    Upload[] upload = {ds.getValue(Upload.class)};
                    Map<String, Object> item = new HashMap<String, Object>();
                    item.put("id", ds.getKey());
                    item.put("classification", push_data.getClassification());
                    item.put("tag", push_data.getTag());
                    item.put("title", push_data.getTitle());
                    item.put("name",push_data.getName());
                    item.put("url", upload[0].getImageUrl());
                    items.add(item);
                    push_data = new Myinfo.PlusString(Integer.parseInt(ds.getKey()), push_data.getClassification(), push_data.getTag(), push_data.getTitle(),push_data.getName());
                    //讀資料庫的url資料，放進upload裡
                    myRef2.child(ds.getKey()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(Task<DataSnapshot> task) {
                            if (!task.isSuccessful()) {
                                Log.e("firebase", "Error getting data", task.getException());
                            } else {
                                upload[0] = new Upload(String.valueOf(task.getResult().child("imageName").getValue()), String.valueOf(task.getResult().child("imageName").getValue()));
                            }
                        }
                    });
                    String data_text=push_data.getTitle().toLowerCase();//資料庫的title
                    String[] data_text_split=data_text.split("");
                    String query_text=Tag.query_text.toLowerCase();//query的title
                    String[] query_text_split=query_text.split("");
                    for(int i=0;i<data_text_split.length;i++){//abc
                        if(data_text_split[i].equals(query_text_split[0])){//b
                            int k=i;//1
                            int j=0;
                            boolean status=true;//是否有收尋到相同字串，true是有，
                            while(true) {
                                if(data_text_split[k].equals(query_text_split[j])){
                                    k++;//0.1.2.3.4
                                    j++;
                                }else{
                                    status=false;
                                    break;
                                }
                                if(j>=query_text_split.length){//5
                                    break;
                                }
                                if(k>=data_text_split.length){//5
                                    status=false;
                                    break;
                                }
                            }
                            if(status==true){
                                plus.add(push_data);
                                uploadString.add(upload[0]);
                                x_last = Integer.parseInt(ds.getKey());   //抓取最後一筆key值
                                break;
                            }
                        }
                    }
                }
                if(plus.size()>0) {
                    ImageAdapter imageadapter = new ImageAdapter(context, uploadString, plus);
                    RV.setAdapter(imageadapter);
                    recyclerViewAction(RV, items, imageadapter);//用來管理滑動
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

    }*/
    //讀取資料方法
    private void firebase_select(DatabaseReference db) {
        //  final List<Map<String,Object>> items=new ArrayList<Map<String,Object>>();
        final List<Myinfo.PlusString> plus=new ArrayList<>();
        final List<Upload> uploadString=new ArrayList<>();
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //1026珵 203~204
                latLng = new LatLng((double) snapshot.child("user").child(uid).child("currentLatLng").child("LatLng").child("latitude").getValue(), (double) snapshot.child("user").child(uid).child("currentLatLng").child("LatLng").child("longitude").getValue());
                SimpleDateFormat dateParser = new SimpleDateFormat("yyyyMMddHHmmss");
                Date push_Time=new Date();
                for (DataSnapshot ds : snapshot.child("push").getChildren()) {
                    Myinfo.PlusString push_data = ds.getValue(Myinfo.PlusString.class);
                    Upload[] upload = {ds.getValue(Upload.class)};
                    Map<String, Object> item = new HashMap<String, Object>();
                    item.put("id", ds.getKey());
                    item.put("classification", push_data.getClassification());
                    item.put("tag", push_data.getTag());
                    item.put("title", push_data.getTitle());
                    item.put("name",push_data.getName());
                    item.put("url", upload[0].getImageUrl());

                    //1026珵 新增218~245
                    item.put("pushplace",ds.child("pushplace").child("address").getValue()==null?"沒有地點":ds.child("pushplace").child("address").getValue());
                    LatLng temLatlng = new LatLng((double)ds.child("pushplace").child("latitude").getValue(), (double)ds.child("pushplace").child("longitude").getValue());
                    item.put("pushLatlng", temLatlng);
                    double distance = getDistance(latLng, temLatlng);
                    DecimalFormat dt = new DecimalFormat("0");
                    double weighted;
                    Long now_Time = push_Time.getTime();
                    Long dead_Time = push_data.getPushTimepoint() + push_data.getPushLength();
                    boolean isaddAD = (boolean) ds.child("addAD").getValue();
                    if (isaddAD) {
                        weighted = distance * 0.7 + (dead_Time - now_Time) / 3600000 * 0.3 - 11000000;
                    } else {
                        weighted = distance * 0.7 + (dead_Time - now_Time) / 3600000 * 0.3;
                    }
                    item.put("weighted", weighted);
                    if (distance >= 1000) {
                        distance = distance / 1000;
                        String finalDistance = dt.format(distance);
                        item.put("distance", finalDistance + "公里");
                    } else {
                        String finalDistance1 = dt.format(distance);
                        item.put("distance", finalDistance1 + "公尺");
                    }
                    //1026
                    item.put("push_context",ds.child("word").getValue()==null?"沒有內文":ds.child("word").getValue());
                    item.put("push_Length",ds.child("pushdate").child("push_Length").getValue()==null?"0":ds.child("pushdate").child("push_Length").getValue()); //1021 推播時長
                    item.put("push_Timepoint",ds.child("pushdate").child("push_Timepoint").getValue()==null?"0":ds.child("pushdate").child("push_Timepoint").getValue()); //1021 推播開始時間
                    //TODO:星期誤差(蔡勒公式)
                    //1026 時間
                    String[] activityStartDate = ds.child("activitydate").child("activityStartDate").getValue().toString().split("\\(");  //只存日期不存星期(因星期有誤差)
                    String[] activityEndDate = ds.child("activitydate").child("activityEndDate").getValue().toString().split("\\(");
                    String activityStartTime=ds.child("activitydate").child("activityStartTime").getValue().toString();
                    String activityEndTime=ds.child("activitydate").child("activityEndTime").getValue().toString();
                    String activityWholeTime=activityStartDate[0]+" "+activityStartTime+"～"+activityEndDate[0]+" "+activityEndTime;
                    item.put("activityStartDate",activityStartDate[0]==null?"0":activityStartDate[0]); //1025活動開始時間
                    item.put("activityEndDate",activityEndDate[0]==null?"0":activityEndDate[0]); //1025活動結束時間
                    item.put("activityWholeTime",activityWholeTime);
                    myRef.child("user").child(push_data.getAuthor()).child("title").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            if (!task.isSuccessful()) {
                                Log.e("firebase", "Error getting user_title", task.getException());
                            } else {
                                item.put("user_title",task.getResult().getValue());
                            }
                        }
                    });
                    //1026珵 修改257~263
                    push_data=new Myinfo.PlusString(Integer.parseInt(ds.getKey()),push_data.getClassification(),push_data.getTag(),push_data.getTitle(),
                            push_data.getAuthor(),push_data.getName(), push_data.getAddr(),push_data.getPushContext(), item.get("distance").toString());
                    push_data.setActivityStartDate(activityStartDate[0]);
                    push_data.setActivityEndDate(activityEndDate[0]);
                    push_data.setPushTimepoint((long) ds.child("pushdate").child("push_Timepoint").getValue());
                    push_data.setPushLength((long) ds.child("pushdate").child("push_Length").getValue());
                    push_data.setIsaddAD(isaddAD);
                    //讀資料庫的url資料，放進upload裡
                    myRef2.child(ds.getKey()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(Task<DataSnapshot> task) {
                            if (!task.isSuccessful()) {
                                Log.e("firebase", "Error getting data", task.getException());
                            } else {
                                upload[0] = new Upload(String.valueOf(task.getResult().child("imageName").getValue()), String.valueOf(task.getResult().child("imageName").getValue()));
                            }
                        }
                    });
                    String data_text=push_data.getTitle().toLowerCase();//資料庫的title
                    String[] data_text_split=data_text.split("");
                    String query_text=Tag.query_text.toLowerCase();//query的title
                    String[] query_text_split=query_text.split("");
                    for(int i=0;i<data_text_split.length;i++){//abc
                        if(data_text_split[i].equals(query_text_split[0])) {//b
                            int k = i;//1
                            int j = 0;
                            boolean status = true;//是否有收尋到相同字串，true是有，
                            while (true) {
                                if (data_text_split[k].equals(query_text_split[j])) {
                                    k++;//0.1.2.3.4
                                    j++;
                                } else {
                                    status = false;
                                    break;
                                }
                                if (j >= query_text_split.length) {//5
                                    break;
                                }
                                if (k >= data_text_split.length) {//5
                                    status = false;
                                    break;
                                }
                            }
                            //1110
                            try {
                                push_Time = dateParser.parse(dateParser.format(push_Time));
                            } catch (ParseException e) {
                                e.printStackTrace();
                                System.out.println("首頁-轉換時間404");
                            }
                            if (push_Time.getTime() >= push_data.getPushTimepoint() && push_Time.getTime() <= (push_data.getPushTimepoint() + push_data.getPushLength())) {
                                plus.add(push_data);
                                uploadString.add(upload[0]);
                                items.add(item);
                                // x_last = Integer.parseInt(ds.getKey());   //抓取最後一筆key值

                            }
                            if (status == true) {
                                x_last = Integer.parseInt(ds.getKey());   //抓取最後一筆key值
                                break;
                            }
                        }
                    }
                }
                for(int i=0; i<items.size()-1; i++){
                    int minInx = i;
                    double min = (double) items.get(i).get("weighted");
                    Map<String, Object> minMap = items.get(i);
                    Myinfo.PlusString minplus = plus.get(i);
                    Upload minUpload = uploadString.get(i);
                    for (int j = 1+i ; j < items.size(); j++) {
                        if (min > (double) items.get(j).get("weighted")) {
                            // 重設minInx, min
                            minInx = j;
                            min = (double) items.get(j).get("weighted");
                            minMap = items.get(j);
                            minplus = plus.get(j);
                            minUpload = uploadString.get(j);
                        }
                    }
                    if (minInx != i) {
                        items.set(minInx, items.get(i));
                        plus.set(minInx, plus.get(i));
                        uploadString.set(minInx, uploadString.get(i));
                        items.set(i, minMap);
                        plus.set(i, minplus);
                        uploadString.set(i, minUpload);
                    }
                }
                Collections.reverse(plus);
                Collections.reverse(uploadString);
                if(plus.size()>0) {
                    // ImageAdapter imageadapter = new ImageAdapter(context, uploadString, plus);
                    imageAdapter = new ImageAdapter(context, uploadString, plus); //1110 a
                    RV.addOnItemTouchListener(new Pushlist.RecyclerItemClickListener(context,RV,new Pushlist.RecyclerItemClickListener.OnItemClickListener(){
                        @Override
                        public void onItemClick(View view,int position){
                        }

                        @Override
                        public void onLongItemClick(View parent, int position) {
                            popupWindow.showAtLocation(parent, Gravity.CENTER_HORIZONTAL, 0, 0);
                            showPopupWindow(position);
                            leavePosition=position; //紀錄最後手指停留的位置 //1110
                        }
                    }
                    ));
                    RV.setAdapter(imageAdapter);
                    recyclerViewAction(RV, items, imageAdapter);//用來管理滑動
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

    }
    private void recyclerViewAction(RecyclerView RV, final List<Map<String,Object>> items, final ImageAdapter imageadapter) {//用來管理滑動
        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {//用來定義哪些方向可以用
                return makeMovementFlags(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
            }
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView//定義item是怎么移動的，從哪移動到哪，並且可以修改被移動所遮擋的其他view的行為，這里主要實現的是拖拽
                    , @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder targe) {
                int position = viewHolder.getAdapterPosition();

                return false;
            }
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {////管理滑動情形
                int position = viewHolder.getAdapterPosition();
                plusid=String.valueOf(items.get(position).get("id"));//貼文的id
                switch (direction) {
                    case ItemTouchHelper.LEFT://按倒讚
                        myRef.child("push").child(plusid).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                            @Override
                            public void onComplete(Task<DataSnapshot> task) {
                                if (!task.isSuccessful()) {
                                    Log.e("firebase", "Error getting data", task.getException());
                                } else {
                                    String numbertext = String.valueOf(task.getResult().child("badnumber").getValue());//取得貼文倒讚的數量
                                    if(task.getResult().child("gooduid").child(uid).getValue() != null){//這個uid之前已按過讚了
                                        //要在good那邊-1和移除uid
                                        myRef.child("push").child(plusid).child("gooduid").child(uid).removeValue();
                                        String goodnumbertext = String.valueOf(task.getResult().child("goodnumber").getValue());//取得貼文讚的數量
                                        int numm = Integer.parseInt(goodnumbertext);//轉數字型態
                                        numm--;//減1
                                        String aftergoodnumbertext = Integer.toString(numm);//轉文字型態
                                        myRef.child("push").child(plusid).child("goodnumber").setValue(aftergoodnumbertext);//存進資料庫
                                    }
                                    myRef.child("push").child(plusid).child("baduid").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {//連結到資料庫的baduid裡面
                                        @Override
                                        public void onComplete(Task<DataSnapshot> task) {
                                            if (!task.isSuccessful()) {
                                                Log.e("firebase", "Error getting data", task.getException());
                                            } else {
                                                if (task.getResult().child(uid).getValue() != null) {//代表之前這個uid已按過倒讚，要-1
                                                    myRef.child("push").child(plusid).child("baduid").child(uid).removeValue();
                                                    int number = Integer.parseInt(numbertext);//轉數字型態
                                                    number--;//加1
                                                    String afternumbertext = Integer.toString(number);//轉文字型態
                                                    myRef.child("push").child(plusid).child("badnumber").setValue(afternumbertext);//存進資料庫
                                                } else {//之前未按過倒讚，要+1
                                                    myRef.child("push").child(plusid).child("baduid").child(uid).setValue(0);
                                                    if (numbertext != "null") {//是否以前有其他uid按過倒讚
                                                        int number = Integer.parseInt(numbertext);//轉數字型態
                                                        number++;//加1
                                                        String afternumbertext = Integer.toString(number);//轉文字型態
                                                        myRef.child("push").child(plusid).child("badnumber").setValue(afternumbertext);//存進資料庫
                                                    } else {
                                                        int number = 1;
                                                        String afternumbertext = Integer.toString(number);//轉文字型態
                                                        myRef.child("push").child(plusid).child("badnumber").setValue(afternumbertext);//存進資料庫
                                                    }
                                                }
                                            }
                                            imageadapter.notifyItemChanged(viewHolder.getAdapterPosition());//可以用來只更新單一物件item//讓圖片回來原位置
                                        }
                                    });
                                }
                            }
                        });
                        break;
                    case ItemTouchHelper.RIGHT://按讚
                        myRef.child("push").child(plusid).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                            @Override
                            public void onComplete(Task<DataSnapshot> task) {
                                if (!task.isSuccessful()) {
                                    Log.e("firebase", "Error getting data", task.getException());
                                } else {
                                    String numbertext = String.valueOf(task.getResult().child("goodnumber").getValue());//取得貼文讚的數量
                                    if(task.getResult().child("baduid").child(uid).getValue() != null){//這個uid之前已按過倒讚了
                                        //要在bad那邊-1和移除uid
                                        myRef.child("push").child(plusid).child("baduid").child(uid).removeValue();
                                        String badnumbertext = String.valueOf(task.getResult().child("badnumber").getValue());//取得貼文倒讚的數量
                                        int num = Integer.parseInt(badnumbertext);//轉數字型態
                                        num--;//加1
                                        String afterbadnumbertext = Integer.toString(num);//轉文字型態
                                        myRef.child("push").child(plusid).child("badnumber").setValue(afterbadnumbertext);//存進資料庫
                                    }
                                    myRef.child("push").child(plusid).child("gooduid").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                        @Override
                                        public void onComplete(Task<DataSnapshot> task) {
                                            if (!task.isSuccessful()) {
                                                Log.e("firebase", "Error getting data", task.getException());
                                            } else {
                                                if (task.getResult().child(uid).getValue() != null) {//代表之前已按讚，要-1
                                                    myRef.child("push").child(plusid).child("gooduid").child(uid).removeValue();
                                                    int number = Integer.parseInt(numbertext);//轉數字型態
                                                    number--;//加1
                                                    String afternumbertext = Integer.toString(number);//轉文字型態
                                                    myRef.child("push").child(plusid).child("goodnumber").setValue(afternumbertext);//存進資料庫
                                                } else {//之前未按讚，要+1
                                                    myRef.child("push").child(plusid).child("gooduid").child(uid).setValue(0);
                                                    if (numbertext != "null") {
                                                        int number = Integer.parseInt(numbertext);//轉數字型態
                                                        number++;//加1
                                                        String afternumbertext = Integer.toString(number);//轉文字型態
                                                        myRef.child("push").child(plusid).child("goodnumber").setValue(afternumbertext);//存進資料庫
                                                    } else {
                                                        int number = 1;
                                                        String afternumbertext = Integer.toString(number);//轉文字型態
                                                        myRef.child("push").child(plusid).child("goodnumber").setValue(afternumbertext);//存進資料庫
                                                    }
                                                }
                                            }
                                            imageadapter.notifyItemChanged(viewHolder.getAdapterPosition());//可以用來只更新單一物件item//讓圖片回來原位置
                                        }
                                    });
                                }
                            }
                        });
                        break;
                }
            }
            public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                //  super.clearView(recyclerView, viewHolder);
                //重置改變，防止由於複用而導致的顯示問題
                viewHolder.itemView.setScrollX(0);
            }
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {//左右滑底圖
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    // Get RecyclerView item from the ViewHolder
                    View itemView = viewHolder.itemView;

                    Paint p = new Paint();
                    Bitmap icon;

                    if (dX > 0) {

                        icon = BitmapFactory.decodeResource(
                                context.getResources(), R.drawable.good);

                        /* Set your color for positive displacement */
                        p.setARGB(255,144, 255, 97);

                        // Draw Rect with varying right side, equal to displacement dX
                        c.drawRect((float) itemView.getLeft(), (float) itemView.getTop(), dX,
                                (float) itemView.getBottom(), p);

                        // Set the image icon for Right swipe
                        c.drawBitmap(icon,
                                (float) itemView.getLeft() + convertDpToPx(16),
                                (float) itemView.getTop() + ((float) itemView.getBottom() - (float) itemView.getTop() - icon.getHeight())/2,
                                p);
                    } else {
                        icon = BitmapFactory.decodeResource(
                                context.getResources(), R.drawable.bad);

                        /* Set your color for negative displacement */
                        p.setARGB(255, 255, 74, 74);

                        // Draw Rect with varying left side, equal to the item's right side
                        // plus negative displacement dX
                        c.drawRect((float) itemView.getRight() + dX, (float) itemView.getTop(),
                                (float) itemView.getRight(), (float) itemView.getBottom(), p);

                        //Set the image icon for Left swipe
                        c.drawBitmap(icon,
                                (float) itemView.getRight() - convertDpToPx(16) - icon.getWidth(),
                                (float) itemView.getTop() + ((float) itemView.getBottom() - (float) itemView.getTop() - icon.getHeight())/2,
                                p);
                    }

                    final float alpha = ALPHA_FULL - Math.abs(dX) / (float) viewHolder.itemView.getWidth();
                    viewHolder.itemView.setAlpha(alpha);
                    viewHolder.itemView.setTranslationX(dX);

                } else {
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                }
            }

            private int convertDpToPx(int dp){
                return Math.round(dp * (getResources().getDisplayMetrics().xdpi / DisplayMetrics.DENSITY_DEFAULT));
            }
        });
        helper.attachToRecyclerView(RV);

    }
    public static double getDistance(LatLng start,
                                     LatLng end) {
        double lat1 = (Math.PI/180)*start.latitude;
        double lat2 = (Math.PI/180)*end.latitude;

        double lon1 = (Math.PI/180)*start.longitude;
        double lon2 = (Math.PI/180)*end.longitude;

        //地球半径
        double R = 6371;

        //两点间距离 km，如果想要米的话，结果*1000就可以了
        double d =  Math.acos(Math.sin(lat1)*Math.sin(lat2)+Math.cos(lat1)*Math.cos(lat2)*Math.cos(lon2-lon1))*R;

        return d*1000;
    }
    private void openClassify() {
        Intent intent = new Intent(this, Classify.class);
        startActivity(intent);
    }
    private void openClassifyTag() {
        Intent intent = new Intent(this, ClassifyTag.class);
        startActivity(intent);
    }
    private void openSearch() {
        Intent intent = new Intent(this, SearchResultsActivity.class);
        startActivity(intent);
    }
    //1110  TODO:點及各種按鈕時(EG:TAG、收藏)會跳懸浮窗
    public static class  RecyclerItemClickListener  implements  RecyclerView.OnItemTouchListener  {
        private Pushlist.RecyclerItemClickListener.OnItemClickListener mListener;
        GestureDetector mGestureDetector;


        @Override
        public boolean onInterceptTouchEvent(@NonNull RecyclerView view, @NonNull MotionEvent e) {
            View childView = view.findChildViewUnder(e.getX(), e.getY());

            //if (childView != null && mListener != null && mGestureDetector.onTouchEvent(e)) {

            if (childView != null && mListener != null && mGestureDetector.onTouchEvent(e)) {
                mListener.onItemClick(childView, view.getChildAdapterPosition(childView));
                return true ;
            }
            return false ;
        }

        @Override
        public void onTouchEvent(@NonNull RecyclerView view, @NonNull MotionEvent e) {
            View childView = view.findChildViewUnder(e.getX(), e.getY());
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        }
        public RecyclerItemClickListener (Context context, final RecyclerView recyclerView, Pushlist.RecyclerItemClickListener.OnItemClickListener listener)  {
            mListener = listener;
            mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                public boolean onSingleTapUp (MotionEvent e) {
                    if (mListener != null ) { //if (mChildView != null && mListener != null ) {
                        final int pos=0; //pos= mRecyclerView.getChildAdapterPosition(mChildView);
                        if (pos != RecyclerView.NO_POSITION) {  //在layout佈局沒有完成的時候會返回NO_POSITION
                            mListener.onItemClick(recyclerView, pos); //mListener.onItemClick(mChildView, pos);
                            return true ;
                        }
                    }
                    return false ;
                }
                public void onLongPress (MotionEvent e) {
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && mListener != null ) {    //TODO:判斷長按秒數
                        // if(checkLongPress(e)) {
                        mListener.onLongItemClick(child, recyclerView.getChildAdapterPosition(child));
                        //}
                    }
                }
            });
        }
        public  interface  OnItemClickListener  {
            public void onItemClick (View view, int position) ;
            public  void  onLongItemClick (View view, int position) ;
        }
    }
}
