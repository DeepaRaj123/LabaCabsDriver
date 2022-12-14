package com.laba.partner.ui.activity.forgot_password;

import com.laba.partner.base.MvpView;
import com.laba.partner.data.network.model.ForgotResponse;

public interface ForgotIView extends MvpView {

    void onSuccess(ForgotResponse forgotResponse);

    void onError(Throwable e);
}
