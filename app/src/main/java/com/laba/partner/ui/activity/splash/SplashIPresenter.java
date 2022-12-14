package com.laba.partner.ui.activity.splash;

import com.laba.partner.base.MvpPresenter;

public interface SplashIPresenter<V extends SplashIView> extends MvpPresenter<V> {

    void handlerCall();

    void getPlaces();

}
