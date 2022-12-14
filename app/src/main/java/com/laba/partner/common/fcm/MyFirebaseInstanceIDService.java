package com.laba.partner.common.fcm;

import android.annotation.SuppressLint;
import android.provider.Settings;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.laba.partner.common.SharedHelper;

public class MyFirebaseInstanceIDService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseIIDService";

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    // [START refresh_token]
    @Override
    public void onNewToken( String deviceToken) {
        // Get updated InstanceID token.
        //String deviceToken = FirebaseInstanceId.getInstance().getToken();
        @SuppressLint("HardwareIds")
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        Log.d(TAG, "deviceId: " + deviceId);
        Log.d(TAG, "FCM Token: " + deviceToken);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(deviceToken, deviceId);
    }
    // [END refresh_token]

    /**
     * Persist token to third-party servers.
     * <p>
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param deviceToken The new token.
     */
    private void sendRegistrationToServer(String deviceToken, String deviceId) {
        SharedHelper.putKeyFCM(this, "device_token", deviceToken);
        SharedHelper.putKeyFCM(this, "device_id", deviceId);
        // TODO: Implement this method to send token to your app server.
    }
}
