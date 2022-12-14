package com.laba.partner.common.fcm;

import android.content.Context;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

public class ForceUpdateChecker {

    public static final String KEY_UPDATE_REQUIRED = "isUpdateDriverApp";
    public static final String KEY_CURRENT_VERSION = "updateVersionDriver";
    public static final String KEY_UPDATE_URL = "playStoreUrlDriver";
    public static final String KEY_PRIORITY_UPDATE = "isPriorityDriver";
    private static final String TAG = "ForceUpdateChecker";
    private OnUpdateNeededListener onUpdateNeededListener;
    private Context context;

    private ForceUpdateChecker(@NonNull Context context,
                               OnUpdateNeededListener onUpdateNeededListener) {
        this.context = context;
        this.onUpdateNeededListener = onUpdateNeededListener;
    }

    public static Builder with(@NonNull Context context) {
        return new Builder(context);
    }

    private void check() {
        final FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();

        if (remoteConfig.getBoolean(KEY_UPDATE_REQUIRED)) {
            String currentVersion = remoteConfig.getString(KEY_CURRENT_VERSION);
            String appVersion = getAppVersion(context);
            String updateUrl = remoteConfig.getString(KEY_UPDATE_URL);

            if (!TextUtils.equals(currentVersion, appVersion)
                    && onUpdateNeededListener != null) {
                onUpdateNeededListener.onUpdateNeeded(updateUrl, remoteConfig.getBoolean(KEY_PRIORITY_UPDATE));
            }
        }
    }

    private String getAppVersion(Context context) {
        String result = "";

        try {
            result = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0)
                    .versionName;
            result = result.replaceAll("[a-zA-Z]|-", "");
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, e.getMessage());
        }

        return result;
    }

    public interface OnUpdateNeededListener {
        void onUpdateNeeded(String updateUrl, boolean isPriorityUpdate);
    }

    public static class Builder {

        private Context context;
        private OnUpdateNeededListener onUpdateNeededListener;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder onUpdateNeeded(OnUpdateNeededListener onUpdateNeededListener) {
            this.onUpdateNeededListener = onUpdateNeededListener;
            return this;
        }

        public ForceUpdateChecker build() {
            return new ForceUpdateChecker(context, onUpdateNeededListener);
        }

        public ForceUpdateChecker check() {
            ForceUpdateChecker forceUpdateChecker = build();
            forceUpdateChecker.check();
            return forceUpdateChecker;

        }
    }
}

