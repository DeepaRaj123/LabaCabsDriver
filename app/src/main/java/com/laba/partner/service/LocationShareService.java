package com.laba.partner.service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.RequiresPermission;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.laba.partner.R;
import com.laba.partner.common.Constants;
import com.laba.partner.common.SharedHelper;
import com.laba.partner.data.network.APIClient;
import com.laba.partner.ui.activity.main.MainActivity;

import java.util.HashMap;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class LocationShareService extends Service {

    private static final String TAG = "LocationShareService";
    String NOTIFICATION_CHANNEL_ID = "com.zippy.partner.service";
    String NOTIFICATION_CHANNEL_ID2 = "com.zippy.partner.service";
    private CompositeDisposable disposable;

    private int UPDATE_INTERVAL_IN_MILLISECONDS = 5000;
    private int FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    private LocationRequest locationRequest = null;
    private LocationSettingsRequest mLocationSettingsRequest = null;
    private LocationCallback mLocationCallback = null;
    private FusedLocationProviderClient mFusedLocationClient = null;
    private SettingsClient mSettingsClient = null;
    private NotificationManager manager = null;
    private NotificationManager manageronline = null;
    private String serviceStatus;
    private String is_OnRide;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotification();

        serviceStatus = SharedHelper.getKey(this, "serviceStatus");
        is_OnRide = SharedHelper.getKey(this, "is_OnRide");

        if (serviceStatus.equalsIgnoreCase("active")) {
            createOnlineNotification();
        }

        if (is_OnRide != null && !is_OnRide.equals("null") && !is_OnRide.isEmpty()) {
            createRideNotification();
        }

        //buildSettingRequest();
        createLocationCallback();
        return START_STICKY;
    }

    @RequiresPermission(anyOf = {"android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"})
    private void buildSettingRequest() {
        try {
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            mSettingsClient = LocationServices.getSettingsClient(this);
            locationRequest = new LocationRequest();
            locationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
            locationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
            builder.addLocationRequest(locationRequest);
            mLocationSettingsRequest = builder.build();
            try {

                mSettingsClient.checkLocationSettings(mLocationSettingsRequest).addOnSuccessListener(settingsResponse -> {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        stopSelf();
                        return;
                    }
                    mFusedLocationClient.requestLocationUpdates(locationRequest,
                            mLocationCallback, Looper.myLooper());
                }).addOnFailureListener(e -> {
                    Log.e(TAG, e.toString());
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Log.d(TAG, "locationResult" + locationResult);
                if (locationResult.getLastLocation() != null)
                    updateLocation(locationResult.getLastLocation());
            }
        };
    }

    private void updateLocation(Location mLastLocation) {
        Log.d(TAG, "LAT" + mLastLocation.getLatitude());
        Log.d(TAG, "Lng" + mLastLocation.getLongitude());
        HashMap map = new HashMap<>();
        map.put("latitude", mLastLocation.getLatitude());
        map.put("longitude", mLastLocation.getLongitude());
        APIClient.getAPIClient().postCurrentLocation(map).subscribeOn(Schedulers.io()).subscribe(new DisposableSingleObserver<Object>() {
            @Override
            public void onSuccess(Object o) {
                Log.d(TAG, o.toString());
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, e.toString());
            }
        });

    }

    private void createNotification() {
        Log.d(TAG, "CreateNotification");
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            String channelName = "Current location sharing";
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH);
            channel.setLightColor(Color.BLUE);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            assert manager != null;
            manager.createNotificationChannel(channel);
            Notification notification = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                    .setOngoing(true)
                    .setSmallIcon(R.drawable.noti_icon_new)
                    .setContentTitle(getString(R.string.app_name) + "- Current location sharing")
                    .setPriority(NotificationManager.IMPORTANCE_MAX)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .setOngoing(true)
                    .setAutoCancel(false)
                    .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(), 0))
                    .build();
            notification.flags = Notification.FLAG_ONGOING_EVENT;
            startForeground(2, notification);
        } else {
            Intent notificationIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                    notificationIntent, 0);

            Notification notification = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(R.drawable.noti_icon_new)
                    .setColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText(getString(R.string.app_name) + "- Current location sharing")
                    .setContentIntent(pendingIntent).build();
            startForeground(2, notification);
        }
    }

    private void onStopUpdate() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        stopForeground(true);
        onStopUpdate();
        super.onDestroy();
    }


    public void createOnlineNotification() {
        Log.d(TAG, "CreateNotification");
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            String channelName = "You are currently online";
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID2, channelName, NotificationManager.IMPORTANCE_HIGH);
            channel.setLightColor(Color.BLUE);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            manageronline = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            assert manageronline != null;
            manageronline.createNotificationChannel(channel);
            Notification notification = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID2)
                    .setOngoing(true)
                    .setSmallIcon(R.drawable.noti_icon_new)
                    .setContentTitle(getString(R.string.app_name) + "- You are currently online")
                    .setPriority(NotificationManager.IMPORTANCE_MAX)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .setOngoing(true)
                    .setAutoCancel(false)
                    .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(), 0))
                    .build();
            notification.flags = Notification.FLAG_ONGOING_EVENT;
            startForeground(10, notification);
        } else {
            Intent notificationIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                    notificationIntent, 0);

            Notification notification = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID2)
//                    .setSmallIcon(R.drawable.ic_push)
                    .setSmallIcon(R.drawable.noti_icon_new)
                    .setColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText(getString(R.string.app_name) + "- You are currently online")
                    .setContentIntent(pendingIntent).build();
            startForeground(10, notification);
        }
    }


    public void createRideNotification() {
        {
            Log.d(TAG, "CreateNotification");
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
                String channelName = "You are currently online";
                NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID2, channelName, NotificationManager.IMPORTANCE_HIGH);
                channel.setLightColor(Color.BLUE);
                channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
                manageronline = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                assert manageronline != null;
                manageronline.createNotificationChannel(channel);
                Notification notification = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID2)
                        .setOngoing(true)
                        .setSmallIcon(R.drawable.noti_icon_new)
                        .setContentTitle(getString(R.string.app_name) + "- You are currently online")
                        .setPriority(NotificationManager.IMPORTANCE_MAX)
                        .setCategory(Notification.CATEGORY_SERVICE)
                        .setOngoing(true)
                        .setAutoCancel(false)
                        .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(), 0))
                        .build();
                notification.flags = Notification.FLAG_ONGOING_EVENT;
                startForeground(10, notification);
            } else {
                Intent notificationIntent = new Intent(this, MainActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                        notificationIntent, 0);

                Notification notification = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID2)
                        .setSmallIcon(R.drawable.noti_icon_new)
                        .setColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(getString(R.string.app_name) + "- You are currently online")
                        .setContentIntent(pendingIntent).build();
                startForeground(10, notification);
            }
        }
    }

}
