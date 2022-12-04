package com.example.coflash;

import android.app.Application;

import com.google.android.libraries.places.api.Places;

public class MyApplication extends Application{

    private  static MyApplication singleton;
    private Places myPlace;

    public Places getMyPlace() {
        String apikey = "AIzaSyBD94V0z8kIT5PKKHpi7XIPgljG1xMn68k";
        Places.initialize(getApplicationContext(), apikey);
        return myPlace;
    }

    public MyApplication getInstance() {
        return singleton;
    }

    public void onCreate(){
        super.onCreate();
        singleton = this;
    }

}
