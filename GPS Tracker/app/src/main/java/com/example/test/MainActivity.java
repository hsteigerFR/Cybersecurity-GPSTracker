package com.example.test;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;


public class MainActivity extends AppCompatActivity {

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ///!\ THE PROGRAM REQUIRES THE GEOLOCATION PERMISSION, SET IT TO "ALLOW ALL THE TIME"

        // The program will be run as a foreground service
        boolean background = false;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (background)
        {
            // A background service will be completely invisible, and can run independently from an open
            // app. Still, the service will get disable after 1 min and the GPS location of the
            // phone will not get refreshed. This is a security measure introduced by Android.
            startService(new Intent(this,BackgroundService.class));
        }
        else
        {
            // A foreground service will run indefinitely but will be visible in the notification bar.
            // The app can be closed and the foreground service will remain. The Android geolocation
            // icon will be visible most of the time.

            // In contrast with a background service, a foreground service requires this 3 step
            // process.
            Intent serviceIntent = new Intent(this, ForegroundService.class);
            serviceIntent.putExtra("inputExtra", "Foreground Service Example in Android");
            ContextCompat.startForegroundService(this, serviceIntent);
        }
        // Once the service is created, the main app gets closed. The service will then run indepedently
        // from the app.
        MainActivity.this.finishAndRemoveTask();
    }
}