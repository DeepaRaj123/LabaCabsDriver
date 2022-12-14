package com.laba.partner.common;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.laba.partner.LatLngPointModel;
import com.laba.partner.data.network.model.LatLngFireBaseDB;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.laba.partner.MvpApplication.mLastKnownLocation;
import static com.laba.partner.base.BaseActivity.DATUM;
import static com.laba.partner.common.SharedHelper.getLocation;
import static com.laba.partner.common.SharedHelper.putLocation;
import static com.laba.partner.ui.activity.main.MainActivity.myLocationCalculationCheck;

public class GPSTracker extends Service {

    private static final String TAG = "RRR GPSTracker";
    private static final int LOCATION_INTERVAL = 2000;     //      2 sec
    private static final float LOCATION_DISTANCE = 3f;     //      10 feet
    LocationListener[] mLocationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };
    private String key;
    private int callCheckStatusApi = 0;
    private LocationManager locationManager = null;

    private void saveLocationToFireBaseDB(double lat, double lng) {
        String refPath = "loc_p_" + DATUM.getProviderId();
        System.out.println("RRR GPSTracker.saveLocationToFireBaseDB :: " + refPath);
        if (!refPath.equals("loc_p_0"))
            try {
                DatabaseReference mLocationRef = FirebaseDatabase.getInstance().getReference(refPath);
                key = key == null ? mLocationRef.push().getKey() : key;
                mLocationRef.setValue(new LatLngFireBaseDB(lat, lng));
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    private void locationProcessing(Location location) {
        List<LatLngPointModel> pointModels = getLocation(this);

        if (pointModels == null)
            pointModels = new ArrayList<>();

        LatLngPointModel latLngPoint = new LatLngPointModel();
        latLngPoint.setId(pointModels.size());
        latLngPoint.setLat(location.getLatitude());
        latLngPoint.setLng(location.getLongitude());
        latLngPoint.setTimeStamp(getCurrentTime());
        pointModels.add(latLngPoint);

        putLocation(this, new Gson().toJson(pointModels));

        System.out.println("  TimeStamp = " + latLngPoint.getTimeStamp());

        if (getLocation(this).size() > 1)
            System.out.println("  Distance = " + calculateDistance(getLocation(this)));
        else System.out.println("  First Time");
    }

    private double calculateDistance(List<LatLngPointModel> locationEntities) {

        double totalDistance = 0;
        for (int i = 0; i + 1 < locationEntities.size(); i++)
            totalDistance += addDistance(locationEntities.get(i), locationEntities.get(i + 1));
        totalDistance = (totalDistance * (1 / 1000.0));
        totalDistance = round(totalDistance);

        //  Toast.makeText(this, "Distance ::: " + totalDistance, Toast.LENGTH_SHORT).show();

        return totalDistance;
    }

    private double addDistance(LatLngPointModel a, LatLngPointModel b) {
        Location startPoint = new Location("start");
        startPoint.setLatitude(a.getLat());
        startPoint.setLongitude(a.getLng());

        Location endPoint = new Location("end");
        endPoint.setLatitude(b.getLat());
        endPoint.setLongitude(b.getLng());

        return startPoint.distanceTo(endPoint);
    }

    private double round(double value) {
        long factor = (long) Math.pow(10, 2);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    @SuppressLint("SimpleDateFormat")
    private String getCurrentTime() {
        return new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime());
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");

        callCheckStatusApi = 0;
        initializeLocationManager();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "location provider requires ACCESS_FINE_LOCATION | ACCESS_COARSE_LOCATION");
            return;
        }

        try {
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    LOCATION_INTERVAL,
                    LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }

        try {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    LOCATION_INTERVAL,
                    LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        callCheckStatusApi = 0;
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        if (locationManager != null) {
            for (LocationListener mLocationListener : mLocationListeners)
                try {
                    locationManager.removeUpdates(mLocationListener);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listeners, ignore", ex);
                }
        }
    }

    private void initializeLocationManager() {
        Log.d(TAG, "initializeLocationManager");
        if (locationManager == null)
            locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
    }

    public class LocationListener implements android.location.LocationListener {

        LocationListener(String provider) {
            Log.d(TAG, "RRR GPSTracker.LocationListener  " + provider);
        }

        @Override
        public void onLocationChanged(final Location location) {
            Log.d(TAG, "RRR GPSTracker.onLocationChanged: " + location);

            //      TODO: Commented because location updating is done here
            //      ....****....
            //      EventBus.getDefault().post(location);

            if (myLocationCalculationCheck)
                locationProcessing(location);

            if (DATUM == null) return;

//            if (mLocationRef == null) {
//                String refPath = "loc_p_" + DATUM.getProviderId();
//                if (!refPath.equals("loc_p_0"))
//                    mLocationRef = FirebaseDatabase.getInstance().getReference(refPath);
//            }

            if (DATUM.getStatus().equalsIgnoreCase("ACCEPTED")
                    || DATUM.getStatus().equalsIgnoreCase("STARTED")
                    || DATUM.getStatus().equalsIgnoreCase("ARRIVED")
                    || DATUM.getStatus().equalsIgnoreCase("PICKEDUP")
                /*|| DATUM.getStatus().equalsIgnoreCase("DROPPED")*/) {

                callCheckStatusApi++;
                mLastKnownLocation = location;
                if (callCheckStatusApi % 8 == 0) //   Call for every 8 seconds
                    getApplicationContext().sendBroadcast(new Intent("INTENT_FILTER"));
                saveLocationToFireBaseDB(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.d(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.d(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            System.out.println(TAG + "onStatusChanged: provider = [" + provider + "], status = [" + status + "], extras = [" + extras + "]");
        }
    }

}
