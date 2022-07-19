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

public class ForegroundService extends Service {
    //LOOPS variables
    final Handler handler_main = new Handler();
    final Handler handler_web = new Handler();
    final int delay_ms = 10000;//10s, the duration between sent messages to the server
    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    //WEB variables
    ///!\THE FOLLOWING URL SHOULD CORRESPOND TO THE CURRENT NGROK SERVER's :
    //It MUST start with an https://
    String url = "https://<>.ngrok.io";
    OkHttpClient client = new OkHttpClient();
    Request requestGET = new Request.Builder()
            .url(url)
            .build();
    //This is the POST request structure, its content is the phone longitude and latitude
    //The server will also retrieve the ID of the connected phone and the POST time
    RequestBody requestPOSTBody = new MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("Longitude", "0")
            .addFormDataPart("Latitude", "0")
            .build();;
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
    // This function starts with the application
    public int onStartCommand(Intent intent, int flags, int startId) {
        // The following instructions enable a foreground service through the creation of a
        // notification channel.
        String input = intent.getStringExtra("");
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("")
                .setContentText(input)
                .setSmallIcon(R.drawable.ic_android_black_24dp)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);

        // Setting up of the geolocation loop :
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationRequest = createLocationRequest();
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    //Toast creates a notification on the phone, it shows what is sent to the server
                    //Toast.makeText(MainActivity.this, " Current location : Latitude is " + location.getLatitude() + " and Longitude is: " + location.getLongitude(), Toast.LENGTH_SHORT).show();
                    requestPOSTBody = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("Longitude", String.valueOf(location.getLongitude()))
                            .addFormDataPart("Latitude", String.valueOf(location.getLatitude()))
                            .build();
                    requestPOST = new Request.Builder()
                            .url(url)
                            .post(requestPOSTBody)
                            .build();
                    //The POST form is filled with the current phone geolocation data
                }
            }
        };

        // Runnable enables to create a loop to regularly send a message to the server,
        // with delay "delay_ms"
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                startLocationUpdates(mLocationRequest,mLocationCallback);
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

    //This function enables to get a message from the server
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
                        //A notification showing the GET response string can be shown on the phone
                        //Toast toast = Toast.makeText(getApplicationContext(),response.toString(),Toast.LENGTH_SHORT);
                        //toast.show();
                    }
                });
            }
        });
    }

    // This function enables to send a message to the server
    private void requestP(){
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
                            //A notification showing the POST response string can be shown on the phone
                            //Toast toast = Toast.makeText(getApplicationContext(),response.toString(),Toast.LENGTH_SHORT);
                            //toast.show();

                        }
                    });
                }
            }
        });
    }
    // This function sets up geolocation parameters, as well as its update time
    private LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(delay_ms/2);
        mLocationRequest.setFastestInterval(delay_ms/4);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }

    private void startLocationUpdates(LocationRequest mLocationRequest, LocationCallback mLocationCallback) {
        // This function enables to update the geolocation data
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
    }

    // The setting up of a notification channel enables to finalize the creation of a Foreground service
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
}