package com.laba.partner.ui.activity.password;

import com.laba.partner.base.BasePresenter;
import com.laba.partner.data.network.APIClient;

import java.util.HashMap;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class PasswordPresenter<V extends PasswordIView> extends BasePresenter<V> implements PasswordIPresenter<V> {

    @Override
    public void login(HashMap<String, Object> obj) {

        getCompositeDisposable().add(
                APIClient
                        .getAPIClient()
                        .login(obj)
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(getMvpView()::onSuccess,
                                getMvpView()::onError));

    }

    @Override
    public void forgot(HashMap<String, Object> obj) {
        getCompositeDisposable().add(
                APIClient
                        .getAPIClient()
                        .forgotPassword(obj)
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                getMvpView()::onSuccess,
                                getMvpView()::onError));
    }
}
