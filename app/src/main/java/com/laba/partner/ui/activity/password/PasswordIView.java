package com.laba.partner.ui.activity.password;

import com.laba.partner.base.MvpView;
import com.laba.partner.data.network.model.ForgotResponse;
import com.laba.partner.data.network.model.User;

public interface PasswordIView extends MvpView {

    void onSuccess(ForgotResponse forgotResponse);

    void onSuccess(User object);

    void onError(Throwable e);
}
