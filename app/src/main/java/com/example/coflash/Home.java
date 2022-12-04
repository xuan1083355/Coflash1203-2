package com.example.coflash;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.MotionEvent;

import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;

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

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Home extends Activity{

    private ImageButton home,tag,plus,collect,myinfo;
    private ImageView Notice,everyday;

    private Button btn_address1,btn_address2;
    private TextView tv, noticeCount;
    private RecyclerView RV,RV_classify;
    private FirebaseDatabase db;
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
    //地址
    LatLng latLng, currentLatLng;
    String str1;
    Geocoder geocoder;
    private static final int ACTIVITY_REPORT = 1000;

    //更新時間
    int timeDistance;

    private PopupWindow popupWindow;
    private ImageButton heart_popup; //1003
    private ImageView photo,good_bold,bad_bold;    //1003
    private TextView push_title,user_name,addr,push_context,user_title,goodnumber,badnumber,countdownTime; //1121
    // private TextView push_distance;
    private String goodnum,badnum;//取得貼文讚數、倒讚數
    private List<Map<String,Object>> items=new ArrayList<Map<String,Object>>();
    private List<Map<String,Object>> items_AD=new ArrayList<Map<String,Object>>();
    private List<Map<String,Object>> items_afterAD=new ArrayList<Map<String,Object>>();
    private RecyclerView tagRv;
    private PopupWindowAdapter popupWindowAdapter;
    //  private TextView activity_date; //活動日期(不含時間)
    private TextView activity_wholeTime;  //活動日期+時間  //1121


    //距離通知
    private static final double EARTH_RADIUS = 6378.137;
    int notion_last = 0;
    String oldAddress="";
    String str3 = "";
    boolean equal, isDistancesNotice, isTimeNotice;

    //到期通知
    ArrayList<Integer> afterlist=new ArrayList<Integer>();
    //推播時長倒數器
    private CountDownTimer countDownTimer;
    private static int leavePosition; //紀錄懸浮視窗最後停留的貼文位置 //1110
    private TextView viewCount; //1121


    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        home = findViewById(R.id.under_home);
        tag = findViewById(R.id.under_tag);
        plus = findViewById(R.id.under_plus);
        collect = findViewById(R.id.under_collect);
        myinfo = findViewById(R.id.under_myinfo);
        Notice = findViewById(R.id.Notice);
        btn_address1 = findViewById(R.id.btn_address1);
        btn_address2 = findViewById(R.id.btn_address2);
        noticeCount = findViewById(R.id.noticeCount);
        everyday = (ImageView)findViewById(R.id.everyday);

        //推播訊息RV
        RV=(RecyclerView)findViewById(R.id.RV);
        RV.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));


        id=(TextView)findViewById(R.id.id);
        tv1=(TextView)findViewById(R.id.tv1);
        //tv2=(TextView)findViewById(R.id.tv2);
        tv3=(TextView)findViewById(R.id.tv3);
        imageView=(ImageView)findViewById(R.id.imageView);
        good_view=(ImageView)findViewById(R.id.good);
        bad_view=(ImageView)findViewById(R.id.bad);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();//取得使用者資料
        if (user != null) {
            uid = user.getUid();//取得uid
        }
        //連線資料庫
        FirebaseDatabase database = FirebaseDatabase.getInstance();//連線數據庫
        myRef = database.getReference("DB");
        myRef2 = myRef.child("push");
        myRef3 = myRef.child("user").child(uid).child("currentLatLng");

        //分類classify的部分
        RV_classify=(RecyclerView)findViewById(R.id.RV_classify);
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
        myRef.child("user").child(uid).child("tag_love").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String s=String.valueOf(ds.getKey());
                    classify_string.add(s);
                }
                setup_classify();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        firebase_select(myRef); //1026珵 修改
        java.util.List list;
//        System.out.println("NEW DATE: "+new Date().getTime());

        home.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
                Intent intent = new Intent(Home.this, Home.class);
                startActivity(intent);
            }
        });
        tag.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent=new Intent(Home.this,Tag.class);
                startActivity(intent);
            }
        });
        plus.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent=new Intent(Home.this,Plus.class);
                startActivity(intent);
            }
        });
        collect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent=new Intent(Home.this,Collect.class);
                startActivity(intent);
            }
        });
        myinfo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent=new Intent(Home.this,Myinfo.class);
                startActivity(intent);
            }
        });
        Notice.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(Home.this, Notice.class);
                startActivity(intent);
            }
        });
        btn_address1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                openChangeaddress();
            }
        });
        btn_address2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                openChangeaddress();
            }
        });
        everyday.setOnTouchListener(imgListener);
        everyday.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(Home.this, Everyday.class);
                startActivity(intent);
            }
        });

        initPopupWindow();

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
        //   push_distance = (TextView)view.findViewById(R.id.push_distance);
        //  activity_date=(TextView) view.findViewById(R.id.activity_date);
        activity_wholeTime=(TextView) view.findViewById(R.id.activity_wholeTime);
        viewCount=(TextView) view.findViewById(R.id.viewCount);

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
                // System.out.println("新位置:"+leavePosition);
                // System.out.println(finalPosition[0]);
                //onRestart(finalPosition[0]);//1107
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
        final int[] viewcount=new int[1];
        System.out.println("貼文文ID:"+items.get(position).get("id"));
        //取得貼文觀看數
        myRef.child("push").child(items.get(position).get("id").toString()).child("viewCount").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()) {
                    viewcount[0] = Integer.parseInt(task.getResult().getValue().toString());
                    viewcount[0] += 1;
                    myRef.child("push").child(items.get(position).get("id").toString()).child("viewCount").setValue(viewcount[0]);  //長按後觀看數+1
                    viewCount.setText(String.valueOf(viewcount[0]));
                }else
                    System.out.println("Fail to get 貼文觀看數");
            }
        });
        darkenBackground(0.5f); //背景變暗
        Glide.with(context).load(items.get(position).get("url")).centerCrop().into(photo);
        //Glide.with(context).load(items.get(position).get("url")).apply(RequestOptions.bitmapTransform(new RoundedCorners(30))).into(photo);
        push_title.setText(items.get(position).get("title").toString());    //推播標題
        user_name.setText(items.get(position).get("user_name").toString()); //使用者名稱
        addr.setText(items.get(position).get("pushplace").toString());  //推播地點
        push_context.setText(items.get(position).get("push_context").toString());   //推播內文
        push_context.setMovementMethod(ScrollingMovementMethod.getInstance());
        user_title.setText(items.get(position).get("user_title").toString());
        //push_distance.setText(items.get(position).get("distance").toString());
        //tag
        tags = new ArrayList<>();
        tagStr = items.get(position).get("tag").toString();
        for (String s : tagStr.split("#")) {
            if (!(s.equals("")) && !(s.equals(" "))) //不為空時
                tags.add(s.trim());
        }
        //1121
        String activityDate = items.get(position).get("activityStartDate") + "～" + items.get(position).get("activityEndDate");
        //   if (items.get(position).get("activityStartDate").equals(items.get(position).get("activityEndDate")))
        //      activityDate = items.get(position).get("activityStartDate") + " 一日快閃限定"; //1026珵
        //   activity_date.setText(activityDate);

        activity_wholeTime.setText("活動時間:\n" + items.get(position).get("activityWholeTime").toString());

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
                        Glide.with(Home.this).load(R.drawable.heart_popupcolor).into(heart_popup);
                    } else {//未收藏
                        Glide.with(Home.this).load(R.drawable.heart_popup).into(heart_popup);
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
                        Glide.with(Home.this).load(R.drawable.good_bold).into(good_bold);
                        Glide.with(Home.this).load(R.drawable.bad_color).into(bad_bold);
                    }else if(task.getResult().child("gooduid").child(uid).getValue() != null){  //按讚
                        Glide.with(Home.this).load(R.drawable.good_color).into(good_bold);
                        Glide.with(Home.this).load(R.drawable.bad_bold).into(bad_bold);
                    } else {    //沒有按過讚&倒讚
                        Glide.with(Home.this).load(R.drawable.bad_bold).into(bad_bold);
                        Glide.with(Home.this).load(R.drawable.good_bold).into(good_bold);
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
                                Glide.with(Home.this).load(R.drawable.heart_popup).into(heart_popup);
                                myRef.child("user").child(uid).child("collect_id").child(plusid).removeValue();//移除紀錄
                            }else{//未收藏
                                Glide.with(Home.this).load(R.drawable.heart_popupcolor).into(heart_popup);
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
                            int  nextPosition;
                            if(event.getY()-lastY<-90){   //從下到上滑(上滑)    //position從0開始
                                finalPosition[0] =(Math.abs(position+1))%items.size();
                                leavePosition=finalPosition[0];   //紀錄最後停留的貼文位置
                                // System.out.println("新新位置為:"+finalPosition[0]);
                                ((LinearLayoutManager)RV.getLayoutManager()).scrollToPositionWithOffset(finalPosition[0],0); //定位到指定項如果該項可以置頂就將其置頂顯示
                                showPopupWindow(finalPosition[0]);
                                nextPosition=finalPosition[0];  //1021
                                push_length[0] =(long) items.get(nextPosition).get("push_Length");
                                push_timepoint[0] =(long) items.get(nextPosition).get("push_Timepoint");
                                push_endtime[0]=push_timepoint[0]+push_length[0];
                                countdown(push_endtime[0],dt.getTime());
                            }else if(event.getY()-lastY>90){
                                finalPosition[0]=(Math.abs(position-1+items.size()))%items.size();
                                //  System.out.println("往下新新位置為:"+finalPosition[0]);
                                leavePosition=finalPosition[0];
                                ((LinearLayoutManager)RV.getLayoutManager()).scrollToPositionWithOffset(finalPosition[0],0);
                                showPopupWindow(finalPosition[0]);
                                nextPosition=finalPosition[0];  //1021
                                push_length[0] =(long) items.get(nextPosition).get("push_Length");
                                push_timepoint[0] =(long) items.get(nextPosition).get("push_Timepoint");
                                push_endtime[0]=push_timepoint[0]+push_length[0];  //推播結束時間點=推播開始時間+推播時長
                                countdown(push_endtime[0],dt.getTime());
                            }else if(event.getX()-lastX>0){ //從左到右滑(右滑)    //按讚
                                Glide.with(Home.this).load(R.drawable.bad_bold).into(bad_bold);
                                isAwesome(plusid);

                            }else if(event.getX()-lastX<0){
                                //按倒讚
                                Glide.with(Home.this).load(R.drawable.good_bold).into(good_bold);
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
                        Glide.with(Home.this).load(R.drawable.bad_bold).into(bad_bold);
                        Glide.with(Home.this).load(R.drawable.good_bold).into(good_bold);
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
                                    Glide.with(Home.this).load(R.drawable.good_bold).into(good_bold);
                                    myRef.child("push").child(plusid).child("gooduid").child(uid).removeValue();
                                    int number = Integer.parseInt(numbertext);//轉數字型態
                                    number--;//加1
                                    String afternumbertext = Integer.toString(number);//轉文字型態
                                    myRef.child("push").child(plusid).child("goodnumber").setValue(afternumbertext);//存進資料庫
                                    if(Integer.parseInt(afternumbertext)>=0)
                                        goodnumber.setText(afternumbertext);
                                } else { //之前未按讚，要+1
                                    Glide.with(Home.this).load(R.drawable.good_color).into(good_bold);
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
                        Glide.with(Home.this).load(R.drawable.good_bold).into(good_bold);  //收回之前按讚的
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
                                    Glide.with(Home.this).load(R.drawable.bad_bold).into(bad_bold);
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
                                    Glide.with(Home.this).load(R.drawable.bad_color).into(bad_bold);
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
            case 7:
                pos_classify=classify_string.get(7);
                openClassifyTag();
                break;
            case 8:
                pos_classify=classify_string.get(8);
                openClassifyTag();
                break;
            case 9:
                pos_classify=classify_string.get(9);
                openClassifyTag();
                break;
            case 10:
                pos_classify=classify_string.get(10);
                openClassifyTag();
                break;
            case 11:
                pos_classify=classify_string.get(11);
                openClassifyTag();
                break;
            default:
                pos_classify="找不到";
        }
        status_classify=1;
    }
    public void recordwindowtag(String tagtext){//設置tag跳頁
        pos_classify=tagtext;
        Home.status_classify=1;
        openClassifyTag();
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
    private void firebase_select(DatabaseReference db) {
        final List<Myinfo.PlusString> plus=new ArrayList<>();
        final List<Upload> uploadString=new ArrayList<>();
        final List<Myinfo.PlusString> plus_AD=new ArrayList<>();
        final List<Upload> uploadString_AD=new ArrayList<>();
        final List<Myinfo.PlusString> plus_afterAD=new ArrayList<>();
        final List<Upload> uploadString_afterAD=new ArrayList<>();
        items.clear();
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {  // 1025 739要搬
                //int x_sum=(int)snapshot.getChildrenCount();    獲取分支總數
                try {
                    latLng = new LatLng((double) snapshot.child("user").child(uid).child("currentLatLng").child("LatLng").child("latitude").getValue(), (double) snapshot.child("user").child(uid).child("currentLatLng").child("LatLng").child("longitude").getValue());
                }catch(Exception e){
                    latLng = new LatLng(22.7344266,120.2850404);
                }
                try{
                    str1 = String.valueOf(snapshot.child("user").child(uid).child("currentLatLng").child("title").getValue());
                    geocoder = new Geocoder(Home.this, Locale.TRADITIONAL_CHINESE);
                    List<Address> address = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                    String str2 = address.get(0).getAdminArea() + ", " + address.get(0).getPostalCode();
                    str3 = address.get(0).getAddressLine(0);
                    btn_address1.setText(str1);
                    btn_address2.setText(str2);
                } catch (Exception e) {
                    Toast.makeText(Home.this, "尚未設定所在地", Toast.LENGTH_SHORT).show();
                    openChangeaddress();
                }
                myRef.child("user").child(uid).child("NoticeSetting").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.child("distance").getValue() != null) {
                            isDistancesNotice = (boolean) snapshot.child("distance").getValue();
                        }
                        else{
                            myRef.child("user").child(uid).child("NoticeSetting").child("distance").setValue(true);
                            isDistancesNotice = true;
                        }
                        if(snapshot.child("time").getValue() != null) {
                            isTimeNotice = (boolean) snapshot.child("time").getValue();
                        }
                        else{
                            myRef.child("user").child(uid).child("NoticeSetting").child("time").setValue(true);
                            isTimeNotice = true;
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
                //1112 珵 777~929
                for (DataSnapshot ds : snapshot.child("push").getChildren()) {
                    Myinfo.PlusString push_data=ds.getValue(Myinfo.PlusString.class);
                    Upload[] upload = {ds.getValue(Upload.class)};
                    Map<String,Object> item=new HashMap<String,Object>();
                    SimpleDateFormat dateParser = new SimpleDateFormat("yyyyMMddHHmmss");
                    Date push_Time=new Date();
                    push_data.setPushTimepoint((long) ds.child("pushdate").child("push_Timepoint").getValue());
                    push_data.setPushLength((long) ds.child("pushdate").child("push_Length").getValue());   //1102
                    push_data.setIsaddAD((boolean) ds.child("addAD").getValue());
                    try {
                        push_Time=dateParser.parse(dateParser.format(push_Time));
                    } catch (ParseException e) {
                        e.printStackTrace();
                        System.out.println("首頁-轉換時間404");
                    }
                    if (push_Time.getTime()>=push_data.getPushTimepoint() && push_Time.getTime()<=(push_data.getPushTimepoint()+push_data.getPushLength())) {
                        item.put("id",ds.getKey());
                        item.put("classification", push_data.getClassification());
                        item.put("tag",push_data.getTag());
                        item.put("title",push_data.getTitle());
                        item.put("url", upload[0].getImageUrl());
                        //0911
                        item.put("user_name",push_data.getName());
                        item.put("pushplace",ds.child("pushplace").child("address").getValue()==null?"沒有地點":ds.child("pushplace").child("address").getValue());
                        LatLng temLatlng = new LatLng((double)ds.child("pushplace").child("latitude").getValue(), (double)ds.child("pushplace").child("longitude").getValue());
                        item.put("pushLatlng", temLatlng);
                        double distance = getDistance(latLng, temLatlng);
                        DecimalFormat dt = new DecimalFormat("0");
                        double weighted;
                        Long now_Time = push_Time.getTime();
                        Long dead_Time = push_data.getPushTimepoint()+push_data.getPushLength();
                        boolean isaddAD = (boolean) ds.child("addAD").getValue();
                        if(isaddAD){
                            weighted = distance * 0.7 + (dead_Time-now_Time)/3600000 * 0.3 - 11000000;
                        }
                        else{
                            weighted = distance * 0.7 + (dead_Time-now_Time)/3600000 * 0.3;
                        }
                        item.put("weighted", weighted);
                        if(distance >= 1000) {
                            distance = distance / 1000;
                            String finalDistance = dt.format(distance);
                            item.put("distance", finalDistance + "公里");
                            if(distance <= 5 && isDistancesNotice && !equal){   //1025 777~793不用
                                myRef.child("user").child(uid).child("Notice").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        notion_last = (int) snapshot.getChildrenCount();
                                        boolean repeat = true;
                                        for(int i=notion_last;i>notion_last-20;i--){
                                            if((String.valueOf(snapshot.child(String.valueOf(i)).child("title").getValue()).equals(item.get("title").toString())) && (String.valueOf(snapshot.child(String.valueOf(i)).child("content").getValue()).equals(finalDistance + "公里"))){
                                                repeat = false;
                                            }
                                        }
                                        if(repeat) {
                                            notion_last += 1;
                                            myRef.child("user").child(uid).child("Notice").child(String.valueOf(notion_last)).child("plus_id").setValue(item.get("id").toString());
                                            myRef.child("user").child(uid).child("Notice").child(String.valueOf(notion_last)).child("content").setValue(finalDistance + "公里");
                                            myRef.child("user").child(uid).child("Notice").child(String.valueOf(notion_last)).child("title").setValue(item.get("title").toString());
                                            myRef.child("user").child(uid).child("Notice").child(String.valueOf(notion_last)).child("isRead").setValue(false);
                                            myRef.child("user").child(uid).child("Notice").child(String.valueOf(notion_last)).child("type").setValue("location");
                                        }
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                    }
                                });
                            }
                        }
                        else{
                            String finalDistance1 = dt.format(distance);
                            item.put("distance", finalDistance1+"公尺");
                            if(isDistancesNotice && !equal){  //1025 798~814不用
                                myRef.child("user").child(uid).child("Notice").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        notion_last = (int) snapshot.getChildrenCount();
                                        boolean repeat = true;
                                        for(int i=notion_last;i>notion_last-20;i--){
                                            if((String.valueOf(snapshot.child(String.valueOf(i)).child("title").getValue()).equals(item.get("title").toString())) && (String.valueOf(snapshot.child(String.valueOf(i)).child("content").getValue()).equals(finalDistance1 + "公尺"))){
                                                repeat = false;
                                            }
                                        }
                                        if(repeat) {
                                            notion_last += 1;
                                            myRef.child("user").child(uid).child("Notice").child(String.valueOf(notion_last)).child("plus_id").setValue(item.get("id").toString());
                                            myRef.child("user").child(uid).child("Notice").child(String.valueOf(notion_last)).child("content").setValue(finalDistance1 + "公尺");
                                            myRef.child("user").child(uid).child("Notice").child(String.valueOf(notion_last)).child("title").setValue(item.get("title").toString());
                                            myRef.child("user").child(uid).child("Notice").child(String.valueOf(notion_last)).child("isRead").setValue(false);
                                            myRef.child("user").child(uid).child("Notice").child(String.valueOf(notion_last)).child("type").setValue("location");
                                        }
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                    }
                                });
                            }
                        }
                        //1025 816~819
                        item.put("push_context",ds.child("word").getValue()==null?"沒有內文":ds.child("word").getValue());
                        item.put("push_Length",ds.child("pushdate").child("push_Length").getValue()==null?"0":ds.child("pushdate").child("push_Length").getValue()); //1021 推播時長
                        item.put("push_Timepoint",ds.child("pushdate").child("push_Timepoint").getValue()==null?"0":ds.child("pushdate").child("push_Timepoint").getValue()); //1021 推播開始時間
                        //TODO:星期誤差(蔡勒公式)
                        //1025 時間   820~828
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
                        push_data=new Myinfo.PlusString(Integer.parseInt(ds.getKey()),push_data.getClassification(),push_data.getTag(),push_data.getTitle(),
                                push_data.getAuthor(),push_data.getName(), push_data.getAddr(),push_data.getPushContext(), item.get("distance").toString());
                        push_data.setActivityStartDate(activityStartDate[0]);
                        push_data.setActivityEndDate(activityEndDate[0]);
                        push_data.setPushTimepoint((long) ds.child("pushdate").child("push_Timepoint").getValue());  //1117
                        push_data.setPushLength((long) ds.child("pushdate").child("push_Length").getValue());   //1117
                        push_data.setIsaddAD(isaddAD);
                        myRef2.child(ds.getKey()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                            @Override
                            public void onComplete(Task<DataSnapshot> task) {
                                if (!task.isSuccessful()) {
                                    Log.e("firebase", "Error getting data", task.getException());
                                } else {
                                    upload[0] =new Upload(String.valueOf(task.getResult().child("imageName").getValue()),String.valueOf(task.getResult().child("imageName").getValue()));
                                }
                            }
                        });
//                        if((Boolean) ds.child("addAD").getValue()){
//                            plus_AD.add(push_data);
//                            uploadString_AD.add(upload[0]);
//                            items_AD.add(item);
//                        }
//                        else{
//                            plus_afterAD.add(push_data);
//                            uploadString_afterAD.add(upload[0]);
//                            items_afterAD.add(item);
//                        }
                        plus.add(push_data);
                        uploadString.add(upload[0]);
                        items.add(item);
                        x_last = Integer.parseInt(ds.getKey());   //抓取最後一筆key值
                    }
                }
                try{
                    Long res = (Long) snapshot.child("user").child(uid).child("mission").child("last_time").getValue();
                    Long now_Time = new Date().getTime();
                    timeDistance = differentDays(new Date(res), new Date(now_Time));
                    if(timeDistance > 0){
                        myRef.child("user").child(uid).child("mission").child("heart").child("isFinish").setValue(false);
                        myRef.child("user").child(uid).child("mission").child("heart").child("isTake").setValue(false);
                        myRef.child("user").child(uid).child("mission").child("plus").child("isFinish").setValue(false);
                        myRef.child("user").child(uid).child("mission").child("plus").child("isTake").setValue(false);
                        myRef.child("user").child(uid).child("mission").child("thumb").child("isFinish").setValue(false);
                        myRef.child("user").child(uid).child("mission").child("thumb").child("isTake").setValue(false);
                        myRef.child("user").child(uid).child("mission").child("everyday").child("isTake").setValue(false);
                        myRef.child("user").child(uid).child("mission").child("last_time").setValue(now_Time);
                    }
                    for (DataSnapshot ds : snapshot.child("user").child(uid).child("collect_id").getChildren()) {
                        afterlist.add(Integer.parseInt(ds.getKey()));
                    }
                    for(Map<String, Object> item : items){
                        if(!afterlist.isEmpty()){
                            if(afterlist.contains(Integer.parseInt((String)item.get("id")))){
                                Long push_Length = (Long) item.get("push_Length");
                                Long push_Timepoint = (Long) item.get("push_Timepoint");
                                String push_Titile = (String) item.get("title");
                                Long push_gap = push_Timepoint + push_Length - now_Time;
                                if(push_gap <= 43200000 && push_gap > 0 && isTimeNotice){
                                    int push_gaphour = (int) (push_gap / 3600000);
                                    if(push_gaphour < 12){
                                        push_gaphour ++;
                                    }
                                    int finalPush_gaphour = push_gaphour;
                                    notion_last = (int) snapshot.child("user").child(uid).child("Notice").getChildrenCount();
                                    boolean repeat = true;
                                    for(int i=notion_last;i>notion_last-20;i--){
                                        if((String.valueOf(snapshot.child("user").child(uid).child("Notice").child(String.valueOf(i)).child("title").getValue()).equals(item.get("title").toString())) && (String.valueOf(snapshot.child("user").child(uid).child("Notice").child(String.valueOf(i)).child("content").getValue()).equals(String.valueOf(finalPush_gaphour) + "小時內"))){
                                            repeat = false;
                                        }
                                    }
                                    if(repeat) {
                                        notion_last += 1;
                                        myRef.child("user").child(uid).child("Notice").child(String.valueOf(notion_last)).child("plus_id").setValue(Integer.parseInt((String) item.get("id")));
                                        myRef.child("user").child(uid).child("Notice").child(String.valueOf(notion_last)).child("content").setValue(String.valueOf(finalPush_gaphour) + "小時內");
                                        myRef.child("user").child(uid).child("Notice").child(String.valueOf(notion_last)).child("title").setValue(push_Titile);
                                        myRef.child("user").child(uid).child("Notice").child(String.valueOf(notion_last)).child("isRead").setValue(false);
                                        myRef.child("user").child(uid).child("Notice").child(String.valueOf(notion_last)).child("type").setValue("time");
                                    }
                                }
                            }
                        }
                    }
                }
                catch (Exception e){
                    Long now_Time = new Date().getTime();
                    myRef.child("user").child(uid).child("mission").child("heart").child("isFinish").setValue(false);
                    myRef.child("user").child(uid).child("mission").child("heart").child("isTake").setValue(false);
                    myRef.child("user").child(uid).child("mission").child("plus").child("isFinish").setValue(false);
                    myRef.child("user").child(uid).child("mission").child("plus").child("isTake").setValue(false);
                    myRef.child("user").child(uid).child("mission").child("thumb").child("isFinish").setValue(false);
                    myRef.child("user").child(uid).child("mission").child("thumb").child("isTake").setValue(false);
                    myRef.child("user").child(uid).child("mission").child("everyday").child("isTake").setValue(false);
                    myRef.child("user").child(uid).child("mission").child("last_time").setValue(now_Time);
                }
                int count = 0;
                for (DataSnapshot ds : snapshot.child("user").child(uid).child("Notice").getChildren()) {
                    Map<String,Object> item= (Map<String, Object>) ds.getValue();
                    boolean isRead = Boolean.valueOf(String.valueOf(item.get("isRead")));
                    if(!isRead){
                        count ++;
                    }
                }
                if(count>0 && count<=99){
                    noticeCount.setVisibility(View.VISIBLE);
                    noticeCount.setText(String.valueOf(count));
                }
                else if(count>99){
                    noticeCount.setVisibility(View.VISIBLE);
                    noticeCount.setText("99+");
                    noticeCount.setTextSize(10);
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
                list = items;
                imageAdapter=new ImageAdapter(context,uploadString,plus);
                RV.addOnItemTouchListener(new RecyclerItemClickListener(context,RV,new RecyclerItemClickListener.OnItemClickListener(){
                    @Override
                    public void onItemClick(View view,int position){
                    }

                    @Override
                    public void onLongItemClick(View parent, int position) {
                        popupWindow.showAtLocation(parent, Gravity.CENTER_HORIZONTAL, 0, 0);
                        showPopupWindow(position);
                        leavePosition=position;
                    }
                }
                ));
                RV.setAdapter(imageAdapter);
                recyclerViewAction(RV, items, imageAdapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
    private void recyclerViewAction(RecyclerView RV, final List<Map<String,Object>> items, final ImageAdapter imageadapter) {
        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {//用來定義哪些方向可以用
                return makeMovementFlags(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
            }
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView//定義item是怎么移動的，從哪移動到哪，並且可以修改被移動所遮擋的其他view的行為，這里主要實現的是拖拽
                    , @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder targe) {

                return false;
            }
            public float getSwipeThreshold(RecyclerView.ViewHolder viewHolder) {//改變拖曳距離
                return .2f;
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
                                                    myRef.child("user").child(uid).child("mission").child("thumb").child("isFinish").setValue(true);
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
                viewHolder.itemView.setScrollX(0); //重置改變，防止由於複用而導致的顯示問題
            }
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {//左右滑底圖
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    // Get RecyclerView item from the ViewHolder
                    View itemView = viewHolder.itemView;
                    Paint p = new Paint();
                    Bitmap icon;

                    if (dX > 0) {
                        icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.good);
                        /* Set your color for positive displacement */
                        p.setARGB(255,144, 255, 97);
                        // Draw Rect with varying right side, equal to displacement dX
                        c.drawRect((float) itemView.getLeft(), (float) itemView.getTop(), dX, (float) itemView.getBottom(), p);

                        // Set the image icon for Right swipe
                        c.drawBitmap(icon, (float) itemView.getLeft() + convertDpToPx(16),
                                (float) itemView.getTop() + ((float) itemView.getBottom() - (float) itemView.getTop() - icon.getHeight())/2, p);
                    } else {
                        icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.bad);

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
    //最新用戶選擇位置
    @Override
    protected void onRestart() {
        super.onRestart();
        myRef.child("user").child(uid).child("currentLatLng").child("title").setValue(str1);
        myRef.child("user").child(uid).child("currentLatLng").child("LatLng").setValue(latLng);
        noticeCount.setVisibility(View.GONE);
        firebase_select(myRef);
    }

    //開啟介面
    private void openChangeaddress() {
        Intent intent = new Intent(this, Changeaddress.class);
        startActivityForResult(intent, ACTIVITY_REPORT);
    }
    private void openClassify() {
        Intent intent = new Intent(this, Classify.class);
        startActivity(intent);
    }
    private void openClassifyTag() {
        Intent intent = new Intent(this, ClassifyTag.class);
        startActivity(intent);
    }
    @Override
    protected void onActivityResult(int requestCode , int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
//            Toast.makeText(Home.this, "有到", Toast.LENGTH_SHORT).show();
            if (requestCode == ACTIVITY_REPORT) {
                latLng = data.getParcelableExtra("address_user_selected");
                str1 = data.getStringExtra("addressName_user_selected");
//                Toast.makeText(Home.this, str1, Toast.LENGTH_SHORT).show();
                geocoder = new Geocoder(this, Locale.TRADITIONAL_CHINESE);
                try {
                    List<Address> address;
                    address = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                    String str2 = address.get(0).getAdminArea() + ", " + address.get(0).getPostalCode();
                    btn_address1.setText(str1);
                    btn_address2.setText(str2);
                } catch (Exception e) {
                }
            }
        }
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
                public boolean onSingleTapUp (MotionEvent e) {   //1202
                   /* if (mListener != null ) { //if (mChildView != null && mListener != null ) {
                        final int pos=0; //pos= mRecyclerView.getChildAdapterPosition(mChildView);
                        if (pos != RecyclerView.NO_POSITION) {  //在layout佈局沒有完成的時候會返回NO_POSITION
                            mListener.onItemClick(recyclerView, pos); //mListener.onItemClick(mChildView, pos);
                            return true ;
                        }
                    }
                    return false ;*/
                    return false;
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
    private View.OnTouchListener imgListener = new View.OnTouchListener() {
        private float x, y;    // 原本圖片存在的X,Y軸位置
        private int mx, my; // 圖片被拖曳的X ,Y軸距離長度

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            // Log.e("View", v.toString());
            long eventtime=0;
            long downtime=0;
            switch (event.getAction()) {          //判斷觸控的動作

                case MotionEvent.ACTION_DOWN:// 按下圖片時
                    x = event.getX();                  //觸控的X軸位置
                    y = event.getY();                  //觸控的Y軸位置

                case MotionEvent.ACTION_MOVE:// 移動圖片
                    eventtime=event.getEventTime();
                    downtime=event.getDownTime();

                    mx = (int) (event.getRawX() - x);
                    my = (int) (event.getRawY() - y);
                    v.layout(mx, my, mx + v.getWidth(), my + v.getHeight());
                    break;
            }
            long sum=downtime-eventtime;
            if(sum <=0.05){
                return false;
            }
            Log.e("address", String.valueOf(mx) + "~~" + String.valueOf(my)); // 記錄目前位置
            return true;
        }
    };
}