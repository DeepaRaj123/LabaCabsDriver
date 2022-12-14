package com.laba.partner.ui.activity.reset_password;

import com.laba.partner.base.MvpView;

public interface ResetIView extends MvpView {

    void onSuccess(Object object);

    void onError(Throwable e);
}
