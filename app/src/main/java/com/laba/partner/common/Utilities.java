package com.laba.partner.common;

import static com.laba.partner.common.fcm.MyFireBaseMessagingService.INTENT_FILTER;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import com.laba.partner.MvpApplication;
import com.laba.partner.R;
import com.laba.partner.ui.activity.welcome.WelcomeActivity;

import org.apache.commons.lang3.StringEscapeUtils;
import org.imperiumlabs.geofirestore.GeoFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import es.dmoral.toasty.Toasty;

public class Utilities {

    public static boolean isEmailValid(String email) {
        Pattern pattern;
        Matcher matcher;
        final String EMAIL_PATTERN =
                "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                        + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        pattern = Pattern.compile(EMAIL_PATTERN);
        matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public static void printV(String TAG, String message) {
        System.out.println(TAG + "==>" + message);
    }

    public static void showAlert(final Context context, String message) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            builder.setMessage(message)
                    .setTitle(context.getString(R.string.app_name))
                    .setCancelable(true)
                    //.setIcon(R.mipmap.ic_launcher)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
            final AlertDialog alert = builder.create();
            alert.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface arg) {
                    alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
                    alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
                }
            });
            alert.show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void showAlertForDeniedPermission(final Activity activity, String message) {

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(activity.getString(R.string.app_name));
        builder.setIcon(activity.getResources().getDrawable(R.mipmap.ic_launcher));
        builder.setMessage(message);
        builder.setCancelable(false);
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.setPositiveButton("GO" + " TO " + "SETTINGS", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                activity.startActivity(new Intent(Settings
                        .ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse
                        ("package:" + activity.getPackageName())));

            }
        });

        AlertDialog alert = builder.create();
        alert.show();
        Button nbutton = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
        nbutton.setTextColor(Color.BLACK);
        Button pbutton = alert.getButton(DialogInterface.BUTTON_POSITIVE);
        pbutton.setTextColor(Color.BLACK);


    }

    public static boolean isConnected() {
        ConnectivityManager
                cm = (ConnectivityManager) MvpApplication.getInstance().getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null
                && activeNetwork.isConnectedOrConnecting();
    }

    public static void LogoutApp(Activity thisActivity, String logout_text) {

        Intent intent = new Intent(INTENT_FILTER);
        intent.putExtra(Constants.INTENT_BROADCAST, Constants.BC_OFFLINE);
        thisActivity.sendBroadcast(intent);

        logout_text = thisActivity.getString(R.string.logout_successfully);
        //GeoFirestore
        GeoFirestore geoFire= new GeoFirestore(FirebaseFirestore.getInstance().collection(Constants.FIREBASE_LOCATION_DB));
        geoFire.removeLocation(SharedHelper.getKey(thisActivity, Constants.SharedPref.user_id));
        // Toast.makeText(thisActivity,thisActivity.getString(R.string.session_timeout),Toast.LENGTH_SHORT).show();
        Toasty.success(thisActivity, logout_text, Toast.LENGTH_SHORT).show();
        SharedHelper.clearSharedPreferences(thisActivity);
        NotificationManager notificationManager = (NotificationManager) thisActivity.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        thisActivity.finishAffinity();
        Intent goToLogin = new Intent(thisActivity, WelcomeActivity.class);
        goToLogin.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        thisActivity.startActivity(goToLogin);
        thisActivity.finish();

    }

    public static void OAuth(Activity thisActivity) {

        String logout_text = "Token Expired!";

        // Toast.makeText(thisActivity,thisActivity.getString(R.string.session_timeout),Toast.LENGTH_SHORT).show();
        Toasty.success(thisActivity, logout_text, Toast.LENGTH_LONG).show();
        SharedHelper.clearSharedPreferences(thisActivity);

        Intent goToLogin = new Intent(thisActivity, WelcomeActivity.class);
        goToLogin.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        thisActivity.startActivity(goToLogin);
        thisActivity.finish();
    }

    public static void LogoutApp(Activity thisActivity) {
        Toasty.success(thisActivity, thisActivity.getString(R.string.logout_successfully), Toast.LENGTH_SHORT).show();
        SharedHelper.clearSharedPreferences(thisActivity);
        NotificationManager notificationManager = (NotificationManager) thisActivity.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        thisActivity.startActivity(new Intent(thisActivity, WelcomeActivity.class));
        thisActivity.finishAffinity();
    }

    public static String getTime(String date) throws ParseException {
        Date d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).parse(date);
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        @SuppressLint("SimpleDateFormat")
        String timeName = new SimpleDateFormat("hh:mm").format(cal.getTime());
        return timeName;
    }

    public static String convertDate(String receiveDate) throws ParseException {
        SimpleDateFormat in = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = in.parse(receiveDate);
        SimpleDateFormat out = new SimpleDateFormat("dd MMM");
        String newdate = out.format(date);
        return newdate;
    }

    public static void animateTextView(int initialValue, int finalValue, final int target, final TextView textview) {
        DecelerateInterpolator decelerateInterpolator = new DecelerateInterpolator(0.8f);
        int start = Math.min(initialValue, finalValue);
        int end = Math.max(initialValue, finalValue);
        int difference = Math.abs(finalValue - initialValue);
        Handler handler = new Handler();
        for (int count = start; count <= end; count++) {
            int time = Math.round(decelerateInterpolator.getInterpolation((((float) count) / difference)) * 100) * count;
            final int finalCount = ((initialValue > finalValue) ? initialValue - count : count);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    textview.setText(finalCount + "/" + target);
                }
            }, time);
        }
    }

    public static String getEncodeMessage(String message) {
        return StringEscapeUtils.escapeJava(message);
    }

    public static String getDecodeMessage(String message) {
        return StringEscapeUtils.unescapeJava(message);
    }

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

    public void hideKeypad(Context context, View view) {
        // Check if no view has focus:
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
