package com.laba.partner.ui.activity.splash;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Handler;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.laba.partner.R;
import com.laba.partner.base.BaseActivity;
import com.laba.partner.common.Constants;
import com.laba.partner.common.SharedHelper;
import com.laba.partner.common.Utilities;
import com.laba.partner.ui.activity.main.MainActivity;
import com.laba.partner.ui.activity.welcome.WelcomeActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SplashActivity extends BaseActivity implements SplashIView {

    private static final String TAG = "SplashActivity";
    SplashPresenter presenter;

    @Override
    public int getLayoutId() {

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        return R.layout.activity_splash;
    }

    @Override
    public void initView() {
        presenter = new SplashPresenter();
        presenter.attachView(this);
        presenter.handlerCall();

        printHashKey();
        Utilities.printV("FCM TOKEN===>", SharedHelper.getKeyFCM(this, "device_token"));
        Utilities.printV("FCM TOKEN ID===>", SharedHelper.getKeyFCM(this, "device_id"));
    }

    @Override
    public void redirectHome() {
        if (SharedHelper.getKey(this, Constants.SharedPref.logged_in).equalsIgnoreCase("true"))
            startActivity(new Intent(activity(), MainActivity.class));
        else
            startActivity(new Intent(activity(), WelcomeActivity.class));
    }

    @Override
    public void onSuccess(Object user) {
        Toast.makeText(activity(), getString(R.string.api_success), Toast.LENGTH_SHORT).show();
        Utilities.printV("jsonObj===>", user.toString());
        String jsonInString = new Gson().toJson(user);
        try {
            JSONObject jsonObj = new JSONObject(jsonInString);
            Utilities.printV("jsonObj===>", jsonObj.toString());
            JSONArray jsonArray = jsonObj.optJSONArray("results");
            if (jsonArray.length() > 0) {
                String formatted = jsonArray.optJSONObject(0).optString("formatted_address");
                Log.v("Formatted Address", "" + formatted);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onError(Throwable e) {
        hideLoading();
        if (e != null)
            onErrorBase(e);
    }

    public void printHashKey() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String hashKey = new String(Base64.encode(md.digest(), 0));
                Log.i(TAG, "printHashKey() Hash Key: " + hashKey);
            }
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "printHashKey()", e);
        } catch (Exception e) {
            Log.e(TAG, "printHashKey()", e);
        }
    }


}
