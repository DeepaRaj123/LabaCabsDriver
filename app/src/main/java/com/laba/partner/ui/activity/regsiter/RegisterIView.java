package com.laba.partner.ui.activity.regsiter;

import com.laba.partner.base.MvpView;
import com.laba.partner.data.network.model.MyOTP;
import com.laba.partner.data.network.model.User;

public interface RegisterIView extends MvpView {

    void onSuccess(User user);

    void onSuccess(Object verifyEmail);

    void onSuccess(MyOTP otp);

    void onError(Throwable e);

}
