package com.laba.partner.data.sharedpref;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

public class PermissionSharedPref {

    private SharedPreferences pref;

    public PermissionSharedPref(Context context) {
        pref = context.getSharedPreferences("APP_PERMISSION", MODE_PRIVATE);
    }

    public void setBackgroundLocationPermissionAsked() {
        SharedPreferences.Editor edit = pref.edit();
        edit.putBoolean("permission", true);
        edit.commit();
        edit.apply();
    }

    public boolean isBackgroundLocationAsked() {
        return pref.getBoolean("permission", false);
    }

}
