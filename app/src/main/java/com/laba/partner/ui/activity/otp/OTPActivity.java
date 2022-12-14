package com.laba.partner.ui.activity.otp;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.auth.api.phone.SmsRetrieverClient;
import com.laba.partner.R;
import com.laba.partner.SmsBroadcastReceiver;
import com.laba.partner.base.BaseActivity;
import com.laba.partner.common.countrypicker.Country;
import com.laba.partner.common.countrypicker.CountryPicker;
import com.laba.partner.data.network.model.MyOTP;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.dmoral.toasty.Toasty;
import me.philio.pinentry.PinEntryView;
import okhttp3.ResponseBody;
import retrofit2.HttpException;

public class OTPActivity extends BaseActivity implements OTPIView {

    @BindView(R.id.pin_entry)
    PinEntryView pinEntry;
    @BindView(R.id.otp_description)
    TextView otpDescription;
    @BindView(R.id.layoutMobile)
    LinearLayout layoutMobile;
    @BindView(R.id.layoutOtp)
    LinearLayout layoutOtp;
    @BindView(R.id.countryNumber)
    TextView countryNumber;
    @BindView(R.id.countryImage)
    ImageView countryImage;
    @BindView(R.id.txtPhoneNumber)
    EditText mobileNumber;
    String mobile, OTP, countryCode = "+91";
    boolean mIsFromRegister = false;
    String description = "";
    HashMap<String, Object> map = new HashMap<>();
    SmsBroadcastReceiver smsBroadcastReceiver;
    private String mCountryCode = "IN";
    private CountryPicker mCountryPicker;
    private OTPPresenter<OTPActivity> presenter = new OTPPresenter<>();
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equalsIgnoreCase("otp")) {
                final String message = intent.getStringExtra("message");
                System.out.println("BroadcastReceiver" + message);
                pinEntry.setText(message);
            }
        }
    };

    @Override
    public int getLayoutId() {
        return R.layout.activity_otp;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        presenter.attachView(this);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mobile = extras.getString("mobile", "");
            countryCode = extras.getString("country_code", "");
            OTP = extras.getString("otp", "");
            mIsFromRegister = extras.getBoolean("isFromRegister");
        }

        if (mIsFromRegister) {
            layoutMobile.setVisibility(View.GONE);
            layoutOtp.setVisibility(View.VISIBLE);
            map = new HashMap<>();
            map.put("mobile", countryCode + mobile);
            map.put("phoneonly", mobile);
            otpDescription.setText("OTP sent to your mobile number " + countryCode + mobile);
        } else {
            layoutMobile.setVisibility(View.VISIBLE);
            layoutOtp.setVisibility(View.GONE);
        }
        setCountryList();

        startSmartUserConsent();
    }

    private void startSmartUserConsent() {
        SmsRetrieverClient client = SmsRetriever.getClient(this);
        client.startSmsUserConsent(null);

    }

    private void registerBroadcaseReceiver() {
        smsBroadcastReceiver = new SmsBroadcastReceiver();

        smsBroadcastReceiver.callbacks = new SmsBroadcastReceiver.SmsBroadcastCallbacks() {
            @Override
            public void success(Intent intent) {
                startActivityForResult(intent, 123);
            }

            @Override
            public void failed() {

            }
        };

        IntentFilter filter = new IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION);
        registerReceiver(smsBroadcastReceiver, filter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerBroadcaseReceiver();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(smsBroadcastReceiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ((requestCode == 123) && (data != null)) {

            String message = data.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE);
            getOtpFromMessage(message);
        }
    }

    private void getOtpFromMessage(String message) {
        Pattern pattern = Pattern.compile("(|^)\\d{4}");
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            pinEntry.setText(matcher.group(0));
            submitButtonClick();
        }
    }

    private void setCountryList() {
        mCountryPicker = CountryPicker.newInstance("Select Country");
        List<Country> countryList = Country.getAllCountries();
        Collections.sort(countryList, (s1, s2) -> s1.getName().compareToIgnoreCase(s2.getName()));
        mCountryPicker.setCountriesList(countryList);

        setListener();
    }

    private void setListener() {
        mCountryPicker.setListener((name, code, dialCode, flagDrawableResID) -> {
            countryNumber.setText(dialCode);
            countryCode = dialCode;
            countryImage.setImageResource(flagDrawableResID);
            mCountryPicker.dismiss();
        });

        countryImage.setOnClickListener(v -> mCountryPicker.show(getSupportFragmentManager(),
                "COUNTRY_PICKER"));

        countryNumber.setOnClickListener(v -> mCountryPicker.show(getSupportFragmentManager(),
                "COUNTRY_PICKER"));

        getUserCountryInfo();
    }

    private void getUserCountryInfo() {
        Country country = getDeviceCountry(OTPActivity.this);
        countryImage.setImageResource(country.getFlag());
        countryNumber.setText(country.getDialCode());
        countryCode = country.getDialCode();
        mCountryCode = country.getCode();
    }

    public Country getDeviceCountry(Context context) {
        return Country.getCountryFromSIM(context) != null
                ? Country.getCountryFromSIM(context)
                : new Country("IN", "India", "+91", R.drawable.flag_in);
    }

    @OnClick({R.id.submit, R.id.resend_otp, R.id.voice_call_otp})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.submit:
                submitButtonClick();
                break;
            case R.id.resend_otp:
                showLoading();
                description = "OTP resent to your mobile number " + countryCode + mobile;
                presenter.sendOTP(map);
                break;
            case R.id.voice_call_otp:
                showLoading();
                description = "You will receive voice call to your mobile number " + countryCode + mobile;
                break;
        }
    }

    private void submitButtonClick() {
        if (layoutMobile.getVisibility() == View.VISIBLE) {

            if (mobileNumber.getText().toString().isEmpty()) {
                Toast.makeText(this, getString(R.string.invalid_mobile), Toast.LENGTH_SHORT).show();
            } else if (mobileNumber.getText().toString().length() != 10) {
                Toast.makeText(this, "Please provide valid 10 digit mobile number", Toast.LENGTH_LONG).show();
            } else {
                hideKeyboard();
                showLoading();
                mobile = mobileNumber.getText().toString();
                description = "OTP sent to your mobile number " + countryCode + mobile;
                map = new HashMap<>();
//                        map.put("mobile", countryCode + mobile);
                map.put("mobile", mobile);
                map.put("phoneonly", mobile);
                presenter.sendOTP(map);

            }
        } else {
            if (pinEntry.getText().toString().isEmpty()) {
                Toast.makeText(this, getString(R.string.invalid_otp), Toast.LENGTH_SHORT).show();
                return;
            }
            if (pinEntry.getText().toString().equalsIgnoreCase(OTP)) {
                Intent intent = new Intent();
                intent.putExtra("mobile", mobile);
                intent.putExtra("country_code", countryCode);
                intent.putExtra("otp", pinEntry.getText().toString());
                setResult(Activity.RESULT_OK, intent);
                finish();
            } else {
                Toast.makeText(this, R.string.wrong_otp, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onResume() {
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter("otp"));
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    @Override
    public void onSuccess(MyOTP otp) {
        try {
            hideLoading();
        } catch (Exception e) {
            e.printStackTrace();
        }
        pinEntry.setText("");
        OTP = otp.getOtp();
        otpDescription.setText(description);
        layoutMobile.setVisibility(View.GONE);
        layoutOtp.setVisibility(View.VISIBLE);
    }

    @Override
    public void onError(Throwable e) {
        try {
            hideLoading();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (e instanceof HttpException) {
            try {
                ResponseBody responseBody = ((HttpException) e).response().errorBody();
                int responseCode = ((HttpException) e).response().code();
                JSONObject jsonObject = new JSONObject(responseBody.string());
                if (responseCode == 422) {
                    if (jsonObject.has("mobile")) {
                        if (jsonObject.getString("mobile").contains("You are already Registered")) {
                            layoutMobile.setVisibility(View.VISIBLE);
                            layoutOtp.setVisibility(View.GONE);
                            if (!mIsFromRegister) {
                                Intent intent = new Intent();
                                intent.putExtra("mobile", mobile);
                                intent.putExtra("country_code", countryCode);
                                setResult(Activity.RESULT_OK, intent);
                                finish();
                            } else {
                                Toasty.error(this, getErrorMessage(jsonObject), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                } else {
                    onErrorBase(e);
                }
            } catch (JSONException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else {
            onErrorBase(e);
        }

    }
}
