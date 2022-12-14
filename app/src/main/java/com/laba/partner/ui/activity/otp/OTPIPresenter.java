package com.laba.partner.ui.activity.otp;

import com.laba.partner.base.MvpPresenter;

import java.util.HashMap;


public interface OTPIPresenter<V extends OTPIView> extends MvpPresenter<V> {
    void sendOTP(HashMap<String, Object> map);
}
