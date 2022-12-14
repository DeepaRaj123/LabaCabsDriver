package com.laba.partner.service;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.laba.partner.common.Constants;
import com.laba.partner.common.SharedHelper;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class CheckScheduleService extends JobService {
    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        String accountStatus = SharedHelper.getKey(getApplicationContext(), "accountStatus");
        String serviceStatus = SharedHelper.getKey(getApplicationContext(), "serviceStatus");


        if (Constants.User.Account.approved.equalsIgnoreCase(accountStatus)
                && Constants.User.Service.active.equalsIgnoreCase(serviceStatus)) {
            /*if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                getApplicationContext().startService(new Intent(getApplicationContext(), LocationShareService.class));
            } else {
                Intent serviceIntent = new Intent(getApplicationContext(), LocationShareService.class);
                ContextCompat.startForegroundService(getApplicationContext(), serviceIntent);
            }*/

            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.putExtra(Constants.INTENT_BROADCAST, Constants.BC_START_LOCATION_TRACK);
            //LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        } else {
            //stopLocationService();
        }
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return true;
    }

  /*  private boolean isServiceRunning() {
        ActivityManager manager =
                (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service :
                manager.getRunningServices(Integer.MAX_VALUE)) {
            if (LocationShareService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }*/

    private void stopLocationService() {
        /*if (isServiceRunning())
            stopService(new Intent(getApplicationContext(), LocationShareService.class));*/

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.putExtra(Constants.INTENT_BROADCAST, Constants.BC_STOP_LOCATION_TRACK);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
