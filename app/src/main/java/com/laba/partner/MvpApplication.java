package com.laba.partner;

import android.app.Application;
import android.content.Context;
import android.location.Location;
import android.util.Log;

import androidx.multidex.MultiDex;

//import com.crashlytics.android.Crashlytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.facebook.accountkit.AccountKit;
import com.google.firebase.FirebaseApp;
import com.google.firebase.crashlytics.internal.common.CrashlyticsCore;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.laba.partner.common.LocaleHelper;
import com.laba.partner.common.fcm.ForceUpdateChecker;

import java.util.HashMap;
import java.util.Map;

//import com.google.firebase.crashlytics.FirebaseCrashlytics;

//import io.fabric.sdk.android.Fabric;

//import com.facebook.stetho.Stetho;
//import com.facebook.stetho.Stetho;


public class MvpApplication extends Application {

    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final String TAG = "MvpApplication";
    public static float DEFAULT_ZOOM = 15;
    public static Location mLastKnownLocation;
    private static MvpApplication mInstance;

    public static synchronized MvpApplication getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        Stetho.initializeWithDefaults(this);
        mInstance = this;
        //Initialize Facebook Account Kit
        FirebaseApp.initializeApp(this);
        AccountKit.initialize(getApplicationContext());
        // MultiDex.install(this);
        // Set up Crashlytics, disabled for debug builds
        remoteConfigUpdate();
        FirebaseCrashlytics crashlyticsKit = FirebaseCrashlytics.getInstance();
               // .Builder()
               // .core(new CrashlyticsCore.Builder().build())
               // .build();
       // if (!BuildConfig.DEBUG) Fabric.with(this, new Crashlytics());
    }

    private void remoteConfigUpdate() {
        final FirebaseRemoteConfig mRemoteConfig = FirebaseRemoteConfig.getInstance();

        Map<String, Object> remoteConfigDefaults = new HashMap();
        remoteConfigDefaults.put(ForceUpdateChecker.KEY_UPDATE_REQUIRED, false);
        remoteConfigDefaults.put(ForceUpdateChecker.KEY_CURRENT_VERSION, "1.0.0");
        remoteConfigDefaults.put(ForceUpdateChecker.KEY_PRIORITY_UPDATE, false);
        remoteConfigDefaults.put(ForceUpdateChecker.KEY_UPDATE_URL,
                "https://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName());

        mRemoteConfig.setDefaultsAsync(remoteConfigDefaults);
        mRemoteConfig.fetch(30) // fetch every minutes
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "remote config is fetched.");
                        mRemoteConfig.fetchAndActivate();
                    }
                });
    }

    public String getNewNumberFormat(double d) {
        //      String text = Double.toString(Math.abs(d));
        String text = Double.toString(d);
        int integerPlaces = text.indexOf('.');
        int decimalPlaces = text.length() - integerPlaces - 1;
        if (decimalPlaces == 2) return text;
        else if (decimalPlaces == 1) return text + "0";
        else if (decimalPlaces == 0) return text + ".00";
        else if (decimalPlaces > 2) {
            String converted = String.valueOf((double) Math.round(d * 100) / 100);
            int convertedInegers = converted.indexOf('.');
            int convertedDecimals = converted.length() - convertedInegers - 1;
            if (convertedDecimals == 2) return converted;
            else if (convertedDecimals == 1) return converted + "0";
            else if (convertedDecimals == 0) return converted + ".00";
            else return converted;
        } else return text;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase, "en"));
        MultiDex.install(newBase);
    }

}
