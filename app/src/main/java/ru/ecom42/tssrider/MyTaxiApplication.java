package ru.ecom42.tssrider;

import android.app.Application;
import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.FirebaseApp;

public class MyTaxiApplication extends Application {
    @Override
    public void onCreate() {
        FirebaseApp.initializeApp(getApplicationContext());
        int nightMode = AppCompatDelegate.MODE_NIGHT_NO;
        AppCompatDelegate.setDefaultNightMode(nightMode);
        super.onCreate();
    }

}
