package com.laba.partner.ui.activity.splash;

import android.os.Handler;

import com.laba.partner.base.BasePresenter;
import com.laba.partner.data.network.APIClient;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class SplashPresenter<V extends SplashIView> extends BasePresenter<V> implements SplashIPresenter<V> {

    @Override
    public void handlerCall() {
        new Handler().postDelayed(() -> {
            //Write your code here
            getMvpView().redirectHome();
        }, 2000); //Timer is in ms here.

    }

    @Override
    public void getPlaces() {
        getCompositeDisposable().add(
                APIClient
                        .getAPIClient()
                        .getPlaces()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                object -> getMvpView().onSuccess(object),
                                throwable -> getMvpView().onError(throwable)
                        )
        );
    }
}
