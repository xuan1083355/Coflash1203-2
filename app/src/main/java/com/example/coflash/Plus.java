package com.example.coflash;

//import static com.google.firebase.database.snapshot.LeafNode.LeafType.String;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.coflash.ui.main.Upload;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseAuth;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.lang.String;

public class Plus extends Activity implements NumberPicker.OnValueChangeListener{
    Context context=this;
    int x_last=0;
    String x_select="0";
    EditText pushLabel;
    EditText pushTitle;
    Button push;
    ImageButton plusBack;
    int Preset=0;

    //上傳圖片+讀取圖片
    ImageButton uploadbtn,imageButton3;  //0821
    ImageView photo;
    Intent intent;
    int PICK_CONTACT_REQUEST=1;
    Uri uri;
    private StorageReference mStorageRef;
    private DatabaseReference myRef,myRef2,myRefUserAddr, myRef3;
    ImageView imageView;

    Spinner spinner;
    String classification;
    int count=0;
    String text="";
    String addr;
    String uid;
    String name;

    TextView PushTime,PushDate,ActivityStartDate,ActivityStartTime,ActivityEndDate,ActivityEndTime;

    private RecyclerView tagsRecycler,addrRecycler;
    tagsGroupAdapter groupAdapter;
    AddrGroupAdapter addrGroupAdapter;

    TextView pushPlace;
    String iname="",url="";
    boolean uploadPhoto=false;
    TextView pushTime2;
    private Activity activity;

    String pushDay ;
    String pushTime ;   //推播開始時間
    String activityStartDate ;    //活動開始日期
    String activityStartTime;    //活動開始時間
    String activityEndDate;    //活動結束日期
    String activityEndTime;    //活動結束時間
    String pushplace;
    Upload upload;

    //地址參數傳遞
    private static final int ACTIVITY_REPORT = 1000;
    LatLng latLng;
    String str1;
    Geocoder geocoder;

    int yearToday,monthToday,dayToday;
    String weekToday;
    EditText word;    //分享活動內容

    //tag通知
    List<Map<String,Object>> Tag = new ArrayList<Map<String,Object>>();
    int notion_last = 0;

    //到期通知
    Date push_Time;
    Long push_Length = Long.valueOf(259199000);
    String[] push_Date=new String[1];  //
    String push_Timepoint;
    //String push_Timepoint,push_Date;  1029原本

    //1029 珵 廣告功能
    CheckBox cb_ad;
    long diamond = 0;

    DatePickerDialog.OnDateSetListener datePickerListener;
    DatePickerDialog datePickerDialog;
    TimePickerDialog.OnTimeSetListener timePickerListener;
    TimePickerDialog timePickerDialog;
    Date now;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plus);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        spinner = (Spinner) findViewById(R.id.chooseClassification);

        pushLabel = (EditText) findViewById(R.id.pushLabel);
        pushTitle = (EditText) findViewById(R.id.pushTitle);
        push = (Button) findViewById(R.id.pushCheck);
        plusBack = (ImageButton) findViewById(R.id.plusBack);

        uploadbtn = (ImageButton) findViewById(R.id.uploadbtn);    //上傳+讀取圖片
        photo = (ImageView) findViewById(R.id.photo);
        imageView = (ImageView) findViewById(R.id.imageView);

        ActivityStartDate = (TextView) findViewById(R.id.ActivityStartDate);
        ActivityStartTime = (TextView) findViewById(R.id.ActivityStartTime);
        ActivityEndDate = (TextView) findViewById(R.id.ActivityEndDate);
        ActivityEndTime = (TextView) findViewById(R.id.ActivityEndTime);
        PushDate = (TextView) findViewById(R.id.PushDate);
        PushTime = (TextView) findViewById(R.id.PushTime);

        tagsRecycler = (RecyclerView) findViewById(R.id.tagsRecycler);
        tagsRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        addrRecycler = (RecyclerView) findViewById(R.id.addrRecycler);
        addrRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");
        pushPlace = (TextView) findViewById(R.id.pushPlace);

        imageButton3 = (ImageButton) findViewById(R.id.imageButton3);  //0821
        pushTime2=(TextView) findViewById(R.id.pushTime2);
        word=(EditText) findViewById(R.id.word);

        cb_ad = (CheckBox)  findViewById(R.id.cb_ad); //1029 珵

        //textview預設為今日日期、時間、分類為食  1102
        now = new Date();
        long now_time = now.getTime();
        SimpleDateFormat format = new SimpleDateFormat("EEE", Locale.SIMPLIFIED_CHINESE);
        String today_week = format.format(now_time);
        String tomorrow_week = format.format(now_time+259200000);
        today_week = week(today_week);
        tomorrow_week = week(tomorrow_week);
        format = new SimpleDateFormat("yyyy/MM/dd");
        String today = format.format(now_time) + " (" + today_week + ")";
        String tomorrow = format.format(now_time+259200000) + " (" + tomorrow_week + ")";
        PushDate.setText(today);
        ActivityStartDate.setText(today);
        ActivityEndDate.setText(tomorrow);
        //String nowDate = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date());    //獲得現在時間
        push_Time = now;
        String nowTime = new SimpleDateFormat("HH:mm").format(now);
        PushTime.setText(nowTime);
        ActivityStartTime.setText(nowTime);
        ActivityEndTime.setText(nowTime);
        classification="食";

        pushTime2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectTime();
            }
        });

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();//取得使用者資料
        if (user != null) {
            uid = user.getUid();//取得uid
        }

        try{
            Intent i=new Intent();
            startActivityForResult(i, 1000);
        }catch(Exception e){
            Toast.makeText(this,"YET",Toast.LENGTH_SHORT);
        }

        //建DB
        FirebaseDatabase database = FirebaseDatabase.getInstance();//連線數據庫
        myRef = database.getReference("DB");
        //child():子節點、分支
        myRef2 = myRef.child("push");
        myRefUserAddr = myRef.child("user").child(uid); //1029 珵
        myRef3 = myRef.child("tag");

        myRef.child("user").child(uid).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                } else {
                    name = String.valueOf(task.getResult().child("name").getValue());//成功找到user_name
                }
            }
        });

        firebase_select(myRef2);
        firebase_selectAddr(myRefUserAddr);
        firebase_selectTag(myRef3);

        List<String> tagList = new ArrayList<>();
        //推播按鈕
        push.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String tag_result="";
                if ((pushLabel.getText().toString().indexOf("#")) > -1 || pushLabel.getText().toString().indexOf("＃")>-1) {   //標籤有"#"
                    //以"#"分割，放入list裡
                    for (String s : pushLabel.getText().toString().trim().split("[# ＃]")) {
                        if (!(s.equals("")) && !(s.equals(" "))) {
                            tagList.add(s);
                        }
                    }

                } else {    //標籤無"#"
                    if (!(pushLabel.getText().toString().trim().equals("")) && !(pushLabel.getText().toString().trim().equals(" "))) {
                        tagList.add(pushLabel.getText().toString().trim());
                    }
                }
                //消除相同的tag
                for (int i = 0; i < tagList.size() - 1; i++) {
                    for (int j = tagList.size() - 1; j > i; j--) {
                        if (tagList.get(j).equals(tagList.get(i))) {
                            tagList.remove(j);
                        }
                    }
                }
                //若超過5個則標籤，無超過五個就推播成功，存入資料庫
                if (tagList.size() <= 5) {
                    for(String s :tagList){
                        tag_result=tag_result+"#"+s;
                    }
                    //0829 推播時間、日期為空時跳出提示框，停在推播頁面
                    pushDay = PushDate.getText().toString();   //推播開始日期
                    pushTime = PushTime.getText().toString();   //推播開始時間
                    activityStartDate = ActivityStartDate.getText().toString();    //活動開始日期
                    activityStartTime = ActivityStartTime.getText().toString();    //活動開始時間
                    activityEndDate = ActivityEndDate.getText().toString();    //活動結束日期
                    activityEndTime = ActivityEndTime.getText().toString();    //活動結束時間
                    pushplace=pushPlace.getText().toString();   //推播地點

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.TAIWAN);
                    SimpleDateFormat sdf_week = new SimpleDateFormat("EEE", Locale.SIMPLIFIED_CHINESE);
                    SimpleDateFormat sdf_time = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.TAIWAN);
                    String[] end_split = activityEndDate.split(" ");
                    long push_timepiont = push_Time.getTime();
                    long push_endtime = push_timepiont + push_Length;
                    boolean isPushEnd = false;
                    boolean isActivityEnd = false;
                    boolean isActivityEndBeforePushEnd = false;
                    if (push_endtime < new Date().getTime()){
                        isPushEnd = true;
                    }
                    try {
                        long end_timepiont = sdf_time.parse(end_split[0]+" "+activityEndTime).getTime();
                        if (end_timepiont < new Date().getTime()){
                            isActivityEnd = false;
                        }
                        if (end_timepiont < push_endtime){
                            isActivityEndBeforePushEnd = true;
                        }
                    }catch (Exception e){}



                    if(pushTitle.getText().toString().equals("")){
                        Toast.makeText(Plus.this, "請填寫推播標題", Toast.LENGTH_LONG).show();
                    }else if(pushplace.equals("地點")) {    //未選擇推播地點
                        Toast.makeText(Plus.this, "請選擇推播地點", Toast.LENGTH_LONG).show();
                    }else if(iname=="" && uri==null) {  //無上傳圖片
                        Toast.makeText(Plus.this, "請選擇圖片", Toast.LENGTH_LONG).show();
                    }else if (isPushEnd){
                        Toast.makeText(Plus.this, "請將推播開始時間往後調整\n或將推播時長延長", Toast.LENGTH_LONG).show();
                    }else if (isActivityEnd){
                        Toast.makeText(Plus.this, "請將活動結束時間往後調整", Toast.LENGTH_LONG).show();
                    }else if (isActivityEndBeforePushEnd){
                        Toast.makeText(Plus.this, "活動將在貼文消失前結束，請重新調整", Toast.LENGTH_LONG).show();
                    }else {  //推播成功
                        x_last+=1;

                        /*儲存分類、標籤、標題、使用者ID、名字(原本)
                        PlusString plusString = new PlusString(classification, pushLabel.getText().toString(), pushTitle.getText().toString(), uid, name);
                        myRef2.child(String.valueOf(x_last)).setValue(plusString); */
                        //0911 儲存分類、標籤、標題、使用者ID、名字、地址、內文
                        //Myinfo.PlusString plusString = new Myinfo.PlusString(classification, pushLabel.getText().toString(), pushTitle.getText().toString(), uid, name,
                        //pushplace,word.getText().toString());
                        Myinfo.PlusString plusString = new Myinfo.PlusString(classification, tag_result, pushTitle.getText().toString(), uid, name,
                                pushplace,word.getText().toString());

                        //儲存tag
                        for (int i = 0; i < tagList.size(); i++) {
                            boolean same = false;
                            long tagNumber = 0;
                            ArrayList<String> follower = new ArrayList<String>();
                            for(Map<String,Object> tag : Tag){
                                if(tagList.get(i).equals((String) tag.get("tagName"))){
                                    same = true;
                                    tagNumber = (long) tag.get("tagNumber");
                                    follower = (ArrayList<String>) tag.get("follower");
                                }
                            }
                            if (same){
                                myRef.child("tag").child(tagList.get(i)).child("number of post").setValue(tagNumber+1);
                                for(int j=0; j<follower.size(); j++){
                                    String uid_current = follower.get(j);
                                    String tag_current = tagList.get(i);
                                    myRef.child("user").child(uid_current).child("NoticeSetting").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if(snapshot.child("tag").getValue() != null){
                                                boolean tag = (boolean) snapshot.child("tag").getValue();
                                                if(tag){
                                                    myRef.child("user").child(uid_current).child("Notice").addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                            notion_last = (int) snapshot.getChildrenCount();
                                                            notion_last += 1;
                                                            myRef.child("user").child(uid_current).child("Notice").child(String.valueOf(notion_last)).child("content").setValue(tag_current);
                                                            myRef.child("user").child(uid_current).child("Notice").child(String.valueOf(notion_last)).child("title").setValue(pushTitle.getText().toString());
                                                            myRef.child("user").child(uid_current).child("Notice").child(String.valueOf(notion_last)).child("isRead").setValue(false);
                                                            myRef.child("user").child(uid_current).child("Notice").child(String.valueOf(notion_last)).child("type").setValue("tag");
                                                        }
                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {
                                                        }
                                                    });
                                                }
                                            }
                                            else{
                                                myRef.child("user").child(uid_current).child("NoticeSetting").child("tag").setValue(true);
                                                myRef.child("user").child(uid_current).child("Notice").addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        notion_last = (int) snapshot.getChildrenCount();
                                                        notion_last += 1;
                                                        myRef.child("user").child(uid_current).child("Notice").child(String.valueOf(notion_last)).child("content").setValue(tag_current);
                                                        myRef.child("user").child(uid_current).child("Notice").child(String.valueOf(notion_last)).child("title").setValue(pushTitle.getText().toString());
                                                        myRef.child("user").child(uid_current).child("Notice").child(String.valueOf(notion_last)).child("isRead").setValue(false);
                                                        myRef.child("user").child(uid_current).child("Notice").child(String.valueOf(notion_last)).child("type").setValue("tag");
                                                    }
                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {
                                                    }
                                                });
                                            }
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                        }
                                    });
                                }
                            }
                            else {
                                myRef.child("tag").child(tagList.get(i)).child("number of post").setValue(1);
                            }
                        }

                        //計算推播開始時間  1029
                        push_Timepoint=PushTime.getText().toString();  //推播開始時間
                        String token[]=PushDate.getText().toString().split(" ");  //推播開始日期
                        SimpleDateFormat dateParser = new SimpleDateFormat("yyyy/MM/ddHH:mm");
                        String str_pushTime = token[0]+push_Timepoint;

                        try {
                            push_Time = dateParser.parse(str_pushTime);  //轉成標準日期格式，like:Sun Oct 30 15:20:00 GMT+08:00 2022
                            System.out.println("推播時間DES"+push_Time.toString());
                        } catch (Exception e) {
                            System.out.println("轉換推播時間404"+push_Time.toString());
                            e.printStackTrace();
                        }
                        //0911 儲存:分類、標籤、標題、使用者ID、名字
                        myRef2.child(String.valueOf(x_last)).child("classification").setValue(classification);
                        //myRef2.child(String.valueOf(x_last)).child("tag").setValue(pushLabel.getText().toString());
                        myRef2.child(String.valueOf(x_last)).child("tag").setValue(tag_result);
                        myRef2.child(String.valueOf(x_last)).child("title").setValue(pushTitle.getText().toString());
                        myRef2.child(String.valueOf(x_last)).child("author").setValue(uid);
                        myRef2.child(String.valueOf(x_last)).child("name").setValue(name);
                        //儲存推播時間、日期、地點
                        myRef2.child(String.valueOf(x_last)).child("pushdate").child("push_Length").setValue(push_Length);
                        myRef2.child(String.valueOf(x_last)).child("pushdate").child("push_Timepoint").setValue(push_Time.getTime());
                        myRef2.child(String.valueOf(x_last)).child("activitydate").child("activityStartDate").setValue(activityStartDate);
                        myRef2.child(String.valueOf(x_last)).child("activitydate").child("activityStartTime").setValue(activityStartTime);
                        myRef2.child(String.valueOf(x_last)).child("activitydate").child("activityEndDate").setValue(activityEndDate);
                        myRef2.child(String.valueOf(x_last)).child("activitydate").child("activityEndTime").setValue(activityEndTime);
                        myRef2.child(String.valueOf(x_last)).child("word").setValue(word.getText().toString());
                        //myRef2.child(String.valueOf(x_last)).child("pushplace").setValue(pushplace);
                        myRef2.child(String.valueOf(x_last)).child("pushplace").child("address").setValue(pushplace);
                        myRef2.child(String.valueOf(x_last)).child("pushplace").child("latitude").setValue(latLng.latitude);
                        myRef2.child(String.valueOf(x_last)).child("pushplace").child("longitude").setValue(latLng.longitude);
                        //1121  觀看數
                        myRef2.child(String.valueOf(x_last)).child("viewCount").setValue(0);
                        //1112 珵 廣告功能 407~411
                        myRef2.child(String.valueOf(x_last)).child("addAD").setValue(cb_ad.isChecked());
                        if(cb_ad.isChecked()){
                            diamond -= 50;
                            myRefUserAddr.child("diamond").setValue(diamond);
                        }

                        if (iname != "" && url!="") {  //若選擇預設背景圖
                            uploadPhoto=true;
                            Upload upload = new Upload(iname, url);//選擇預設背景圖已有new Upload
                            myRef2.child(String.valueOf(x_last)).child("imageName").setValue(iname);
                            myRef2.child(String.valueOf(x_last)).child("imageUrl").setValue(url);
                            Toast.makeText(Plus.this, "推播成功🎉", Toast.LENGTH_SHORT).show();
                            //上傳圖片成功
                            openHome();
                        }else {
                            uploadFile();
                        }

                        text = "";  //清空上次推播資訊
                        count = 0;  //count:計算有沒有點超過五個標籤
                    }
                } else{  //超過5個標籤時提醒，並清空num重新計算下次的標籤數
                    Toast.makeText(context, "標籤至多輸入5個", Toast.LENGTH_SHORT).show();
                    tagList.clear();
                }
            }
        });


        //返回鍵
        plusBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

        //載入圖片
        uploadbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent();
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.setType("image/*");      //開啟手機的檔案內容的語法--指定為圖片類型 setType("image/*")
                intent.setAction(Intent.ACTION_GET_CONTENT);
                iname="";//0823
                url="";
                startActivityForResult(intent, 1);
            }
        });

        //地點
        pushPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAddress();
            }
        });

        //1029 珵 廣告功能
        cb_ad.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked && diamond < 50){
                    Toast.makeText(Plus.this, "鑽石不足無法使用廣告功能", Toast.LENGTH_LONG).show();
                    cb_ad.setChecked(false);
                }
            }
        });

        //下拉選單選擇"分類"
        //產生ArrayAdapter，設定資料選項string array及預設spinner的layout
        ArrayAdapter<CharSequence> adapter =
                ArrayAdapter.createFromResource(this,    //對應的Context
                        R.array.class_array,                             //資料選項內容
                        android.R.layout.simple_spinner_item);  //預設Spinner未展開時的View(預設及選取後樣式)

        //指定Spinner展開時，選項清單樣式，如果與未展開時樣式一樣可以不設定
        adapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);

        //設定好的ArrayAdapter，指定給要操作spinner
        spinner.setAdapter(adapter);
        //預設選項設為第0項
        spinner.setSelection(0, true);  //0823
        spinner.setOnItemSelectedListener(spnOnItemSelected);

        //0823
        //int[] bgcolor = new int[]{R.drawable.green, R.drawable.purple, R.drawable.orange};
        String[] bgcolor={"漸層綠","漸層紫","漸層橘"};
        imageButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(Plus.this);
                builder.setTitle("選擇背景");
                builder.setSingleChoiceItems(bgcolor, Preset, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Preset = which;//把預設值改成選擇的
                    }
                });
                builder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch(Preset){
                            case 0:
                                photo.setImageResource(R.drawable.greenbig);
                                iname="greenBig.png";
                                url="https://firebasestorage.googleapis.com/v0/b/coflash-48a90.appspot.com/o/uploads%2FgreenBig.png?alt=media&token=69d31ba8-51e0-40e1-b109-f481ce94acfc";
                                break;
                            case 1:
                                photo.setImageResource(R.drawable.purplebig);
                                iname="purpleBig.png";
                                url="https://firebasestorage.googleapis.com/v0/b/coflash-48a90.appspot.com/o/uploads%2FpurpleBig.png?alt=media&token=00fc2f6f-0ba5-47a1-b8ad-1f6949fbe131";
                                break;
                            case 2:
                                photo.setImageResource(R.drawable.orangebig);
                                iname="orangebig.png";
                                url="https://firebasestorage.googleapis.com/v0/b/coflash-48a90.appspot.com/o/uploads%2Forangebig.png?alt=media&token=f285d9df-7317-4344-b2cf-43d06a7badb6";
                                break;
                        }
                        dialog.dismiss();//結束對話框
                    }
                });
                builder.show();
            }
        });
    }

    //選擇推播時長
    public void selectTime() {
        final Dialog dialog = new Dialog(Plus.this);
        dialog.setContentView(R.layout.number_picker_dialog);
        Button setBtn = dialog.findViewById(R.id.setBtn);
        Button cancelBtn = dialog.findViewById(R.id.cancelBtn);
        /* 天數
        final NumberPicker dayPicker = dialog.findViewById(R.id.numberPicker_D);
        dayPicker.setMaxValue(2);
        dayPicker.setMinValue(0);
        dayPicker.setWrapSelectorWheel(false);
        dayPicker.setOnValueChangedListener(this);*/

        //小時
        final NumberPicker hourPicker = dialog.findViewById(R.id.numberPicker_H);
        hourPicker.setMaxValue(72);
        hourPicker.setMinValue(1);
        hourPicker.setWrapSelectorWheel(false);
        hourPicker.setOnValueChangedListener(this);
/* 分鐘
        final NumberPicker minPicker = dialog.findViewById(R.id.numberPicker_M);
        minPicker.setMaxValue(59);
        minPicker.setMinValue(1);
        minPicker.setWrapSelectorWheel(false);
        minPicker.setOnValueChangedListener(this);*/
/* 秒
        final NumberPicker secPicker = dialog.findViewById(R.id.numberPicker_S);
        secPicker.setMaxValue(59);
        secPicker.setMinValue(0);
        secPicker.setWrapSelectorWheel(false);
        secPicker.setOnValueChangedListener(this);*/
        setBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {  //1030
                //pushTime2.setText(dayPicker.getValue()+"天 " +hourPicker.getValue() + "時:" + minPicker.getValue() + "分:" + secPicker.getValue()+"秒");
                pushTime2.setText(hourPicker.getValue() + "小時");
                //push_Length = Long.valueOf(dayPicker.getValue()*86400 + hourPicker.getValue()*3600 + minPicker.getValue()*60 + secPicker.getValue())*1000;
                push_Length = Long.valueOf(hourPicker.getValue()*3600000);  //小時轉成毫秒
                dialog.dismiss();
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    @Override
    public void onValueChange(NumberPicker numberPicker, int i, int i1) {
    }

    //設定Spinner選擇後的動作
    private AdapterView.OnItemSelectedListener spnOnItemSelected
            = new AdapterView.OnItemSelectedListener() {
        public void onItemSelected(AdapterView<?> parent, View view,
                                   int pos, long id) {
            // 選項有選取時的動作
            String sPos=String.valueOf(pos);
            classification=parent.getItemAtPosition(pos).toString();
        }
        public void onNothingSelected(AdapterView<?> parent) {
            //沒有選取的動作
        }
    };



    List list;
    public List<Map<String,Object>> getItem(){
        return list;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void datePicker0(View v) {
        Calendar calendar = Calendar.getInstance();
        datePickerListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, day);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.TAIWAN);
                SimpleDateFormat sdf_week = new SimpleDateFormat("EEE", Locale.SIMPLIFIED_CHINESE);
                System.out.println("在這");
                System.out.println(sdf_week.format(calendar.getTime()));
                String week = week(sdf_week.format(calendar.getTime()));
                System.out.println(week);
                PushDate.setText(sdf.format(calendar.getTime()) + " (" + week + ")");
            }
        };
        datePickerDialog = new DatePickerDialog(Plus.this,
                datePickerListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.getDatePicker().setMinDate(new Date().getTime());
        datePickerDialog.show();
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void timePicker0(View v) {
        Calendar calendar = Calendar.getInstance();
        int hourOfDay = calendar.get(Calendar.HOUR);
        int minute = calendar.get(Calendar.MINUTE);
        timePickerListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                calendar.set(Calendar.HOUR_OF_DAY,hour);
                calendar.set(Calendar.MINUTE,minute);
                String time =String.format("%02d:%02d",hourOfDay,minute);
                push_Timepoint = String.valueOf(hourOfDay) + String.valueOf(minute);
                PushTime.setText(time);
            }
        };
        timePickerDialog = new TimePickerDialog(Plus.this,
                timePickerListener,
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true);
        timePickerDialog.show();
    }
    //活動開始日期、時間
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void datePicker(View v) {
        Calendar calendar = Calendar.getInstance();
        datePickerListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, day);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.TAIWAN);
                SimpleDateFormat sdf_week = new SimpleDateFormat("EEE", Locale.SIMPLIFIED_CHINESE);
                String week = week(sdf_week.format(calendar.getTime()));
                String end = ActivityEndDate.getText().toString();
                String[] end_split = end.split(" ");
                try {
                    long start_time = calendar.getTime().getTime();
                    long end_time = sdf.parse(end_split[0]).getTime();
                    if(start_time > end_time){
                        end_time = start_time + 86400000;
                        String end_week = sdf_week.format(end_time);
                        end_week = week(end_week);
                        end = sdf.format(end_time) + " (" + end_week + ")";
                        ActivityEndDate.setText(end);
                    }
                } catch (ParseException e) {
                }
                ActivityStartDate.setText(sdf.format(calendar.getTime()) + " (" + week + ")");
            }
        };
        datePickerDialog = new DatePickerDialog(Plus.this,
                datePickerListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void timePicker(View v) {
        Calendar calendar = Calendar.getInstance();
        timePickerListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                calendar.set(Calendar.HOUR_OF_DAY,hour);
                calendar.set(Calendar.MINUTE,minute);
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.TAIWAN);
                ActivityStartTime.setText(sdf.format(calendar.getTime()));
            }
        };
        timePickerDialog = new TimePickerDialog(Plus.this,
                timePickerListener,
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true);
        timePickerDialog.show();
    }
    //活動結束日期、時間
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void datePicker2(View v) throws ParseException {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.TAIWAN);
        SimpleDateFormat sdf_week = new SimpleDateFormat("EEE", Locale.SIMPLIFIED_CHINESE);
        datePickerListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, day);
                String week = week(sdf_week.format(calendar.getTime()));
                ActivityEndDate.setText(sdf.format(calendar.getTime()) + " (" + week + ")");
            }
        };
        datePickerDialog = new DatePickerDialog(Plus.this,
                datePickerListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        String start = ActivityStartDate.getText().toString();
        String[] start_split = start.split(" ");
        long start_time = sdf.parse(start_split[0]).getTime();
        if(start_time > new Date().getTime()){
            datePickerDialog.getDatePicker().setMinDate(start_time);
        }
        else{
            datePickerDialog.getDatePicker().setMinDate(new Date().getTime());
        }
        datePickerDialog.show();
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void timePicker2(View v) {
        Calendar calendar = Calendar.getInstance();
        timePickerListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                calendar.set(Calendar.HOUR_OF_DAY,hour);
                calendar.set(Calendar.MINUTE,minute);
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.TAIWAN);
                ActivityEndTime.setText(sdf.format(calendar.getTime()));
            }
        };
        timePickerDialog = new TimePickerDialog(Plus.this,
                timePickerListener,
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true);
        timePickerDialog.show();
    }

    public String week(String weekToday){
        String week = "";
        switch (weekToday){
            case "周一": week="一"; break;
            case "周二": week="二"; break;
            case "周三": week="三"; break;
            case "周四": week="四"; break;
            case "周五": week="五"; break;
            case "周六": week="六"; break;
            case "周日": week="日"; break;
        }
        return week;
    }

    List<AddressString> address = new ArrayList<>();
    private void firebase_selectAddr(DatabaseReference db) {
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int x_sum=(int)snapshot.getChildrenCount();    //獲取分支總數
                diamond = (long)snapshot.child("diamond").getValue(); //1029 珵
                for (DataSnapshot ds : snapshot.child("address").getChildren()) { //1029 珵
                    //讀資料庫的url資料，放進upload裡
                    LatLng reg = new LatLng((double) ds.child("LatLng").child("latitude").getValue(), (double) ds.child("LatLng").child("longitude").getValue());
                    AddressString address_data = ds.getValue(AddressString.class);
                    address_data = new AddressString(ds.getKey(), address_data.getAddressLine(), reg);  //資料庫key，詳細地址，經緯度
                    address.add(address_data);
                }
                addrGroupAdapter=new AddrGroupAdapter(context,address);
                addrGroupAdapter.setOnItemClickListener(new AddrGroupAdapter.OnItemClickListener() {
                    @Override
                    public void onClick(View view, int position) {
                        pushPlace.setText(address.get(position).getAddressLine());
                        latLng=new LatLng(address.get(position).getLatLng().latitude,address.get(position).getLatLng().longitude);
                    }
                });
                addrRecycler.setAdapter(addrGroupAdapter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    final List<String> tags=new ArrayList<>();
    //讀取資料方法
    private void firebase_select(DatabaseReference db) {
        final List<Map<String,Object>> items=new ArrayList<Map<String,Object>>();
        final List<Myinfo.PlusString> plus=new ArrayList<>();
        final List<Upload> uploadString=new ArrayList<>();
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Myinfo.PlusString push_data=ds.getValue(Myinfo.PlusString.class);
                    Upload[] upload = {ds.getValue(Upload.class)};
                    Map<String,Object> item=new HashMap<String,Object>();
                    item.put("id",ds.getKey());
                    item.put("classification", push_data.getClassification());
                    item.put("tag",push_data.getTag());
                    item.put("title",push_data.getTitle());
                    item.put("author",push_data.getAuthor());
                    item.put("name",push_data.getName());
                    item.put("url", upload[0].getImageUrl());
                    //0911
                    item.put("user_name",push_data.getName());
                    item.put("pushplace",push_data.getAddr());
                    item.put("push_context",push_data.getPushContext());
                    items.add(item);
                    //0911
                    push_data=new Myinfo.PlusString(Integer.parseInt(ds.getKey()),push_data.getClassification(),push_data.getTag(),push_data.getTitle(),push_data.getAuthor(),push_data.getName(),
                            push_data.getAddr(),push_data.getPushContext());

                    if( push_data.getTag()!=null) {
                        if ((push_data.getTag().indexOf("#")) > -1 || (push_data.getTag().indexOf("＃")) > -1 ) {
                            for (String s : push_data.getTag().trim().split("[# ＃]")) { //trim()去掉前後空格
                                if (!(s.equals("")) && !(s.equals(" "))) {
                                    tags.add(s);
                                }
                            }
                        } else {
                            if (!(push_data.getTag().equals("")) && !(push_data.getTag().equals(" "))) {
                                tags.add(push_data.getTag().trim());
                            }
                        }
                    }


                    //讀資料庫的url資料，放進upload裡
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

                    plus.add(push_data);
                    uploadString.add(upload[0]);
                    x_last = Integer.parseInt(ds.getKey());   //抓取最後一筆key值

                }
                list = items;
                groupAdapter=new tagsGroupAdapter(context,tags);
                groupAdapter.setOnItemClickListener(new tagsGroupAdapter.OnItemClickListener() {
                    @Override
                    public void onClick(View view, int position) {
                        count++;
                        String clickStr="#"+groupAdapter.getStr()[position];
                        text=pushLabel.getText().toString();
                        if(count<=5){
                            text+=clickStr;
                            clickStr="";
                        }
                        pushLabel.setText(text);
                    }
                });
                tagsRecycler.setAdapter(groupAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

    }

    private void firebase_selectTag(DatabaseReference db) {
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot DS : snapshot.getChildren()) {
                    Map<String,Object> item=new HashMap<String,Object>();
                    ArrayList<String> follower = new ArrayList<String>();
                    String tagName = DS.getKey();
                    long tagNumber;
                    try {
                        tagNumber = (long) DS.child("number of post").getValue();
                    }catch(Exception e){
                        tagNumber=0;
                    }
                    for (DataSnapshot ds : DS.child("follower").getChildren()){
                        follower.add(ds.getKey());
                    }
                    item.put("tagName", tagName);
                    item.put("tagNumber", tagNumber);
                    item.put("follower", follower);
                    Tag.add(item);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void openHome(){
        Intent intent=new Intent(this, Home.class);
        finish();
        startActivity(intent);
    }

    private void openAddress(){
        Intent intent=new Intent(this, PushAddress.class);
        startActivityForResult(intent, ACTIVITY_REPORT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == ACTIVITY_REPORT) {
                latLng = data.getParcelableExtra("address_user_selected");
                geocoder = new Geocoder(this, Locale.TRADITIONAL_CHINESE);
                try {
                    List<Address> address;
                    address = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                    pushPlace.setText(address.get(0).getAddressLine(0));
                    latLng=new LatLng(latLng.latitude,latLng.longitude);
                } catch (Exception e) {
                }
            }
        }
        switch (requestCode) {
            case 1000:  //ACTIVITY_RESULT
                //if(resultCode==RESULT_OK) {
                try {
                    data=this.getIntent();
                    Bundle bundle = data.getExtras();
                    String addr = bundle.getString("addr");
                    pushPlace.setText(addr);
                }catch(Exception e){
                    System.out.println("ERROR!!!!!!!!!!!!!!!!!!!!!!");
                }
                //}
                break;
            case 1:  //PICK_CONTACT_REQUEST
                try {
                    uri = data.getData();
                    photo.setImageURI(uri);
                }catch(Exception e){

                }
                break;

        }
    }

    //取得檔案的附檔名
    private String getFileExtension(Uri uri){
        ContentResolver contentResoler=getContentResolver();//取得檔案的附檔名方法:ContentResolver內容解析器、MimeTypeMap模仿類型圖
        MimeTypeMap mimeTypeMap=MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(getContentResolver().getType(uri));
    }

    //上傳圖片+新增圖片名、圖片在資料庫的位置  //0823
    private void uploadFile() {
        String imagename;
        StorageReference fileReference;

        if (uri != null) {
            imagename = System.currentTimeMillis() + "." + getFileExtension(uri);
            fileReference = mStorageRef.child(imagename);
            fileReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            uploadPhoto=true;
                            String urll = uri.toString();
                            Upload upload = new Upload(fileReference.toString(), urll);
                            myRef2.child(String.valueOf(x_last)).child("imageName").setValue(upload.getName());
                            myRef2.child(String.valueOf(x_last)).child("imageUrl").setValue(upload.getImageUrl());
                            myRef.child("user").child(uid).child("mission").child("plus").child("isFinish").setValue(true);
                            //上傳圖片成功
                            Toast.makeText(Plus.this, "推播成功🎉", Toast.LENGTH_SHORT).show();
                            openHome();

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            uploadPhoto=false;
                            Toast.makeText(Plus.this, "推播失敗", Toast.LENGTH_SHORT).show();

                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(Plus.this, "Fail", Toast.LENGTH_SHORT).show();
                    uploadPhoto=false;
                }
            });
        } else {
            uploadPhoto=false;
            Toast.makeText(Plus.this, "請選擇圖片", Toast.LENGTH_SHORT).show();
        }
    }
}
