package com.example.taller3;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.parse.Parse;

public class ConexionPARSE extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Inicialización de Firebase
        FirebaseApp.initializeApp(this);
        FirebaseAuth.getInstance();

    }
}