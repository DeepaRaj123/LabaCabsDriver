package com.laba.partner.common;

import android.Manifest;

public class Constants {

    //Camera request test
    public static final String[] MULTIPLE_PERMISSION = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    public static final int RC_MULTIPLE_PERMISSION_CODE = 12224;
    public static final String LANGUAGE_ENGLISH = "en";
    public static final String LANGUAGE_FINNISH = "fi";
    public static final String LANGUAGE_ARABIC = "ar";
    public static final String LANGUAGE_FRENCH = "fr";
    public static final int CHECK_SCHEDULE_JOB_ID = 12;
    public static final int RC_CALL_PHONE = 123;
    public static final String INTENT_BROADCAST = "laba_local_broadcast";
    public static int APP_REQUEST_CODE = 99;
    public static String Currency = "";

    public static final int BC_START_LOCATION_TRACK = 1;
    public static final int BC_STOP_LOCATION_TRACK = 2;
    public static final int BC_OFFLINE = 3;

    public static final String FIREBASE_LOCATION_DB ="DriverLocation";

    public interface SharedPref {

        String logged_in = "logged_in";
        String email = "email";
        String txt_email = "txtEmail";
        String otp = "otp";
        String id = "id_";
        String user_id = "user_id";
        String user_name = "user_name";
        String user_avatar = "user_avatar";
        String device_token = "device_token";
        String device_id = "device_id";
        String access_token = "access_token";
        String dial_code = "dial_code";
        String mobile = "mobile";
        String cancel_id = "cancel_id";
        String request_id = "request_id";
        String currency = "currency";
    }

    public interface checkStatus {

        String EMPTY = "EMPTY";
        String SEARCHING = "SEARCHING";
        String ACCEPTED = "ACCEPTED";
        String STARTED = "STARTED";
        String ARRIVED = "ARRIVED";
        String PICKEDUP = "PICKEDUP";
        String DROPPED = "DROPPED";
        String COMPLETED = "COMPLETED";
        String INVOICE = "INVOICE";
        String RATING = "RATING";
        String PATCH = "PATCH";
    }

    public interface User {
        interface Account {
            String pendingDocument = "document";
            String pendingCard = "card";
            String onBoarding = "onboarding";
            String approved = "approved";
            String banned = "banned";
            String wallet = "wallet";//when wallet limit is low
        }

        interface Service {
            String offLine = "offline";
            String active = "active";
        }
    }

    public interface PaymentMode {

        String CASH = "CASH";
        String CARD = "CARD";
        String PAYPAL = "PAYPAL";
        String WALLET = "WALLET";
        String razorpay = "razorpay";
    }

    public interface Login {

        String facebook = "facebook";
        String google = "google";
    }

    public interface InvoiceFare {

        String min = "MIN";
        String hour = "HOUR";
        String distance = "DISTANCE";
        String distanceMin = "DISTANCEMIN";
        String distanceHour = "DISTANCEHOUR";
    }

    public interface Document {
        String ASSESSING = "ASSESSING";
        String ACTIVE = "ACTIVE";
    }

}
