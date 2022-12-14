package com.laba.partner.ui.activity.wallet;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.laba.partner.R;
import com.laba.partner.base.BaseActivity;
import com.laba.partner.common.Constants;
import com.laba.partner.common.SharedHelper;
import com.laba.partner.data.network.model.User;
import com.laba.partner.data.network.model.UserResponse;
import com.laba.partner.data.network.model.WalletResponse;
import com.laba.partner.ui.activity.profile.ProfileIView;
import com.laba.partner.ui.activity.profile.ProfilePresenter;
import com.laba.partner.ui.activity.request_money.RequestMoneyActivity;
import com.laba.partner.ui.adapter.WalletAdapter;
import com.laba.partner.ui.dialog.PaymentSuccessfulDialog;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class WalletActivity extends BaseActivity implements WalletIView, PaymentResultListener,
        ProfileIView {

    WalletPresenter mPresenter = new WalletPresenter();
    ProfilePresenter profilePresenter = new ProfilePresenter();
    UserResponse user = null;
    boolean isUserTyringPayment = false;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.tvWalletAmt)
    TextView tvWalletAmt;
    @BindView(R.id.rvWalletData)
    RecyclerView rvWalletData;
    @BindView(R.id.tvWalletPlaceholder)
    TextView tvWalletPlaceholder;
    @BindView(R.id.llWalletHistory)
    LinearLayout llWalletHistory;
    @BindView(R.id.ivRequestMoney)
    ImageView ivRequestMoney;

    @BindView(R.id._199)
    Button bt199;

    @BindView(R.id._599)
    Button bt599;

    @BindView(R.id._1099)
    Button bt1099;

    @BindView(R.id.amount)
    EditText edAmount;

    @BindView(R.id.addMoneyBt)
    Button btAddMoney;

    private double walletAmt;

    @Override
    public int getLayoutId() {
        return R.layout.activity_wallet;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        mPresenter.attachView(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.wallet));
        showLoading();

        profilePresenter.attachView(this);
        profilePresenter.getProfile();


     /*   if (SharedHelper.getIntKey(this, "card") == 0) {
            ivRequestMoney.setVisibility(View.GONE);
        } else {
            ivRequestMoney.setVisibility(View.VISIBLE);
        }*/
        mPresenter.getWalletData();
//        rvWalletData.setLayoutManager(new LinearLayoutManager(activity(), LinearLayoutManager.VERTICAL, false));
        rvWalletData.setLayoutManager(new LinearLayoutManager(this));
        rvWalletData.setItemAnimator(new DefaultItemAnimator());
//        rvWalletData.setHasFixedSize(true);
        rvWalletData.setHasFixedSize(false);
        rvWalletData.setNestedScrollingEnabled(false);

        edAmount.setTag(SharedHelper.getKey(this, "currency"));
        bt199.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edAmount.setText("199");
            }
        });

        bt599.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edAmount.setText("599");
            }
        });

        bt1099.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edAmount.setText("1099");
            }
        });

        btAddMoney.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String amount = edAmount.getText().toString();
                if (amount.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please provide an amount",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                if (user == null) {
                    profilePresenter.getProfile();
                    isUserTyringPayment = true;
                } else {
                    initPayment(Integer.parseInt(amount) * 100, user.getMobile(), user.getEmail());
                }

//                startPayment(Integer.parseInt(amount)*100);

            }
        });
    }

//    public void startPayment(int amount) {
//        Checkout checkout = new Checkout();
//        checkout.setKeyID("PTklg8XVf7rwZNbJPo3aNXzo");
//        /**
//         * Instantiate Checkout
//         */
//
//        /**
//         * Set your logo here
//         */
////        checkout.setImage(R.drawable.app_icon);
//
//        /**
//         * Reference to current activity
//         */
//        final Activity activity = this;
//
//        /**
//         * Pass your payment options to the Razorpay Checkout as a JSONObject
//         */
//        try {
//            JSONObject options = new JSONObject();
//
//            options.put("name", "Merchant Name");
//            options.put("description", "Reference No. #123456");
//            options.put("image", "https://s3.amazonaws.com/rzp-mobile/images/rzp.png");
//            options.put("order_id", "order_DBJOWzybf0sJbb");//from response of step 3.
//            options.put("theme.color", "#3399cc");
//            options.put("currency", "INR");
//            options.put("amount", "50000");//pass amount in currency subunits
//            options.put("prefill.email", "gaurav.kumar@example.com");
//            options.put("prefill.contact","9988776655");
//            JSONObject retryObj = new JSONObject();
//            retryObj.put("enabled", true);
//            retryObj.put("max_count", 4);
//            options.put("retry", retryObj);
//
//            checkout.open(activity, options);
//
//        } catch(Exception e) {
//            Log.e("razorpay exception", "Error in starting Razorpay Checkout", e);
//        }
//    }

    public void initPayment(int amount, String mobileSt, String emailSt) {
        String mail = SharedHelper.getKey(this, Constants.SharedPref.txt_email);

//        String mail = SharedHelper.getKey(getApplicationContext(), "mail");
        //String mobile = SharedHelper.getKey(getApplicationContext(), "mobile");
        Checkout checkout = new Checkout();
        checkout.setKeyID("rzp_live_HEclv53o7Kz8PI");
        checkout.setImage(R.drawable.app_icon);
        final Activity activity = this;
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("name", getString(R.string.app_name));
            jsonObject.put("description", getString(R.string.app_name));
            jsonObject.put("currency", "INR");
            jsonObject.put("amount", amount);
            JSONObject preFill = new JSONObject();
            /*if (mail.equalsIgnoreCase("")) {
                preFill.put("email", "");
            } else {
                preFill.put("email", mail);
            }*/
            /*if (mobile.equalsIgnoreCase("")) {
                preFill.put("contact", "");
            } else {
                preFill.put("contact", mobile);
            }*/
            preFill.put("email", emailSt);
            preFill.put("contact", mobileSt);
            jsonObject.put("prefill", preFill);
            checkout.open(activity, jsonObject);
        } catch (JSONException e) {
            Log.e("razor pay error", e.toString());
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSuccess(WalletResponse response) {
        hideLoading();

        walletAmt = response.getWalletBalance();
        tvWalletAmt.setText(String.format("%s %s", Constants.Currency, walletAmt));
//        tvWalletAmt.setText(String.format("%s %s", Constants.Currency,
//                MvpApplication.getInstance().getNewNumberFormat(walletAmt)));
        if (response.getWalletTransactions() != null && response.getWalletTransactions().size() > 0) {
            rvWalletData.setAdapter(new WalletAdapter(response.getWalletTransactions()));
            llWalletHistory.setVisibility(View.VISIBLE);
            tvWalletPlaceholder.setVisibility(View.GONE);
        } else {
            llWalletHistory.setVisibility(View.GONE);
            tvWalletPlaceholder.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onSuccess(Void success) {
        Toast.makeText(getApplicationContext(), "Payment updated!", Toast.LENGTH_SHORT).show();
        mPresenter.getWalletData();
    }

    @Override
    public void onSuccess(UserResponse user) {
        this.user = user;

        if (isUserTyringPayment) {
            String amount = edAmount.getText().toString();

            if (amount.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Please provide an amount",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            initPayment(Integer.parseInt(amount) * 100, user.getMobile(), user.getEmail());
        }
    }

    @Override
    public void onSuccessUpdate(UserResponse object) {

    }

    @Override
    public void onError(Throwable e) {
        hideLoading();
        if (e != null)
            onErrorBase(e);
    }

    @OnClick(R.id.ivRequestMoney)
    public void onViewClicked() {
        startActivity(new Intent(this, RequestMoneyActivity.class).putExtra("WalletAmt", walletAmt));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
    }

    @Override
    public void onPaymentSuccess(String s) {
//        Toast.makeText(getApplicationContext(),"Payment success",Toast.LENGTH_SHORT).show();
        mPresenter.razoryPaySuccess(s);
        /**
         * work around. The api was given http error response while doing it.
         * this should change when the api is giving valid response
         * on success will call when .razorpaysuccess is returned*/
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mPresenter.getWalletData();
            }
        }, 2000);

        edAmount.setText("");
        new PaymentSuccessfulDialog(WalletActivity.this).show();
    }

    @Override
    public void onPaymentError(int i, String s) {
        Toast.makeText(getApplicationContext(), "Your payment is not complete!",
                Toast.LENGTH_SHORT).show();
    }
}
