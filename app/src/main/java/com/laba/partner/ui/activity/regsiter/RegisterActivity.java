package com.laba.partner.ui.activity.regsiter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.facebook.accountkit.AccountKitLoginResult;
import com.facebook.accountkit.PhoneNumber;
import com.facebook.accountkit.ui.AccountKitActivity;
import com.facebook.accountkit.ui.AccountKitConfiguration;
import com.facebook.accountkit.ui.LoginType;
import com.laba.partner.BuildConfig;
import com.laba.partner.R;
import com.laba.partner.base.BaseActivity;
import com.laba.partner.common.CommonValidation;
import com.laba.partner.common.Constants;
import com.laba.partner.common.SharedHelper;
import com.laba.partner.common.Utilities;
import com.laba.partner.common.countrypicker.Country;
import com.laba.partner.common.countrypicker.CountryPicker;
import com.laba.partner.data.network.model.MyOTP;
import com.laba.partner.data.network.model.User;
import com.laba.partner.ui.activity.main.MainActivity;
import com.laba.partner.ui.activity.otp.OTPActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.dmoral.toasty.Toasty;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.HttpException;

import static com.laba.partner.common.Constants.APP_REQUEST_CODE;

public class RegisterActivity extends BaseActivity implements RegisterIView {

    private static final int PICK_OTP_VERIFY = 222;

    @BindView(R.id.back)
    ImageView back;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.txtEmail)
    EditText txtEmail;
    @BindView(R.id.txtFirstName)
    EditText txtFirstName;
    @BindView(R.id.txtLastName)
    EditText txtLastName;
    @BindView(R.id.txtPassword)
    EditText txtPassword;
    @BindView(R.id.txtConfirmPassword)
    EditText txtConfirmPassword;
    @BindView(R.id.chkTerms)
    CheckBox chkTerms;
    @BindView(R.id.countryNumber)
    TextView countryNumber;
    @BindView(R.id.countryImage)
    ImageView countryImage;
    @BindView(R.id.txtPhoneNumber)
    EditText mobile;

    private String countryDialCode = "+91";
    private String countryCode = "IN";
    private CountryPicker mCountryPicker;

    private RegisterPresenter presenter;

    @Override
    public int getLayoutId() {
        return R.layout.activity_register;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        presenter = new RegisterPresenter();
        presenter.attachView(this);
        setCountryList();
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
            countryDialCode = dialCode;
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
        Country country = getDeviceCountry(RegisterActivity.this);
        countryImage.setImageResource(country.getFlag());
        countryNumber.setText(country.getDialCode());
        countryDialCode = country.getDialCode();
        countryCode = country.getCode();
    }

    public Country getDeviceCountry(Context context) {
        return Country.getCountryFromSIM(context) != null
                ? Country.getCountryFromSIM(context)
                : new Country("IN", "India", "+91", R.drawable.flag_in);
    }

    private boolean validation() {
        if (txtEmail.getText().toString().isEmpty()) {
            Toasty.error(this, getString(R.string.invalid_email), Toast.LENGTH_SHORT, true).show();
            return false;
        }

        String email = txtEmail.getText().toString();
        String regex = "^(.+)@(.+)$";
        //Compile regular expression to get the pattern
        Pattern pattern = Pattern.compile(regex);

        Matcher matcher = pattern.matcher(email);
        if (!matcher.matches()) {
            Toasty.error(this, getString(R.string.invalid_email), Toast.LENGTH_SHORT, true).show();
            return false;
        } else if (mobile.getText().toString().isEmpty()) {
            Toasty.error(this, getString(R.string.invalid_mobile), Toast.LENGTH_SHORT, true).show();
            return false;
        } else if (mobile.getText().toString().length() != 10) {
            Toasty.error(this, getString(R.string.mobile_no_lengh_error), Toast.LENGTH_SHORT, true).show();
            return false;
        } else if (CommonValidation.Validation(txtFirstName.getText().toString().trim())) {
            Toasty.error(this, getString(R.string.invalid_first_name), Toast.LENGTH_SHORT, true).show();
            return false;
        } else if (CommonValidation.Validation(txtLastName.getText().toString().trim())) {
            Toasty.error(this, getString(R.string.invalid_last_name), Toast.LENGTH_SHORT, true).show();
            return false;
        } else if (txtPassword.getText().toString().length() < 6) {
            Toasty.error(this, getString(R.string.invalid_password_length), Toast.LENGTH_SHORT, true).show();
            return false;
        } else if (txtConfirmPassword.getText().toString().isEmpty()) {
            Toasty.error(this, getString(R.string.invalid_confirm_password), Toast.LENGTH_SHORT, true).show();
            return false;
        } else if (!txtPassword.getText().toString().equals(txtConfirmPassword.getText().toString())) {
            Toasty.error(this, getString(R.string.password_should_be_same), Toast.LENGTH_SHORT, true).show();
            return false;
        } else if (!chkTerms.isChecked()) {
            Toasty.error(this, getString(R.string.please_accept_terms_conditions), Toast.LENGTH_SHORT, true).show();
            return false;
        } else return true;


    }

    private void register(String countryCode, String phoneNumber) {

        //All the String parameters, you have to put like
        Map<String, RequestBody> map = new HashMap<>();
        map.put("first_name", toRequestBody(txtFirstName.getText().toString()));
        map.put("last_name", toRequestBody(txtLastName.getText().toString()));
        map.put("email", toRequestBody(txtEmail.getText().toString()));
        map.put("mobile", toRequestBody(phoneNumber));
        map.put("country_code", toRequestBody(countryCode));
        map.put("password", toRequestBody(txtPassword.getText().toString()));
        map.put("password_confirmation", toRequestBody(txtConfirmPassword.getText().toString()));
        map.put("device_token", toRequestBody(SharedHelper.getKeyFCM(this, Constants.SharedPref.device_token)));
        map.put("device_id", toRequestBody(SharedHelper.getKeyFCM(this, Constants.SharedPref.device_id)));
        map.put("device_type", toRequestBody(BuildConfig.DEVICE_TYPE));

        List<MultipartBody.Part> parts = new ArrayList<>();
        showLoading();
        presenter.register(map, parts);
    }

    @OnClick({R.id.next, R.id.back, R.id.lblTerms})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.next:
                if (validation()) if (Utilities.isConnected()) {
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("email", txtEmail.getText().toString());
                    showLoading();
                    presenter.verifyEmail(map);
                } else showAToast(getString(R.string.no_internet_connection));
                break;
            case R.id.lblTerms:
                showTermsConditionsDialog();
                break;
            case R.id.back:
                onBackPressed();
                break;
        }
    }

    private void showTermsConditionsDialog() {
        showLoading();
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(getText(R.string.terms_and_conditions));

        WebView wv = new WebView(this);
        wv.loadUrl(BuildConfig.TERMS_CONDITIONS);
        wv.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                hideLoading();
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                try {
                    hideLoading();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                alert.show();
            }
        });

        alert.setView(wv);
        alert.setNegativeButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                hideLoading();
                dialog.dismiss();
            }
        });
    }

    @Override
    public void onSuccess(User user) {
        hideLoading();
        SharedHelper.putKey(this, Constants.SharedPref.user_id, String.valueOf(user.getId()));
        SharedHelper.putKey(this, Constants.SharedPref.access_token, user.getAccessToken());
        SharedHelper.putKey(this, Constants.SharedPref.logged_in, "true");
        Toasty.success(this, getString(R.string.register_success), Toast.LENGTH_SHORT, true).show();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    public void onSuccess(Object verifyEmail) {
        hideLoading();
        showLoading();
        //register(countryDialCode, mobile.getText().toString());
        HashMap<String, Object> params = new HashMap<>();
        params.put("mobile", mobile.getText().toString());
        params.put("phoneonly", mobile.getText().toString());
        presenter.sendOTP(params);
    }

    @Override
    public void onSuccess(MyOTP otp) {
        try {
            hideLoading();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Intent intent = new Intent(activity(), OTPActivity.class);
        intent.putExtra("isFromRegister", true);
        intent.putExtra("mobile", mobile.getText().toString());
        intent.putExtra("country_code", countryDialCode);
        intent.putExtra("otp", otp.getOtp());
        startActivityForResult(intent, PICK_OTP_VERIFY);
    }


    @Override
    public void onError(Throwable e) {
        hideLoading();
        if (e instanceof HttpException) {
            try {
                ResponseBody responseBody = ((HttpException) e).response().errorBody();
                int responseCode = ((HttpException) e).response().code();
                JSONObject jsonObject = new JSONObject(responseBody.string());
                if (responseCode == 422) {
                    if (jsonObject.has("mobile")) {
                        Toasty.error(this, getErrorMessage(jsonObject), Toast.LENGTH_SHORT).show();
                    }
                    if (jsonObject.has("email")) {
                        Toasty.error(this, jsonObject.getString("email"), Toast.LENGTH_SHORT).show();
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

    public void fbPhoneLogin() {
        final Intent intent = new Intent(this, AccountKitActivity.class);
        AccountKitConfiguration.AccountKitConfigurationBuilder mBuilder =
                new AccountKitConfiguration.AccountKitConfigurationBuilder(
                        LoginType.PHONE, AccountKitActivity.ResponseType.TOKEN);
        mBuilder.setReadPhoneStateEnabled(true);
        mBuilder.setReceiveSMS(true);
        intent.putExtra(AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION,
                mBuilder.setInitialPhoneNumber(new PhoneNumber("+91", "")).
                        build());
        startActivityForResult(intent, APP_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == APP_REQUEST_CODE && data != null) {
            AccountKitLoginResult loginResult = data.getParcelableExtra(AccountKitLoginResult.RESULT_KEY);
            if (!loginResult.wasCancelled()) {
                AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                    @Override
                    public void onSuccess(Account account) {
                        Log.d("AccountKit", "onSuccess: Account Kit" + AccountKit.getCurrentAccessToken().getToken());
                        if (AccountKit.getCurrentAccessToken().getToken() != null) {
                            PhoneNumber phoneNumber = account.getPhoneNumber();
                            SharedHelper.putKey(RegisterActivity.this, Constants.SharedPref.dial_code,
                                    "+" + phoneNumber.getCountryCode());
                            SharedHelper.putKey(RegisterActivity.this, Constants.SharedPref.mobile,
                                    phoneNumber.getPhoneNumber());
                            register(SharedHelper.getKey(RegisterActivity.this, Constants.SharedPref.dial_code),
                                    SharedHelper.getKey(RegisterActivity.this, Constants.SharedPref.mobile));
                        }
                    }

                    @Override
                    public void onError(AccountKitError accountKitError) {
                        Log.e("AccountKit", "onError: Account Kit" + accountKitError);
                    }
                });
            }
        } else if (requestCode == PICK_OTP_VERIFY && data != null) {
            register(data.getExtras().getString("country_code"), data.getExtras().getString("mobile"));
        }
    }
}
