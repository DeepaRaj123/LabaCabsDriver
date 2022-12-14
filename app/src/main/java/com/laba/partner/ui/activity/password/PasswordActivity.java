package com.laba.partner.ui.activity.password;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.laba.partner.BuildConfig;
import com.laba.partner.R;
import com.laba.partner.base.BaseActivity;
import com.laba.partner.common.Constants;
import com.laba.partner.common.SharedHelper;
import com.laba.partner.common.Utilities;
import com.laba.partner.data.network.model.ForgotResponse;
import com.laba.partner.data.network.model.User;
import com.laba.partner.ui.activity.main.MainActivity;
import com.laba.partner.ui.activity.regsiter.RegisterActivity;
import com.laba.partner.ui.activity.reset_password.ResetActivity;

import org.json.JSONObject;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.dmoral.toasty.Toasty;
import retrofit2.HttpException;
import retrofit2.Response;

public class PasswordActivity extends BaseActivity implements PasswordIView {

    public static String TAG = "";
    PasswordPresenter presenter = new PasswordPresenter();
    @BindView(R.id.back)
    ImageView back;
    @BindView(R.id.password)
    TextInputEditText password;
    @BindView(R.id.sign_up)
    TextView signUp;
    @BindView(R.id.forgot_password)
    TextView forgotPassword;
    @BindView(R.id.next)
    FloatingActionButton next;
    String email = "";

    @Override
    public int getLayoutId() {
        return R.layout.activity_password;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        presenter.attachView(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            email = extras.getString(Constants.SharedPref.email);
            Utilities.printV("email===>", email);
            Utilities.printV("email===>", SharedHelper.getKeyFCM(this, Constants.SharedPref.device_token));
            Utilities.printV("email===>", SharedHelper.getKeyFCM(this, Constants.SharedPref.device_id));
        }

    }

    @OnClick({R.id.back, R.id.sign_up, R.id.forgot_password, R.id.next})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.back:
                activity().onBackPressed();
                break;
            case R.id.sign_up:
                startActivity(new Intent(this, RegisterActivity.class));
                break;
            case R.id.forgot_password:
                showLoading();
                HashMap<String, Object> map = new HashMap<>();
                map.put("email", email);
                presenter.forgot(map);
//                startActivity(new Intent(this, ForgotActivity.class));
                break;
            case R.id.next:
                login();
                break;
            default:
                break;
        }
    }

    private void login() {
        if (password.getText().toString().isEmpty()) {
            Toasty.error(this, getString(R.string.invalid_password),
                    Toast.LENGTH_SHORT, true).show();
            return;
        }
        if (email.isEmpty()) {
            Toasty.error(this, getString(R.string.invalid_email),
                    Toast.LENGTH_SHORT, true).show();
            return;
        }

        HashMap<String, Object> map = new HashMap<>();
        map.put("email", email);
        map.put("password", password.getText().toString());
        map.put("device_type", BuildConfig.DEVICE_TYPE);
        map.put("device_id", SharedHelper.getKeyFCM(this, Constants.SharedPref.device_id));
        map.put("device_token", SharedHelper.getKeyFCM(this, Constants.SharedPref.device_token));

        //map.put("device_token", "device_token");
        //map.put("device_id","device_id");
        presenter.login(map);
        showLoading();
    }

    @Override
    public void onSuccess(ForgotResponse forgotResponse) {
        hideLoading();
        SharedHelper.putKey(this, Constants.SharedPref.txt_email, email);
        SharedHelper.putKey(this, Constants.SharedPref.otp, String.valueOf(forgotResponse.getProvider().getOtp()));
        SharedHelper.putKey(this, Constants.SharedPref.id, String.valueOf(forgotResponse.getProvider().getId()));
        Toasty.success(this, forgotResponse.getMessage(), Toast.LENGTH_SHORT, true).show();
        startActivity(new Intent(this, ResetActivity.class));
    }

    @Override
    public void onSuccess(User user) {
        hideLoading();
        SharedHelper.putKey(this, Constants.SharedPref.access_token, user.getAccessToken());
        SharedHelper.putKey(this, Constants.SharedPref.user_id,
                String.valueOf(user.getId()));
        SharedHelper.putKey(this, Constants.SharedPref.logged_in, "true");
        Toasty.success(activity(), getString(R.string.login_out_success), Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
        finish();


    }

    @Override
    public void onError(Throwable e) {
        hideLoading();
//        if (e != null)
//            TAG = "PasswordActivity";
//        onErrorBase(e);
        if (e instanceof HttpException) {
            Response response = ((HttpException) e).response();
            try {
                JSONObject jObjError = new JSONObject(response.errorBody().string());
                if (jObjError.has("message"))
                    Toast.makeText(activity(), jObjError.optString("message"), Toast.LENGTH_SHORT).show();
                else if (jObjError.has("email"))
                    Toast.makeText(activity(), jObjError.optString("email"), Toast.LENGTH_SHORT).show();
                else if (jObjError.has("error"))
                    Toast.makeText(activity(), jObjError.optString("error"), Toast.LENGTH_SHORT).show();
                else if (jObjError.has("password"))
                    Toast.makeText(activity(), jObjError.optString("password"), Toast.LENGTH_SHORT).show();
            } catch (Exception exp) {
                Log.e("Error", exp.getMessage());
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
    }
}
