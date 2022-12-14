package com.laba.partner.ui.activity.change_password;

import com.laba.partner.base.MvpView;

public interface ChangePasswordIView extends MvpView {


    void onSuccess(Object object);

    void onError(Throwable e);
}
