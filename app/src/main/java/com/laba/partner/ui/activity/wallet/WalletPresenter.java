package com.laba.partner.ui.activity.wallet;

import com.laba.partner.base.BasePresenter;
import com.laba.partner.data.network.APIClient;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class WalletPresenter<V extends WalletIView> extends BasePresenter<V> implements WalletIPresenter<V> {

    @Override
    public void getWalletData() {
        getCompositeDisposable().add(APIClient
                .getAPIClient()
                .getWalletTransactions()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(getMvpView()::onSuccess, getMvpView()::onError));
    }

    @Override
    public void razoryPaySuccess(String s) {
        getCompositeDisposable().add(APIClient
                .getAPIClient()
                .razoryPaySuccess(s)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(getMvpView()::onSuccess, getMvpView()::onError));
    }
}
