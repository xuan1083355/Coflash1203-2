package com.example.coflash;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.RelativeLayout;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.coflash.ui.main.Upload;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Notice extends Activity {
    private ImageButton home,tag,plus,collect,myinfo,back;

    //RecyclerView
    RecyclerView rv_notice;
    NoticeAdapter noticeAdapter;

    //資料庫
    DatabaseReference myRef, myRef2,myRef_push;  //1117
    String uid;
    List<Map<String,Object>> notices = new ArrayList<Map<String,Object>>();
//    List<Map<String,Object>> Notices = new ArrayList<Map<String,Object>>();

    //懸浮視窗 1117
    //懸浮視窗 1117
    private PopupWindow popupWindow;
    private ImageButton heart_popup; //1003
    private ImageView photo,good_bold,bad_bold;    //1003
    private TextView push_title,user_name,addr,push_context,user_title,goodnumber,badnumber,countdownTime, push_distance; //1021
    private String goodnum,badnum;//取得貼文讚數、倒讚數
    private List<Map<String,Object>> items=new ArrayList<Map<String,Object>>();
    //Map<String,Object> pop_item=new HashMap<>();
    private RecyclerView tagRv;
    private PopupWindowAdapter popupWindowAdapter;
    private TextView activity_date; //活動日期(不含時間)
    private TextView activity_wholeTime;  //活動日期+時間
    //推播時長倒數器
    private CountDownTimer countDownTimer;
    Context context = this;
    private static String usertitle;
    LatLng latLng;
    private TextView viewCount; //1121

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice);

        home = findViewById(R.id.under_home);
        tag = findViewById(R.id.under_tag);
        plus = findViewById(R.id.under_plus);
        collect = findViewById(R.id.under_collect);
        myinfo = findViewById(R.id.under_myinfo);
        back = findViewById(R.id.back);

        //資料庫連線
        FirebaseDatabase database = FirebaseDatabase.getInstance();//連線數據庫
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser(); //取得使用者資料
        if (user != null) {
            uid = user.getUid();//取得uid
        }
        myRef = database.getReference("DB");
        myRef2 = myRef.child("user").child(uid).child("Notice");
//        myRef_push=myRef.child("push"); //1117
        firebase_select(myRef);

        rv_notice = findViewById(R.id.rv_notice);
        rv_notice.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));



        home.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                openHome();
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
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
        initPopupWindow();
    }
    //初始化PopupWindow(懸浮視窗)
    private void initPopupWindow() {
        View view = LayoutInflater.from(context) .inflate(R.layout.popupwindow_layout_notice, null);
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
        activity_wholeTime=(TextView) view.findViewById(R.id.activity_wholeTime);
        viewCount=(TextView) view.findViewById(R.id.viewCount);

        popupWindow.setTouchable(true);
        popupWindow.setFocusable(true); //點一下消失，聚焦於popupWindow的操作
        popupWindow.setOutsideTouchable(true);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {  //popupWindow關閉後，背景顏色恢復
            @Override
            public void onDismiss() {
                darkenBackground(1f);
              //  RV.setAdapter(imageAdapter);   //懸浮式窗關閉後，重新更新RV的資料，讓在懸浮視窗點擊的愛心、讚同步更新到頁面  //1110
              //  recyclerViewAction(RV, items, imageAdapter);
              //  ((LinearLayoutManager)RV.getLayoutManager()).scrollToPositionWithOffset(leavePosition,0); //回到剛剛停留的貼文位置
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
    final int[] finalPosition = {0};

    //懸浮窗出現
    private void showPopupWindow(int position){
        darkenBackground(0.5f); //背景變暗
//        System.out.println("TITLE:"+items.get(position).get("title"));
        final int[] viewcount=new int[1];  //1121
        System.out.println("貼文文ID:"+items.get(position).get("plus_id"));
        //取得貼文觀看數
        myRef.child("push").child(items.get(position).get("plus_id").toString()).child("viewCount").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()) {
                    viewcount[0] = Integer.parseInt(task.getResult().getValue().toString());
                    viewcount[0] += 1;
                    myRef.child("push").child(items.get(position).get("plus_id").toString()).child("viewCount").setValue(viewcount[0]);  //長按後觀看數+1
                    viewCount.setText(String.valueOf(viewcount[0]));
                }else
                    System.out.println("Fail to get 貼文觀看數");
            }
        });
        Glide.with(context).load(items.get(position).get("url")).centerCrop().into(photo);
        push_title.setText(items.get(position).get("title").toString());    //推播標題
        user_name.setText(items.get(position).get("user_name").toString()); //使用者名稱
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

        activity_wholeTime.setText("活動時間:\n" + items.get(position).get("activityWholeTime").toString());

        popupWindowAdapter = new PopupWindowAdapter(context, tags);
        tagRv.setAdapter(popupWindowAdapter);
        popupWindowAdapter.setOnItemClickListener(new PopupWindowAdapter.OnItemClickListener() {
            @Override
            public void onClick(View view, String tagtext) {
              //  recordwindowtag(tagtext);
            }
        });

        String plusid = items.get(position).get("plus_id").toString();
       // String plusid="2";
//        System.out.println("GET: "+items.get(position).get("plus_id"));
        //檢查是不是已經收藏
        myRef.child("user").child(uid).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                } else {
                    if (task.getResult().child("collect_id").child(plusid).getValue() != null) {//代表之前這個uid收藏了這則貼文
                        Glide.with(Notice.this).load(R.drawable.heart_popupcolor).into(heart_popup);
                    } else {//未收藏
                        Glide.with(Notice.this).load(R.drawable.heart_popup).into(heart_popup);
                    }
                }
            }
        });
        //1024
        final long[] push_length = {(long) items.get(position).get("push_Length")};
        final long[] push_timepoint = {(long) items.get(position).get("push_Timepoint")};
        final long[] push_endtime = {(long) push_timepoint[0] + push_length[0]};
        Date dt=new Date();
        countdown(push_endtime[0],dt.getTime());

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
                    if( !goodnum.equals("null") && Integer.parseInt(goodnum)>=0)
                        goodnumber.setText(goodnum);
                    else
                        goodnumber.setText("0");
                    //貼文倒讚數
                    if(!badnum.equals("null") && Integer.parseInt(badnum)>=0)
                        badnumber.setText(badnum);
                    else
                        badnumber.setText("0");

                    if (task.getResult().child("baduid").child(uid).getValue() != null) {//這個uid之前已按過倒讚了
                        Glide.with(Notice.this).load(R.drawable.good_bold).into(good_bold);
                        Glide.with(Notice.this).load(R.drawable.bad_color).into(bad_bold);
                    }else if(task.getResult().child("gooduid").child(uid).getValue() != null){  //按讚
                        Glide.with(Notice.this).load(R.drawable.good_color).into(good_bold);
                        Glide.with(Notice.this).load(R.drawable.bad_bold).into(bad_bold);
                    } else {    //沒有按過讚&倒讚
                        Glide.with(Notice.this).load(R.drawable.bad_bold).into(bad_bold);
                        Glide.with(Notice.this).load(R.drawable.good_bold).into(good_bold);
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
                                Glide.with(Notice.this).load(R.drawable.heart_popup).into(heart_popup);
                                myRef.child("user").child(uid).child("collect_id").child(plusid).removeValue();//移除紀錄
                            }else{//未收藏
                                Glide.with(Notice.this).load(R.drawable.heart_popupcolor).into(heart_popup);
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
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:   //手指的初次觸摸
                        downX=event.getX();
                        downY=event.getY();
                        currentMs=System.currentTimeMillis();
                        lastX=event.getX();
                        lastY=event.getY();
                        break;
                    case MotionEvent.ACTION_MOVE:   //手指滑動
                        downX=event.getX();
                        downY=event.getY();
                        moveX += Math.abs(event.getRawX() - downX);//x軸移動距離
                        moveY += Math.abs(event.getRawY() - downY);//y軸移動距離
                        break;
                    case MotionEvent.ACTION_UP: //抬起
                        moveTime=System.currentTimeMillis()-currentMs;

                        //判斷是滑動還是點擊操作、判斷是否繼續傳遞信號
                        if(moveTime<300 && moveX<20 && moveY<20) {   //點擊事件
                            return false;
                        }else{  //TODO:tag滑動和頁面滑動會一起判斷
                            //滑動事件
                            if(event.getX()-lastX>0){ //從左到右滑(右滑)    //按讚
                                Glide.with(Notice.this).load(R.drawable.bad_bold).into(bad_bold);
                                isAwesome(plusid);

                            }else if(event.getX()-lastX<0){
                                //按倒讚
                                Glide.with(Notice.this).load(R.drawable.good_bold).into(good_bold);
                                isNotAwesome(plusid);
                            }
                            moveY=0;
                            moveX=0;
                            return true;
                        }
                    default:
                        break;
                }
                return false;
            }
        });

    }
    //1021 推播時長倒數
    public void countdown(long milliseconds,long now_time){
        if(countDownTimer!=null)   //若已有在倒數的，取消舊有的倒數器
            countDownTimer.cancel();
        milliseconds=milliseconds-now_time;
        countDownTimer=new CountDownTimer(milliseconds, 1000) {
            @Override
            public void onTick(long l) {  //每次倒數變動
                long seconds = (l / 1000) % 60 ;
                long minutes = (l / (1000*60)) % 60;
                long hours   = (l / (1000*60*60));
                //long day   =  (l / (1000*60*60*24));  //1029
                //String timestr=String.format("%02d:%02d:%02d:%02d",day,hours,minutes,seconds);  // 格式化字串，整數，長度2，不足部分左邊補0
                String timestr=String.format("%02d:%02d:%02d",hours,minutes,seconds);  // 格式化字串，整數，長度2，不足部分左邊補0
                if(l>0)
                    countdownTime.setText("倒數"+timestr+" "+"貼文將一閃即逝");
                else
                    countdownTime.setText("推播結束!");
            }
            @Override
            public void onFinish() {
                countdownTime.setText("推播結束!");
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
                        Glide.with(Notice.this).load(R.drawable.bad_bold).into(bad_bold);
                        Glide.with(Notice.this).load(R.drawable.good_bold).into(good_bold);
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
                                    Glide.with(Notice.this).load(R.drawable.good_bold).into(good_bold);
                                    myRef.child("push").child(plusid).child("gooduid").child(uid).removeValue();
                                    int number = Integer.parseInt(numbertext);//轉數字型態
                                    number--;//加1
                                    String afternumbertext = Integer.toString(number);//轉文字型態
                                    myRef.child("push").child(plusid).child("goodnumber").setValue(afternumbertext);//存進資料庫
                                    if(Integer.parseInt(afternumbertext)>=0)
                                        goodnumber.setText(afternumbertext);
                                } else { //之前未按讚，要+1
                                    Glide.with(Notice.this).load(R.drawable.good_color).into(good_bold);
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
                        Glide.with(Notice.this).load(R.drawable.good_bold).into(good_bold);  //收回之前按讚的
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
                                    Glide.with(Notice.this).load(R.drawable.bad_bold).into(bad_bold);
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
                                    Glide.with(Notice.this).load(R.drawable.bad_color).into(bad_bold);
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

    public static long plus_id;  //1117
    private void firebase_select(DatabaseReference db) {
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    latLng = new LatLng((double) snapshot.child("user").child(uid).child("currentLatLng").child("LatLng").child("latitude").getValue(), (double) snapshot.child("user").child(uid).child("currentLatLng").child("LatLng").child("longitude").getValue());
                }catch(Exception e){
                    latLng = new LatLng(22.7344266,120.2850404);
                }

                ArrayList<String> status=new ArrayList<>();
                for (DataSnapshot ds : snapshot.child("user").child(uid).child("Notice").getChildren()) {
                    Map<String, Object> item = (Map<String, Object>) ds.getValue();
                    Map<String, Object> pop_item = new HashMap<>();
                    item.put("id", String.valueOf(ds.getKey()));
                    String id = String.valueOf(ds.getKey());
//                    System.out.println("在這");
//                    System.out.println(ds.child("isRead").getValue());
                    if ((boolean)ds.child("isRead").getValue()) {//判定已讀未讀
                        status.add("true");
                    } else {
                        status.add("false");
                    }
                    myRef2.child(String.valueOf(ds.getKey())).child("isRead").setValue(true);//看完就設定已讀
                    notices.add(item);
//                    System.out.println("plus_id:"+ds.child("plus_id").getValue());
                    pop_item.put("plus_id", ds.child("plus_id").getValue());  //依照position存入貼文ID
//                    System.out.println("plus_id:"+pop_item.get("plus_id"));

                    //}
                    DataSnapshot notice_push = snapshot.child("push").child(ds.child("plus_id").getValue().toString());
                    pop_item.put("classification", notice_push.child("classification").getValue());
                    pop_item.put("tag", notice_push.child("tag").getValue());
                    pop_item.put("title", notice_push.child("title").getValue());
                    pop_item.put("url", notice_push.child("imageUrl").getValue());
                    //0911
                    pop_item.put("user_name", notice_push.child("name").getValue());
                    pop_item.put("pushplace", notice_push.child("pushplace").child("address").getValue() == null ? "沒有地點" : notice_push.child("pushplace").child("address").getValue());
                    System.out.println("在這");
                    System.out.println(ds.child("plus_id").getValue().toString());
                    System.out.println(pop_item);
                    LatLng temLatlng = new LatLng((double) notice_push.child("pushplace").child("latitude").getValue(), (double) notice_push.child("pushplace").child("longitude").getValue());
                    pop_item.put("pushLatlng", temLatlng);
                    //1120
                    double distance = getDistance(latLng, temLatlng);
                    DecimalFormat dt = new DecimalFormat("0");

                    //Notice 地點
                    if (distance >= 1000) {
                        distance = distance / 1000;
                        String finalDistance = dt.format(distance);
                        pop_item.put("distance", finalDistance + "公里");

                    } else {
                        String finalDistance1 = dt.format(distance);
                        pop_item.put("distance", finalDistance1 + "公尺");

                    }

                    //1025 816~819
                    pop_item.put("push_context", notice_push.child("word").getValue() == null ? "沒有內文" : notice_push.child("word").getValue());
                    pop_item.put("push_Length", notice_push.child("pushdate").child("push_Length").getValue() == null ? "0" : notice_push.child("pushdate").child("push_Length").getValue()); //1021 推播時長
                    pop_item.put("push_Timepoint", notice_push.child("pushdate").child("push_Timepoint").getValue() == null ? "0" : notice_push.child("pushdate").child("push_Timepoint").getValue()); //1021 推播開始時間
                    //TODO:星期誤差(蔡勒公式)
                    //1025 時間   820~828
                    String[] activityStartDate = notice_push.child("activitydate").child("activityStartDate").getValue().toString().split("\\(");  //只存日期不存星期(因星期有誤差)
                    String[] activityEndDate = notice_push.child("activitydate").child("activityEndDate").getValue().toString().split("\\(");
                    String activityStartTime = notice_push.child("activitydate").child("activityStartTime").getValue().toString();
                    String activityEndTime = notice_push.child("activitydate").child("activityEndTime").getValue().toString();
                    String activityWholeTime = activityStartDate[0] + " " + activityStartTime + "～" + activityEndDate[0] + " " + activityEndTime;
                    pop_item.put("activityStartDate", activityStartDate[0] == null ? "0" : activityStartDate[0]); //1025活動開始時間
                    pop_item.put("activityEndDate", activityEndDate[0] == null ? "0" : activityEndDate[0]); //1025活動結束時間
                    pop_item.put("activityWholeTime", activityWholeTime);
                    myRef.child("user").child(notice_push.child("author").getValue().toString()).child("title").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            if (!task.isSuccessful()) {
                                Log.e("firebase", "Error getting user_title", task.getException());
                                System.out.println("Error getting user_title");
                            } else {
                                usertitle = (String) task.getResult().getValue();
                                pop_item.put("user_title", task.getResult().getValue());
                                //  System.out.println("ITEM:"+items.get(0).get("user_title"));
                                usertitle = (String) pop_item.get("user_title");
                            }
                        }
                    });
                    items.add(pop_item);
                }
                Collections.reverse(notices);
                Collections.reverse(status);
                Collections.reverse(items);

                noticeAdapter = new NoticeAdapter(Notice.this, notices,status);
                rv_notice.addOnItemTouchListener(new RecyclerItemClickListener(context, rv_notice, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View parent, int position) {
                        popupWindow.showAtLocation(parent, Gravity.CENTER_HORIZONTAL, 0, 0);
                        showPopupWindow(position);
//                        System.out.println("position:"+position);
                        // leavePosition=position;
                    }
                    @Override
                    public void onLongItemClick(View parent, int position) {
                    }
                }));
                rv_notice.setAdapter(noticeAdapter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }


    //0930  TODO:點擊各種按鈕時(EG:TAG、收藏)會跳懸浮窗
    public static class  RecyclerItemClickListener  implements  RecyclerView.OnItemTouchListener  {
        private OnItemClickListener mListener;
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
        public RecyclerItemClickListener (Context context, final RecyclerView recyclerView, OnItemClickListener listener)  {
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

    //開啟介面
    private void openHome(){
        finish();
    }
    private void openTag(){
        Intent intent=new Intent(this,Tag.class);
        startActivity(intent);
    }
    private void openPlus(){
        Intent intent=new Intent(this,Plus.class);
        startActivity(intent);
    }
    private void openCollect(){
        Intent intent=new Intent(this,Collect.class);
        startActivity(intent);
    }
    private void openMyinfo() {
        Intent intent = new Intent(this, Myinfo.class);
        startActivity(intent);
    }

    //RecyclerView
    public class NoticeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private Context mContext;
        private List<Map<String,Object>> mnotice = new ArrayList<>();
        private LayoutInflater layoutInflater;
        private ArrayList<String> status=new ArrayList<>();


        // Create constructor
        public NoticeAdapter(Context context, List<Map<String,Object>> noticeStrings,ArrayList<String> status)
        {
            mContext = context;
            mnotice = noticeStrings;
            layoutInflater=LayoutInflater.from(mContext);
            this.status=status;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(
                    LayoutInflater.from(context)
                            .inflate(R.layout.layout_notice, parent, false)
            );
        }


        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            Map<String,Object> noticeCurrent = mnotice.get(position);
            String  type = (String) noticeCurrent.get("type");
            //改變已讀未讀顏色
            if(status.get(position).equals("false")){//未讀
                ((ViewHolder)holder).background.setBackgroundColor(Color.WHITE);
            }else{//已讀
//                ((ViewHolder)holder).v_isRead.setVisibility(View.GONE);
                ((ViewHolder)holder).background.setBackgroundColor(Color.rgb(217,217,217));
            }
            if(type.equals("location")){
                String str1 = "距離你" + (String) noticeCurrent.get("content") + "有新活動！";
                String str2 = (String) noticeCurrent.get("title");
                ((ViewHolder)holder).tv_tag.setText(str1);
                ((ViewHolder)holder).tv_title.setText(str2);
                ((ViewHolder)holder).imgV_type.setImageResource(R.drawable.address);
            }
            else if(type.equals("time")){
                String str1 = "你收藏的貼文在" + (String) noticeCurrent.get("content") + "就要消失囉！";
                String str2 = (String) noticeCurrent.get("title");
                ((ViewHolder)holder).tv_tag.setText(str1);
                ((ViewHolder)holder).tv_title.setText(str2);
                ((ViewHolder)holder).imgV_type.setImageResource(R.drawable.time);
            }
            else{
                String str1 = "你追蹤的「" + (String) noticeCurrent.get("content") + "」有新貼文了！";
                String str2 = (String) noticeCurrent.get("title");
                ((ViewHolder)holder).tv_tag.setText(str1);
                ((ViewHolder)holder).tv_title.setText(str2);
            }

        }

        @Override
        public int getItemCount() {
            return mnotice.size();
        }


        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView tv_tag, tv_title;
            public ImageView imgV_type,background;
            public View v_isRead;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tv_tag = itemView.findViewById(R.id.tv_tag);
                tv_title = itemView.findViewById(R.id.tv_title);
                imgV_type = itemView.findViewById(R.id.imgV_type);
                background = itemView.findViewById(R.id.background);
                v_isRead = itemView.findViewById(R.id.v_isRead);
            }
        }
    }
    public static double getDistance(LatLng start, LatLng end) {
        double lat1 = (Math.PI/180)*start.latitude;
        double lat2 = (Math.PI/180)*end.latitude;

        double lon1 = (Math.PI/180)*start.longitude;
        double lon2 = (Math.PI/180)*end.longitude;
        double R = 6371;//地球半径
        //两点间距离 km，如果想要米的话，结果*1000就可以了
        double d =  Math.acos(Math.sin(lat1)*Math.sin(lat2)+Math.cos(lat1)*Math.cos(lat2)*Math.cos(lon2-lon1))*R;
        return d*1000;
    }
}