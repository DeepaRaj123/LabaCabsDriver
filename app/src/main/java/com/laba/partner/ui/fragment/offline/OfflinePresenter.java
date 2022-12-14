package com.laba.partner.ui.fragment.offline;

import com.laba.partner.base.BasePresenter;
import com.laba.partner.data.network.APIClient;

import java.util.HashMap;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class OfflinePresenter<V extends OfflineIView> extends BasePresenter<V> implements OfflineIPresenter<V> {

    @Override
    public void providerAvailable(HashMap<String, Object> obj) {
        getCompositeDisposable().add(APIClient
                .getAPIClient()
                .providerAvailable(obj)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(getMvpView()::onSuccess, getMvpView()::onError));
    }
}
