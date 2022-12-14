package com.laba.partner.common.fcm;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.laba.partner.R;
import com.laba.partner.common.Utilities;
import com.laba.partner.ui.activity.main.MainActivity;

import java.util.List;
import java.util.Map;

import es.dmoral.toasty.Toasty;

public class MyFireBaseMessagingService extends FirebaseMessagingService {

    public static final String INTENT_FILTER = "INTENT_FILTER";
    private static final String TAG = "MyFireBaseMessagingService";
    int notificationId = 0;

    public static boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground = false;
            }
        }

        return isInBackground;
    }

//    private void sendNotification(String messageBody, boolean moveToScheduledWindow) {
//
//        PendingIntent pendingIntent;
//        if(moveToScheduledWindow){
////            Intent intent = new Intent(this, OnBoardActivity.class);
////            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
////            pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
////                    PendingIntent.FLAG_ONE_SHOT);
//
//            Intent intent = new Intent(this, MainActivity.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            intent.putExtra("move_to","now");
//            pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
//                    PendingIntent.FLAG_ONE_SHOT);
//        }
//        else{
//            Intent intent = new Intent(this, MainActivity.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
//                    PendingIntent.FLAG_ONE_SHOT);
//        }
//
////        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
////        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
////        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//
//
//        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, "PUSH");
//        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
//        inboxStyle.addLine(messageBody);
//
//        long when = System.currentTimeMillis();         // notification time
//
//
//        // Sets an ID for the notification, so it can be updated.
//        int notifyID = 1;
//        String CHANNEL_ID = "my_channel_01";// The id of the channel.
//        CharSequence name = "Channel human readable title";// The user-visible name of the channel.
//        int importance = NotificationManager.IMPORTANCE_HIGH;
//
//
//        Notification notification;
//        notification = mBuilder.setSmallIcon(R.drawable.noti_icon_new).setTicker(getString(R.string.app_name)).setWhen(when)
////                .setAutoCancel(true)
//                .setContentTitle(getString(R.string.app_name))
//                .setContentIntent(pendingIntent)
//                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
//                .setStyle(new NotificationCompat.BigTextStyle().bigText(messageBody))
//                .setWhen(when)
//                .setSmallIcon(getNotificationIcon(mBuilder))
//                .setContentText(messageBody)
//                .setChannelId(CHANNEL_ID)
//                .setDefaults(Notification.DEFAULT_VIBRATE
//                        | Notification.DEFAULT_LIGHTS
//                )
//                .build();
//
//        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            android.app.NotificationChannel mChannel = new android.app.NotificationChannel(CHANNEL_ID, name, importance);
//            // Create a notification and set the notification channel.
//            notificationManager.createNotificationChannel(mChannel);
//        }
//
//        notificationManager.notify(0, notification);
//    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.e("checkingtext2", String.valueOf(remoteMessage));

        if (remoteMessage.getData() != null) {
            System.out.println(TAG + "From: " + remoteMessage.getFrom());
            System.out.println(TAG + "getData: " + remoteMessage.getData());
            Log.e("TAGG", "onMessageReceived :"+ remoteMessage.getData());

            if(remoteMessage.getData().get("message").equalsIgnoreCase(
                    "User Cancelled")){
                sendBroadcast(new Intent("UserCancelled").putExtra("Cancelled",true));
            }

            sendBroadcast(new Intent(INTENT_FILTER));
            String message = remoteMessage.getData().get("message");
             sendBroadcast(new Intent(INTENT_FILTER).putExtra("msg",message));

            Map<String, String> data = remoteMessage.getData();
            boolean pushToProvider = data.containsValue("PushToProvider");

            if (pushToProvider) {
                sendNotification(remoteMessage.getData().get("message"), true);

            } else {
                sendNotification(remoteMessage.getData().get("message"), false);
            }

//            sendNotification(message);
            Log.e("TAGG", "onMessageReceived :");
//            sendBroadcast(new Intent(INTENT_FILTER).putExtra("message",message));
            sendBroadcast(new Intent(INTENT_FILTER));

            if (!isAppIsInBackground(getApplicationContext())) {
                // app is in foreground, broadcast the push message
                Utilities.printV(TAG, "foreground");
            } else {
                Utilities.printV(TAG, "background");
                if (message.equalsIgnoreCase("New Incoming Ride")) {
                    Intent mainIntent = new Intent(this, MainActivity.class);
                    mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(mainIntent);
                }
            }

        } else {
            System.out.println(TAG + "FCM Notification failed");
        }

    }

    private void sendNotification(String messageBody, boolean moveToScheduledWindow) {


        if(messageBody.equalsIgnoreCase(
                "User Cancelled")){
            messageBody = "User Request Cancelled";
       }
       else if(messageBody.equalsIgnoreCase(
                "User Cancelled the Ride")){
            messageBody = "User Ride Cancelled";
        }

//        PendingIntent pendingIntent;
//        Intent intent = new Intent(this, MainActivity.class);
//        if(moveToScheduledWindow){
////            Intent intent = new Intent(this, OnBoardActivity.class);
////            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
////            pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
////                    PendingIntent.FLAG_ONE_SHOT);
//
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            intent.putExtra("move_to","now");
//            pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
//                    PendingIntent.FLAG_ONE_SHOT);
//        }
//        else{
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            intent.putExtra("move_to","not_now");
//            pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
//                    PendingIntent.FLAG_ONE_SHOT);
//        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("move_to", "not_now");
        /* Request code */
        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                    PendingIntent.FLAG_IMMUTABLE);
        } else {
            pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                    PendingIntent.FLAG_ONE_SHOT);        }



        String channelId = getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(R.raw.alert_tone);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.noti_icon_new)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(notificationId++ /* ID of notification */, notificationBuilder.build());

    }

    private int getNotificationIcon(NotificationCompat.Builder notificationBuilder) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            notificationBuilder.setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
//            return R.drawable.ic_push;
//        } else {
//            return R.mipmap.ic_launcher;
//        }
        return R.drawable.noti_icon_new;
    }
}
