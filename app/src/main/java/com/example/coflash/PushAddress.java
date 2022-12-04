package com.example.coflash;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.Status;
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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.CollationElementIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PushAddress extends FragmentActivity {
    public static final int DEFAULT_UPDATE_INTERVAL = 15;
    public static final int FAST_UPDATE_INTERVAL = 5;
    private static final int PERMISSION_FINE_LOCATION = 99;
    private LatLng sydney = new LatLng(22.734463419825964, 120.28429407293929);

    private ImageButton addrBack, gps;
    private Button addrCheck, currentAddr;
    private TextView addrC;
    private String addr, uid;
    private DatabaseReference myRef, myRef2;

    //清單0914
    private RecyclerView RV_addr;
    List<AddressString> address = new ArrayList<>();

    // 地圖和定位
    //SupportMapFragment supportMapFragment;
    LatLng latLng;

    //variable to remember if we are tracking location or not.
    boolean updateOn = false;

    //Location request is a config file for all setting related to FusedLocationProviderClient
    LocationRequest locationRequest;

    //Google's API for location service. The majority of the app functions using this class.
    FusedLocationProviderClient fusedLocationProviderClient;

    LocationCallback locationCallBack;
    Places myPlaces;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pushaddress);

        addrBack = (ImageButton) findViewById(R.id.addrBack);
        addrCheck = (Button) findViewById(R.id.addrCheck);
        currentAddr = (Button) findViewById(R.id.currentAddr);
        addrC = (TextView) findViewById(R.id.addrC);
        //supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.googlemap);
        gps = (ImageButton) findViewById(R.id.gps);

        //資料庫連線
        FirebaseDatabase database = FirebaseDatabase.getInstance();//連線數據庫
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser(); //取得使用者資料
        if (user != null) {
            uid = user.getUid();//取得uid
        }
        myRef = database.getReference("DB");
        myRef2 = myRef.child("user").child(uid).child("address");
        firebase_select(myRef2);

        //搜尋位址
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.search);
        MyApplication myApplication = (MyApplication) getApplicationContext();
        myPlaces = myApplication.getMyPlace();

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                if (place.getLatLng() != null) {
                    sydney = place.getLatLng();
                    latLng= place.getLatLng();
                    Geocoder geocoder = new Geocoder(PushAddress.this, Locale.TRADITIONAL_CHINESE);
                    try {
                        List<Address> address;
                        address = geocoder.getFromLocation(sydney.latitude, sydney.longitude, 1);
                        addrC.setText(address.get(0).getAddressLine(0));    //807台灣高雄市三民區建工路415號
                        addr = address.get(0).getSubThoroughfare() + " " + address.get(0).getThoroughfare();  //415號 建工路
                        currentAddr.setText(addr);
                        currentAddr.setTextColor(Color.parseColor("#4485F3"));
                        //System.out.println(addr);
                    } catch (Exception e) {
                    }
                }
            }


            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                //mapFragment.getMapAsync(MapActivity.this);
            }
        });

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
                updateUIValue(locationResult.getLastLocation());
            }
        };

        //返回按鈕
        addrBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        //確認按鈕
        addrCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle args = new Bundle();
                args.putParcelable("address_user_selected", latLng);
                Intent i = new Intent(PushAddress.this, Plus.class);
                i.putExtras(args);
                setResult(RESULT_OK, i);
                PushAddress.this.finish();
            }
        });
        //0821 點擊圖片按鈕GPS定位
        gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateGPS();//已包含updateUIValue();
            }
        });

        updateGPS();
    }

    //讀取資料方法
    private void firebase_select(DatabaseReference db) {
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int x_sum=(int)snapshot.getChildrenCount();    //獲取分支總數
                Map<String, Object> items = new HashMap<String, Object>();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    //讀資料庫的url資料，放進upload裡
                    LatLng reg = new LatLng((double) ds.child("LatLng").child("latitude").getValue(), (double) ds.child("LatLng").child("longitude").getValue());
                    AddressString address_data = ds.getValue(AddressString.class);
                    address_data = new AddressString(ds.getKey(), address_data.getAddressLine(), reg);
                    address.add(address_data);
                }
                //設置RecycleView
                RV_addr = (RecyclerView) findViewById(R.id.RV_addr);
                RV_addr.setLayoutManager(new LinearLayoutManager(PushAddress.this, LinearLayoutManager.VERTICAL, false));
                RV_addr.setAdapter(new PushAddress.CommonAddressAdapter(PushAddress.this, address));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

    }

    private void openPlus() {
        Intent intent = new Intent(this, Plus.class);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSION_FINE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    updateGPS();
                } else {
                    Toast.makeText(this, "This app requests permission to be granted in order to work property", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }
    private boolean gpsClick=false;
    private void updateGPS() {
        //get permission from the user to track GPS
        //get the current location from the fused client
        //update the UI - i.e. set all properties in their associated text view items.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(PushAddress.this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //user provided the permission
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    //we got permission. Put the value of location. XXX into the UI components.
                    updateUIValue(location);
                    currentAddr.setTextColor(Color.parseColor("#4485F3"));
                    setGpsClick(true);
                    setClickPos(-1);

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
        try {
            latLng = new LatLng(location.getLatitude(), location.getLongitude());
            Geocoder geocoder = new Geocoder(PushAddress.this, Locale.TRADITIONAL_CHINESE);
            List<Address> address = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 5);   //取得位址
            currentAddr.setText("目前位置");
            addrC.setText(address.get(0).getAddressLine(0));
        } catch (Exception e) {
            addrC.setText("載入目前位置中...");
        }
    }

    private void startLocationUpdates() {
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallBack, null);
        updateGPS();
    }

    private void stopLocationUpdates() {
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        fusedLocationProviderClient.removeLocationUpdates(locationCallBack);
    }

    //RecyclerView 0827
    public class CommonAddressAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private Context mContext;
        private List<AddressString> maddress = new ArrayList<>();
        private LayoutInflater layoutInflater;

        // Create constructor
        public CommonAddressAdapter(Context context, List<AddressString> addressStrings) {
            mContext = context;
            maddress = addressStrings;
            layoutInflater = LayoutInflater.from(mContext);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new PushAddress.CommonAddressAdapter.ViewHolder(
                    LayoutInflater.from(PushAddress.this)
                            .inflate(R.layout.addr_recycleview, parent, false));
        }

        //TODO:修改點擊常用地址後，再點GPS的顏色沒變到的BUG(常用地址應變回黑色)
        //TODO:有些搜尋的地點是null
        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
            final AddressString addressCurrent = maddress.get(position);
            System.out.println("pos:"+gpsClick);
            //未點擊位置時文字變色
            if(getGpsClick() || getClickPos()<0){
                currentAddr.setTextColor(Color.parseColor("#4485F3"));
                ((ViewHolder) holder).tv_addr.setTextColor(Color.parseColor("#000000"));
            }else if(!getGpsClick() && position==getClickPos()){    //getClickPos()取得剛才點擊時儲存的位置；gps按鈕未被點擊且物件位置==點擊位置時，文字為藍色
                currentAddr.setTextColor(Color.parseColor("#000000"));
                ((ViewHolder) holder).tv_addr.setTextColor(Color.parseColor("#4485F3"));
            }else{
                currentAddr.setTextColor(Color.parseColor("#000000"));
                ((ViewHolder) holder).tv_addr.setTextColor(Color.parseColor("#000000"));
            }
            //changeColor(holder,position);

            ((ViewHolder) holder).tv_addr.setText(addressCurrent.getTitle());
            ((ViewHolder) holder).tv_addr2.setText(addressCurrent.getAddressLine());
            ((ViewHolder) holder).relativeLayout3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setGpsClick(false);

                    setClickPos(position);  //設置position-點擊的位置
                    currentAddr.setTextColor(Color.parseColor("#000000"));      //recyclerview的物件被點擊時，目前位置顏色設為黑
                    currentAddr.setText(addressCurrent.getTitle());
                    addrC.setText(addressCurrent.getAddressLine());
                    latLng = addressCurrent.getLatLng();

                    // 通知資料被變動，更新 ListView 顯示內容
                    notifyDataSetChanged();
                }
            });

        }
        
        @Override
        public int getItemCount() {
            // pass total list size
            return maddress.size();
        }

        @Override
        public int getItemViewType(int position) {
            // 返回 View 布局类型, 奇数序号组件类型为 VIEW_TYPE_2, 偶数序号组件类型为 VIEW_TYPE_1
            return position;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tv_addr,tv_addr2;
            RelativeLayout relativeLayout3;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tv_addr = itemView.findViewById(R.id.tv_addr);
                tv_addr2 = itemView.findViewById(R.id.tv_addr2);
                relativeLayout3=itemView.findViewById(R.id.relativeLayout3);
            }
        }

    }
    int clickPos;   //紀錄點擊位置
    public void setClickPos(int clickPos){
        this.clickPos=clickPos;
    }

    public int getClickPos(){
        return clickPos;
    }
    public void setGpsClick(boolean gpsClick){
        this.gpsClick=gpsClick;
    }
    public boolean getGpsClick(){
        return this.gpsClick;
    }
}

