package com.laba.partner.ui.activity.splash;

import com.laba.partner.base.MvpView;

public interface SplashIView extends MvpView {


    void redirectHome();

    void onSuccess(Object user);

    void onError(Throwable e);
}
