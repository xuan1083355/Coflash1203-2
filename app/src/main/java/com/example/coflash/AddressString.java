package com.example.coflash;

import com.google.android.gms.maps.model.LatLng;

public class AddressString {
    private String title;
    private String addressLine;
    private LatLng latLng;

    public AddressString(){
    }

    public AddressString(String text_1,String text_2,LatLng latLng_1){
        this.title = text_1;
        this.addressLine = text_2;
        this.latLng = latLng_1;
    }

    public String getTitle(){
        return title;
    }

    public String getAddressLine(){
        return addressLine;
    }

    public LatLng getLatLng(){
        return latLng;
    }

}
