package com.example.taller3;

import android.app.Application;
import android.util.Log;

import com.parse.Parse;

public class ConexionPARSE extends Application {
    //public static final String IP_NUBEPUJ = "http://10.43.100.9:1337/parse";
    public static final String IP_GCP = "https://34.30.202.225:1337/parse";

    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("pontiLive").clientKey("clave_maestra_segura") // should correspond to Application Id env variable
                .server(IP_GCP)
                .enableLocalDataStore()
                .build());

        Log.d("Parse Connection", "Parse initialized with server URL: " + IP_GCP);

    }
}