package com.example.coflash;

import android.Manifest;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.coflash.ui.main.Upload;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Changeaddress extends FragmentActivity {

    //ToDO
    //  1.固定"現在位置"的位置
    //  2.解決自動選擇清單某一列

    public static final int DEFAULT_UPDATE_INTERVAL = 15;
    public static final int FAST_UPDATE_INTERVAL = 5;
    private static final int PERMISSION_FINE_LOCATION = 99;
    private static final int ACTIVITY_REPORT = 1000;
    private static final int ACTIVITY_REPORT_2 = 500;

    private ImageButton close;
    private TextView tv_address1, tv_address2;
    private RadioButton rbtn_0;
    private Button btn_add;

    // 地圖和定位
    RelativeLayout map_layout;
    SupportMapFragment supportMapFragment;
    LatLng latLng = new LatLng(0, 0);
    //variable to remember if we are tracking location or not.
    boolean updateOn = false;
    //Location request is a config file for all setting related to FusedLocationProviderClient
    LocationRequest locationRequest;
    //Google's API for location service. The majority of the app functions using this class.
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationCallback locationCallBack;
    Marker markerName;
    int count = 0;

    //設定首頁地址
    Bundle args = new Bundle();
    String str1, str2, str3;

    //清單
    private RecyclerView rv_address;
    List<AddressString> address = new ArrayList<>();
    AddressAdapter adapter;
    String title;
    int resultcode;
//    private RecyclerViewReadyCallback recyclerViewReadyCallback;

    //資料庫
    DatabaseReference myRef, myRef2;
    String uid;


    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_changeaddress);

        close = findViewById(R.id.close);

        tv_address1 = findViewById(R.id.tv_address1);
        tv_address2 = findViewById(R.id.tv_address2);
        btn_add = findViewById(R.id.btn_add);
        //11-8
        Toast.makeText(this, "請開啟定位或選擇一地址", Toast.LENGTH_SHORT).show();

        //地圖和定位
        map_layout = findViewById(R.id.map_layout);
        supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.google_map);
        //set all properties of LocationRequest
        locationRequest = new LocationRequest();
        //how often does the default location check occur?
        locationRequest.setInterval(1000 * DEFAULT_UPDATE_INTERVAL);
        //how often does the location check occur when set to the most frequent update?
        locationRequest.setFastestInterval(1000 * FAST_UPDATE_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        //event that is triggered whenver the update interval is met.
        locationCallBack = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                updateGPS();
                //save the location
//                updateUIValue(locationResult.getLastLocation());
            }
        };

        close.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                openHome();
            }
        });

        //清單
        rv_address = (RecyclerView) findViewById(R.id.rv_address);
        rv_address.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        //資料庫連線
        FirebaseDatabase database = FirebaseDatabase.getInstance();//連線數據庫
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser(); //取得使用者資料
        if (user != null) {
            uid = user.getUid();//取得uid
        }
        myRef = database.getReference("DB");
        myRef2 = myRef.child("user").child(uid).child("address");
        address.add(new AddressString("","", latLng));
        adapter = new AddressAdapter(Changeaddress.this, address);
        rv_address.setAdapter(adapter);
//        firebase_select(myRef2);

        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                args.putParcelable("longLat_dataPrivider", latLng);
                Intent i = new Intent(Changeaddress.this, MapActivity.class);
                i.putExtras(args);
//                finish();
                startActivityForResult(i, ACTIVITY_REPORT);
            }
        });
//        if(resultcode != RESULT_OK){
//            updateGPS();
//        }
        updateGPS();
    } // end onCreate method

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {//捕捉返回鍵
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            openHome();//按返回鍵，則執行退出確認
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        address.clear();
//        Toast.makeText(Changeaddress.this, "onResume", Toast.LENGTH_SHORT).show();
        firebase_select(myRef2);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        Toast.makeText(Changeaddress.this, String.valueOf(resultCode), Toast.LENGTH_SHORT).show();
        if (resultCode == RESULT_OK) {
            if (requestCode == ACTIVITY_REPORT) {
                latLng = data.getParcelableExtra("latLng_from_mapactivity");
                str1 = data.getStringExtra("title_form_mapactivity");
            }
        }
        else{
            updateGPS();
        }
    }

    //讀取資料方法
    private void firebase_select(DatabaseReference db) {
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //int x_sum=(int)snapshot.getChildrenCount();    獲取分支總數
                address.add(new AddressString("","", latLng));
                for (DataSnapshot ds : snapshot.getChildren()) {
                    //讀資料庫的url資料，放進upload裡
                    LatLng reg = new LatLng((double) ds.child("LatLng").child("latitude").getValue(), (double) ds.child("LatLng").child("longitude").getValue());
                    AddressString address_data = ds.getValue(AddressString.class);
                    Map<String, Object> item = new HashMap<String, Object>();
                    item.put("title", ds.getKey());
                    item.put("addressLine", address_data.getAddressLine());
                    item.put("LatLng", reg);
                    address_data = new AddressString(ds.getKey(), address_data.getAddressLine(), reg);
                    myRef.child("user").child(uid).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(Task<DataSnapshot> task) {
                            if (!task.isSuccessful()) {
                                Log.e("firebase", "Error getting data", task.getException());
                            }
                        }
                    });
                    address.add(address_data);
                }
                adapter.notifyDataSetChanged();
//                rv_address.post(new Runnable()
//                {
//                    @Override
//                    public void run() {
//                        for (int i = 0; i < address.size(); i++) {
//                            title = address.get(i).getTitle();
//                            if (title.equals(str1)) {
//                                adapter.notifyItemChanged(i);
//                                adapter.addressChecked = adapter.maddress.get(i);
//                                adapter.changeSelect();
//                            }
//                        }
//                    }
//                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }


    // start of map and GSP
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        Toast.makeText(this, "requestCode:" + String.valueOf(requestCode) + "permissions:" + String.valueOf(permissions) + "grantResults:" + String.valueOf(grantResults), Toast.LENGTH_SHORT).show();
        switch (requestCode) {
            case PERMISSION_FINE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    map_layout.setVisibility(View.VISIBLE);
                    rbtn_0.setBackgroundColor(Color.parseColor("#4DF9C316"));
                    updateGPS();
                }
                else {
                    Toast.makeText(this, "請於設定啟用定位服務", Toast.LENGTH_SHORT).show();
                    map_layout.setVisibility(View.GONE);
                    rbtn_0.setChecked(false);
                    rbtn_0.setText("使用我目前的位置");
                    rbtn_0.setBackgroundColor(Color.parseColor("#00000000"));
                    tv_address1.setText("");
                    tv_address2.setText("");
                }
                break;
        }
    }

    private void updateGPS() {
        //get permission from the user to track GPS
        //get the current location from the fused client
        //update the UI - i.e. set all properties in their associated text view items.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(Changeaddress.this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //user provided the permission
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    //we got permission. Put the value of location. XXX into the UI components.
//                    updateUIValue(location);
                    if (location != null) {
                        supportMapFragment.getMapAsync(new OnMapReadyCallback() {
                            @Override
                            public void onMapReady(GoogleMap googleMap) {
                                //Initialize lat lng
                                latLng = new LatLng(location.getLatitude(), location.getLongitude());
                                //Create marler options
                                Geocoder geocoder = new Geocoder(Changeaddress.this, Locale.TRADITIONAL_CHINESE);
                                MarkerOptions markerOptions = null;
                                try {
                                    List<Address> address = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                    markerOptions = new MarkerOptions().position(latLng).title(address.get(0).getAddressLine(0));
                                    str1 = address.get(0).getSubThoroughfare() + " " + address.get(0).getThoroughfare();
                                    str2 = address.get(0).getAdminArea() + ", " + address.get(0).getPostalCode();
                                    str3 = "現在位置\n" + address.get(0).getAddressLine(0);
                                    tv_address1.setText(str1);
                                    tv_address2.setText(str2);
                                    rbtn_0.setText(str3);
                                    count = 0;
                                } catch (Exception e) {
                                    count += 1 ;
                                    if(count == 1){
                                        startLocationUpdates();
                                    }
                                    else{
//                                        tv_address2.setText("Unable to get street address");
                                    }
                                }
                                //Zoom map
                                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                                if (markerName != null) {
                                    markerName.remove();
                                }
                                //Add marker on map
                                markerName = googleMap.addMarker(markerOptions);
//                                for (int i = 0; i < address.size(); i++) {
//                                    title = address.get(i).getTitle();
//                                    if (title.equals(str1)) {
////                                        radioButton.getText();
//                                        Toast.makeText(Changeaddress.this, String.valueOf(radioButton.getText()), Toast.LENGTH_SHORT).show(); //結果為777號
////                                        adapter.notifyItemChanged(i);
//                                        adapter.addressChecked = adapter.maddress.get(i);
//                                        adapter.changeSelect();
//                                    }
//                                }
                            }
                        });
                    }
                }
            });
        } else {
            //permission not granted yet.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_FINE_LOCATION);
            }
        }

    }

    private void updateUIValue(Location location) {
        Geocoder geocoder = new Geocoder(Changeaddress.this, Locale.TRADITIONAL_CHINESE);
        try {
            List<Address> address = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 5);
            str1 = address.get(0).getSubThoroughfare() + " " + address.get(0).getThoroughfare();
            str2 = address.get(0).getAdminArea() + ", " + address.get(0).getPostalCode();
            str3 = "現在位置\n" + address.get(0).getAddressLine(0);
            tv_address1.setText(str1);
            tv_address2.setText(str2);
            rbtn_0.setText("現在位置\n" + address.get(0).getAddressLine(0));
        } catch (Exception e) {
            tv_address2.setText("Unable to get street address");
        }
    }

    private void startLocationUpdates() {
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallBack, null);
        updateGPS();
    }

    private void stopLocationUpdates() {
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        fusedLocationProviderClient.removeLocationUpdates(locationCallBack);
    }
    // end of map and GSP

    private void openHome(){
        if (!"".equals(tv_address1.getText())){
            Bundle args = new Bundle();
            args.putParcelable("address_user_selected", latLng);
            args.putString("addressName_user_selected", str1);
            Intent i = new Intent(this, Home.class);
            i.putExtras(args);
            setResult(RESULT_OK, i);
            this.finish();
        }
        else
        {
            Toast.makeText(this, "請開啟定位或選擇一地址", Toast.LENGTH_SHORT).show();
        }
    }


    //RecyclerView
    public class AddressAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        int selectedPosition = -1;
        private Context mContext;
        private List<AddressString> maddress = new ArrayList<>();
        private LayoutInflater layoutInflater;
        public AddressString addressChecked;
//        public RadioButton rbtn_0;
//        ItemClickListener itemClickListener;
//        public RadioButton radioButton;

        // Create constructor
        public AddressAdapter(Context context, List<AddressString> addressStrings)
        {
            mContext = context;
            maddress = addressStrings;
            layoutInflater=LayoutInflater.from(mContext);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            // 根据不同的组件类型加载不同类型的布局文件
            if(viewType == 0){
                return new ViewHolder(
                        LayoutInflater.from(Changeaddress.this)
                                .inflate(R.layout.layout_framge, parent, false)
                );
            }
            else{
                return new ViewHolder2(
                        LayoutInflater.from(Changeaddress.this)
                                .inflate(R.layout.layout_radio, parent, false)
                );
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            // 根据 position 获取布局类型 , 然后绑定数据
            if(getItemViewType(position) == 0){
                rbtn_0.setText(str3);
            }
            else{
                AddressString addressCurrent = maddress.get(position);
                String str = addressCurrent.getTitle() + "\n" + addressCurrent.getAddressLine();
                ((ViewHolder2)holder).radioButton.setText(str);
                ((ViewHolder2)holder).radioButton.setChecked(position == selectedPosition);
            }
        }

        @Override public int getItemCount()
        {
            // pass total list size
            return maddress.size();
        }

        @Override
        public int getItemViewType(int position) {
            // 返回 View 布局类型, 奇数序号组件类型为 VIEW_TYPE_2, 偶数序号组件类型为 VIEW_TYPE_1
            return position;
        }

        public void changeSelect() {
            str1 = addressChecked.getTitle();
            latLng = addressChecked.getLatLng();
            tv_address1.setText(str1);
            Geocoder geocoder = new Geocoder(Changeaddress.this, Locale.TRADITIONAL_CHINESE);
            try{
                List<Address> address = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 5);
                str2 = address.get(0).getAdminArea() + ", " + address.get(0).getPostalCode();
                tv_address2.setText(str2);
            }
            catch (Exception e){
                tv_address2.setText("Unable to get street address");
            }
            map_layout.setVisibility(View.GONE);
            rbtn_0.setChecked(false);
            rbtn_0.setText("使用我目前的位置");
            rbtn_0.setBackgroundColor(Color.parseColor("#00000000"));
            stopLocationUpdates();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            //            public RelativeLayout map_layout;
//            public RadioButton rbtn_0;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
//                map_layout = itemView.findViewById(R.id.map_layout);
                rbtn_0 = itemView.findViewById(R.id.rbtn_0);
                rbtn_0.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (ActivityCompat.checkSelfPermission(Changeaddress.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            int copyOfLastCheckedPosition = selectedPosition;
                            selectedPosition = -1;
                            notifyItemChanged(copyOfLastCheckedPosition);
                            notifyItemChanged(selectedPosition);
                            map_layout.setVisibility(View.VISIBLE);
                            rbtn_0.setBackgroundColor(Color.parseColor("#4DF9C316"));
                            startLocationUpdates();
                        }
                        else{
                            ActivityCompat.requestPermissions(Changeaddress.this, new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, PERMISSION_FINE_LOCATION) ;
                        }
                    }
                });
            }
        }

        public class ViewHolder2 extends RecyclerView.ViewHolder {
            // Initialize variable
            public RadioButton radioButton;
            public ViewHolder2(@NonNull View itemView) {
                super(itemView);
                // Assign variable
                radioButton = itemView.findViewById(R.id.radio_button);
                radioButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int copyOfLastCheckedPosition = selectedPosition;
                        selectedPosition = getAdapterPosition();
                        notifyItemChanged(copyOfLastCheckedPosition);
                        notifyItemChanged(selectedPosition);
                        AddressString addressChecked = maddress.get(selectedPosition);
                        str1 = addressChecked.getTitle();
                        latLng = addressChecked.getLatLng();
                        tv_address1.setText(str1);
                        Geocoder geocoder = new Geocoder(Changeaddress.this, Locale.TRADITIONAL_CHINESE);
                        try{
                            List<Address> address = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 5);
                            str2 = address.get(0).getAdminArea() + ", " + address.get(0).getPostalCode();
                            tv_address2.setText(str2);
                        }
                        catch (Exception e){
                            tv_address2.setText("Unable to get street address");
                        }
                        map_layout.setVisibility(View.GONE);
                        rbtn_0.setChecked(false);
                        rbtn_0.setText("使用我目前的位置");
                        rbtn_0.setBackgroundColor(Color.parseColor("#00000000"));
                        stopLocationUpdates();
                    }
                });
            }
        }
    }
}
