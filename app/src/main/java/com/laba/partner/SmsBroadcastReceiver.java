package com.laba.partner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.common.api.*;

public class SmsBroadcastReceiver extends BroadcastReceiver {

    public SmsBroadcastCallbacks callbacks;

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction() == SmsRetriever.SMS_RETRIEVED_ACTION) {
            Bundle extras = intent.getExtras();
            Status smsReceiverStatus = (Status) extras.get(SmsRetriever.EXTRA_STATUS);

            switch (smsReceiverStatus.getStatusCode()) {

                case CommonStatusCodes.SUCCESS:
                    Intent messageIntent = extras.getParcelable(SmsRetriever.EXTRA_CONSENT_INTENT);
                    callbacks.success(messageIntent);
                    break;
                case CommonStatusCodes.TIMEOUT:
                    callbacks.failed();
                    break;
            }
        }
    }

    public interface SmsBroadcastCallbacks {
        void success(Intent intent);

        void failed();
    }

}
