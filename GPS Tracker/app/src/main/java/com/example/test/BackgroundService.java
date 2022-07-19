package com.example.test;


import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class BackgroundService extends Service {
    //The structure of the code is close to a foreground service's.
    // What mainly differs is that no notification channel is created onStartCommand.
    //LOOPS variables
    final Handler handler_main = new Handler();
    final Handler handler_web = new Handler();
    final int delay_ms = 10000;//10s
    //WEB variables
    String url = "https://<>.ngrok.io";
    OkHttpClient client = new OkHttpClient();
    Request requestGET = new Request.Builder()
            .url(url)
            .build();
    RequestBody requestPOSTBody = new MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("Longitude", "0")
            .addFormDataPart("Latitude", "0")
            .build();
    ;
    Request requestPOST = new Request.Builder()
            .url(url)
            .post(requestPOSTBody)
            .build();
    //GEOLOC variables
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest mLocationRequest;
    protected LocationCallback mLocationCallback;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationRequest = createLocationRequest();
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    requestPOSTBody = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("Longitude", String.valueOf(location.getLongitude()))
                            .addFormDataPart("Latitude", String.valueOf(location.getLatitude()))
                            .build();
                    requestPOST = new Request.Builder()
                            .url(url)
                            .post(requestPOSTBody)
                            .build();
                }
            }
        };

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                startLocationUpdates(mLocationRequest, mLocationCallback);
                requestP();
                handler_main.postDelayed(this, delay_ms);
            }
        };
        handler_main.post(runnable);
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler_web.removeCallbacksAndMessages(null);
        handler_main.removeCallbacksAndMessages(null);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void requestG() {
        client.newCall(requestGET).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                handler_web.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast toast = Toast.makeText(getApplicationContext(), response.toString(), Toast.LENGTH_SHORT);
                        toast.show();
                    }
                });
            }
        });
    }

    private void requestP() {
        client.newCall(requestPOST).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    handler_web.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast toast = Toast.makeText(getApplicationContext(), response.toString(), Toast.LENGTH_SHORT);
                            toast.show();

                        }
                    });
                }
            }
        });
    }

    private LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(delay_ms / 2);
        mLocationRequest.setFastestInterval(delay_ms / 4);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }

    private void startLocationUpdates(LocationRequest mLocationRequest, LocationCallback mLocationCallback) {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
    }
}