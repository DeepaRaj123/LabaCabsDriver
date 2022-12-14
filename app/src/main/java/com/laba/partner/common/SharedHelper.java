package com.laba.partner.common;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.laba.partner.BuildConfig;
import com.laba.partner.LatLngPointModel;

import java.util.List;

public class SharedHelper {

    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;

    public static void putKey(Context context, String Key, String Value) {
        sharedPreferences = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.putString(Key, Value);
        editor.apply();

    }

    public static String getKey(Context context, String Key) {
        sharedPreferences = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        return sharedPreferences.getString(Key, "");
    }


    public static void putKey(Context context, String Key, Boolean Value) {
        sharedPreferences = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.putBoolean(Key, Value);
        editor.apply();
    }

    public static void putKey(Context context, String Key, Integer value) {
        sharedPreferences = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.putInt(Key, value);
        editor.apply();
    }

    public static Integer getIntKey(Context context, String Key) {
        sharedPreferences = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(Key, -1);
    }

    public static boolean getBoolKey(Context context, String Key) {
        sharedPreferences = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(Key, false);
    }

    public static String getKey(Context context, String Key, String defaultValue) {
        sharedPreferences = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        return sharedPreferences.getString(Key, defaultValue);
    }

    public static void clearSharedPreferences(Context context) {
        sharedPreferences = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        sharedPreferences.edit().clear().apply();
    }

    public static void putKeyFCM(Context context, String Key, String Value) {
        sharedPreferences = context.getSharedPreferences(BuildConfig.APPLICATION_ID + ".fcm", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.putString(Key, Value);
        editor.apply();
    }

    public static String getKeyFCM(Context context, String Key) {
        sharedPreferences = context.getSharedPreferences(BuildConfig.APPLICATION_ID + ".fcm", Context.MODE_PRIVATE);
        return sharedPreferences.getString(Key, "");
    }

    public static void putLocation(Context context, String Value) {
        sharedPreferences = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.putString("location", Value);
        editor.apply();
    }

    public static List<LatLngPointModel> getLocation(Context context) {
        sharedPreferences = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
//        return sharedPreferences.getString("location", "");
        return new Gson().fromJson(sharedPreferences.getString("location", ""),
                new TypeToken<List<LatLngPointModel>>() {
                }.getType());
    }

}
