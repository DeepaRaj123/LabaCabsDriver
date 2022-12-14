package com.laba.partner.base;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.facebook.accountkit.ui.AccountKitActivity;
import com.facebook.accountkit.ui.AccountKitConfiguration;
import com.facebook.accountkit.ui.LoginType;
import com.google.gson.Gson;
import com.laba.partner.R;
import com.laba.partner.common.Constants;
import com.laba.partner.common.LocaleHelper;
import com.laba.partner.common.SharedHelper;
import com.laba.partner.common.Utilities;
import com.laba.partner.data.network.model.HistoryDetail;
import com.laba.partner.data.network.model.HistoryList;
import com.laba.partner.data.network.model.Request_;
import com.laba.partner.data.network.model.TripResponse;
import com.laba.partner.data.network.model.User;
import com.laba.partner.ui.activity.password.PasswordActivity;
import com.laba.partner.ui.activity.welcome.WelcomeActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import pl.aprilapps.easyphotopicker.EasyImage;
import retrofit2.HttpException;

import static com.laba.partner.common.Constants.APP_REQUEST_CODE;
import static com.laba.partner.common.fcm.MyFireBaseMessagingService.INTENT_FILTER;

public abstract class BaseActivity extends AppCompatActivity implements MvpView {

    public static Request_ DATUM = null;
    public static TripResponse tripResponse = null;
    public static Integer time_to_left = 60;
    public static HistoryList DATUM_history = null;
    public static HistoryDetail DATUM_history_detail = null;
    ProgressDialog progressDialog;
    Toast mToast;
    private BasePresenter<BaseActivity> presenter = new BasePresenter<BaseActivity>();
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int status = intent.getIntExtra(Constants.INTENT_BROADCAST, -1);
            onLocalBroadcastReceiver(status);
        }
    };

    // This method  converts String to RequestBody
    public static RequestBody toRequestBody(String value) {
        return RequestBody.create(MediaType.parse("text/plain"), value);
    }

    public static String getDisplayableTime(long value) {

        long difference;
        Long mDate = java.lang.System.currentTimeMillis();

        if (mDate > value) {
            difference = mDate - value;
            final long seconds = difference / 1000;
            final long minutes = seconds / 60;
            final long hours = minutes / 60;
            final long days = hours / 24;
            final long months = days / 31;
            final long years = days / 365;

            //return "not yet";
            if (seconds < 86400)
                return new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date(value));
            else if (seconds < 172800) // 48 * 60 * 60
                return "yesterday";
            else if (seconds < 2592000) // 30 * 24 * 60 * 60
                return days + " days ago";
            else if (seconds < 31104000) // 12 * 30 * 24 * 60 * 60
                return months <= 1 ? "one month ago" : days + " months ago";
            else return years <= 1 ? "one year ago" : years + " years ago";
        }
        return null;
    }

    public static Map<String, Object> jsonToMap(JSONObject json) throws JSONException {
        Map<String, Object> retMap = new HashMap<String, Object>();
        if (json != JSONObject.NULL) retMap = toMap(json);
        return retMap;
    }

    public static Map<String, Object> toMap(JSONObject object) throws JSONException {
        Map<String, Object> map = new HashMap<String, Object>();
        Iterator<String> keysItr = object.keys();
        while (keysItr.hasNext()) {
            String key = keysItr.next();
            Object value = object.get(key);
            if (value instanceof JSONArray) value = toList((JSONArray) value);
            else if (value instanceof JSONObject) value = toMap((JSONObject) value);
            map.put(key, value);
        }
        return map;
    }

    public static List<Object> toList(JSONArray array) throws JSONException {
        List<Object> list = new ArrayList<Object>();
        for (int i = 0; i < array.length(); i++) {
            Object value = array.get(i);
            if (value instanceof JSONArray) value = toList((JSONArray) value);
            else if (value instanceof JSONObject) value = toMap((JSONObject) value);
            list.add(value);
        }
        return list;
    }

    @Override
    public Activity activity() {
        return this;
    }

    public abstract int getLayoutId();

    public abstract void initView();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());

        DATUM = null;

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.setCancelable(false);

        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver,
                new IntentFilter(Intent.ACTION_MAIN)
        );

        initView();
    }

    public void onLocalBroadcastReceiver(int status) {

    }

    @TargetApi(Build.VERSION_CODES.M)
    public void requestPermissionsSafely(String[] permissions, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            requestPermissions(permissions, requestCode);
    }

    @TargetApi(Build.VERSION_CODES.M)
    public boolean hasPermission(String permission) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

    public void pickImage() {
        EasyImage.openChooserWithGallery(this, "", 0);
    }

    public void showLoading() {
        if (progressDialog != null && !progressDialog.isShowing()) progressDialog.show();
    }

    public void hideLoading() {
        if (progressDialog.isShowing())
            progressDialog.dismiss();
    }

    public void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void showAToast(String message) {
        if (mToast != null) mToast.cancel();
        mToast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        mToast.show();
    }

    public void fbOtpVerify() {
        final Intent intent = new Intent(this, AccountKitActivity.class);
        AccountKitConfiguration.AccountKitConfigurationBuilder configurationBuilder =
                new AccountKitConfiguration.AccountKitConfigurationBuilder(
                        LoginType.PHONE,
                        AccountKitActivity.ResponseType.TOKEN);
        configurationBuilder.setReadPhoneStateEnabled(true);
        configurationBuilder.setReceiveSMS(true);
    /*    if (BuildConfig.DEBUG) {
            PhoneNumber phoneNumber = new PhoneNumber("IN", "9003440134", "+91");
            configurationBuilder.setInitialPhoneNumber(phoneNumber);
        }*/
        intent.putExtra(
                AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION,
                configurationBuilder.build());
        startActivityForResult(intent, APP_REQUEST_CODE);
    }

    public void onErrorBase(Throwable e) {

        try {
            if (e instanceof ConnectException || e instanceof UnknownHostException ||
                    e instanceof SocketTimeoutException || e instanceof NoRouteToHostException) {
//                Toasty.error(this, getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
            } else if (e instanceof HttpException) {
                ResponseBody responseBody = ((HttpException) e).response().errorBody();
                int responseCode = ((HttpException) e).response().code();
                try {
                    JSONObject jsonObject = new JSONObject(responseBody.string());
                    if (responseCode == 400 || responseCode == 405 || responseCode == 500) {
                        Toasty.error(this, getErrorMessage(jsonObject), Toast.LENGTH_SHORT).show();
                    } else if (responseCode == 404) {
                        if (PasswordActivity.TAG.equals("PasswordActivity")) {
                            Collection<Object> values = jsonToMap(jsonObject).values();
                            printIfContainsValue(jsonToMap(jsonObject), values.toString()
                                    .replaceAll("[\\[\\],]", ""));
                        } else {
                            Toasty.error(this, getErrorMessage(jsonObject), Toast.LENGTH_SHORT).show();
                        }
                    } else if (responseCode == 401) {

                        Toast.makeText(this, getString(R.string.expireSession), Toast.LENGTH_SHORT).show();

                        SharedHelper.clearSharedPreferences(activity());
                        NotificationManager notificationManager = (NotificationManager) activity().getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.cancelAll();
                        activity().finishAffinity();
                        Intent goToLogin = new Intent(activity(), WelcomeActivity.class);
                        goToLogin.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        activity().startActivity(goToLogin);
                        activity().finish();

                       /* if (!SharedHelper.getKey(this, "refresh_token").equalsIgnoreCase(""))
                            refreshToken();
                        else {
                            String message = "";
                            if (jsonObject.has("message"))
                                message = jsonObject.getString("message");
                            else if (jsonObject.has("error"))
                                message = jsonObject.getString("error");
                            else message = "Unauthenticated. Please try with correct credentials";
                            Toasty.error(this, message, Toast.LENGTH_SHORT).show();
                        }*/


                    } else if (responseCode == 422) {
                        Collection<Object> values = jsonToMap(jsonObject).values();
                        printIfContainsValue(jsonToMap(jsonObject), values.toString()
                                .replaceAll("[\\[\\],]", ""));
                    } else if (responseCode == 520) {
                        Collection<Object> values = jsonToMap(jsonObject).values();
                        printIfContainsValue(jsonToMap(jsonObject), values.toString()
                                .replaceAll("[\\[\\],]", ""));
                    } else if (responseCode == 503) {
                        Toasty.error(this, getString(R.string.server_down), Toast.LENGTH_SHORT).show();
                    } else {
                        Toasty.error(this, getErrorMessage(jsonObject), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                    //Toast.makeText(getApplicationContext(), getString(R.string.some_thing_wrong), Toast.LENGTH_LONG).show();
                }
            } else {
                //Toast.makeText(this, getString(R.string.some_thing_wrong), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e1) {
            e1.printStackTrace();
            // Toast.makeText(this, getString(R.string.some_thing_wrong), Toast.LENGTH_SHORT).show();
        }
    }

    public void printIfContainsValue(Map mp, String value) {
        Toasty.error(this, value, Toast.LENGTH_SHORT).show();
    }

    public String getErrorMessage(JSONObject jsonObject) {
        try {
            String error;
            if (jsonObject.has("message")) error = jsonObject.getString("message");
            else if (jsonObject.has("error")) error = jsonObject.getString("error");
            else if (jsonObject.has("mobile")) error = jsonObject.optString("mobile");
            else {
                error = getString(R.string.some_thing_wrong);
            }
            return error;
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    public String printJSON(Object o) {
        return new Gson().toJson(o);
    }

    public void refreshToken() {
        showLoading();
        presenter.refreshToken();
    }

    @Override
    public void onSuccessRefreshToken(User user) {
        hideLoading();
        SharedHelper.putKey(this, user.getAccessToken(), Constants.SharedPref.access_token);
        Toasty.error(this, getString(R.string.please_try_again), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onErrorRefreshToken(Throwable throwable) {
        hideLoading();
        if (throwable != null) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("id", SharedHelper.getKey(activity(),
                    Constants.SharedPref.user_id) + "");
            showLoading();
            presenter.logout(map);
        }
    }

    @Override
    public void onSuccessLogout(Object object) {
        hideLoading();
        Utilities.LogoutApp(activity(), "");
    }

    @Override
    public void onError(Throwable throwable) {
        hideLoading();
        // throwable.printStackTrace();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.detachView();
    }
}
