package com.laba.partner.ui.activity.otp;


import com.laba.partner.base.MvpView;
import com.laba.partner.data.network.model.MyOTP;

public interface OTPIView extends MvpView {
    void onSuccess(MyOTP otp);

    void onError(Throwable e);
}
