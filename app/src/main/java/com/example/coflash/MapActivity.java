package com.example.coflash;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LatLng sydney = new LatLng(0, 0);
    private MapFragment mapFragment;
    private Button btn_new;
    private ImageButton close;
    TextView tv_address2;
    EditText et_title;
    String uid, str_addressLine, str_address;

    Places myPlaces;
    Location location;

    //清單
    DatabaseReference myRef, myRef2;
    List<AddressString> address = new ArrayList<>();
    List<String> addressList = new ArrayList<>();

    String title;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        btn_new = findViewById(R.id.btn_new);
        close = findViewById(R.id.close);
        tv_address2 = findViewById(R.id.tv_address2);
        et_title = findViewById(R.id.et_title);


        try {
            Intent i = getIntent();
            sydney = i.getParcelableExtra("longLat_dataPrivider");
        }
        catch (Exception e){

        }

        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        MyApplication myApplication = (MyApplication) getApplicationContext();
        myPlaces = myApplication.getMyPlace();
        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                if(place.getLatLng() != null){
                    sydney = place.getLatLng();
                }
                mapFragment.getMapAsync(MapActivity.this);
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                mapFragment.getMapAsync(MapActivity.this);
            }
        });

        FirebaseDatabase database = FirebaseDatabase.getInstance();//連線數據庫
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser(); //取得使用者資料
        if (user != null) {
            uid = user.getUid();//取得uid
        }
        myRef = database.getReference("DB");
        myRef2 = myRef.child("user").child(uid).child("address");


        btn_new.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebase_select(myRef2);
            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                closeFrame();
            }
        });

        mapFragment.getMapAsync(MapActivity.this);
//        setupAutoCompleteFragment();
    }

    private void firebase_select(DatabaseReference db) {
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //int x_sum=(int)snapshot.getChildrenCount();    獲取分支總數
                for (DataSnapshot ds : snapshot.getChildren()) {
                    //讀資料庫的url資料，放進upload裡
                    AddressString address_data = ds.getValue(AddressString.class);
                    Map<String,Object> item=new HashMap<String,Object>();
                    item.put("title", ds.getKey());
                    item.put("addressLine", address_data.getAddressLine());
                    item.put("LatLng", address_data.getLatLng());
                    address_data = new AddressString(ds.getKey(), address_data.getAddressLine(), address_data.getLatLng());
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
                for(int j=0; j<address.size(); j++){
                    AddressString addressCurrent = address.get(j);
                    String str = addressCurrent.getTitle();
                    addressList.add(str);
                }
                isWrite();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void isWrite() {
        title = et_title.getText().toString();
        boolean exist = false;
        if(title.isEmpty()){
            title = str_address;
            for(int j=0; j<addressList.size(); j++){
                String str = addressList.get(j);
                if(title.equals(str)){
                    exist = true;
                }
            }
            if(exist){
                Toast.makeText(MapActivity.this, "The title have already exist.", Toast.LENGTH_SHORT).show();
                System.out.print("The title have already exist.");
            }
            else{
                myRef.child("user").child(uid).child("address").child(title).child("addressLine").setValue(str_addressLine);
                myRef.child("user").child(uid).child("address").child(title).child("LatLng").setValue(sydney);
                Toast.makeText(MapActivity.this, title, Toast.LENGTH_SHORT).show();
            }
        }
        else{
            for(int j=0; j<addressList.size(); j++){
                String str = addressList.get(j);
                if(str.equals(title.trim())){
                    exist = true;
                }
            }
            if(exist){
                Toast.makeText(MapActivity.this, "The title have already exist.", Toast.LENGTH_SHORT).show();
                System.out.print("The title have already exist.");
            }
            else{
                myRef.child("user").child(uid).child("address").child(title).child("addressLine").setValue(str_addressLine);
                myRef.child("user").child(uid).child("address").child(title).child("LatLng").setValue(sydney);
                Toast.makeText(MapActivity.this, "Uploaded Successfully", Toast.LENGTH_SHORT).show();
            }
        }
        close();
    }

    private void closeFrame() {
        finish();
    }

    private void close(){
        Bundle args = new Bundle();
        args.putParcelable("latLng_from_mapactivity", sydney);
        args.putString("title_form_mapactivity", title);
        Intent i = new Intent(MapActivity.this, Changeaddress.class);
        i.putExtras(args);
        setResult(RESULT_OK, i);
        this.finish();
    }

    //11-8
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        MarkerOptions markerOptions = null;
        Geocoder geocoder = new Geocoder(MapActivity.this, Locale.TRADITIONAL_CHINESE);
        try{
            List<Address> address;
            address = geocoder.getFromLocation(sydney.latitude, sydney.longitude, 1);
            str_addressLine = address.get(0).getAddressLine(0);
            str_address = address.get(0).getSubThoroughfare() + " " + address.get(0).getThoroughfare();
            tv_address2.setText(str_addressLine);
            markerOptions = new MarkerOptions().position(sydney);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 15));
            mMap.addMarker(markerOptions);
        }
        catch (Exception e){
        }
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 15));
        //mMap.addMarker(markerOptions);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mMap != null) {
            mMap.clear();
        }
    }

}