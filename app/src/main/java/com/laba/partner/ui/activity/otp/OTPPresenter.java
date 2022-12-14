package com.laba.partner.ui.activity.otp;


import com.laba.partner.base.BasePresenter;
import com.laba.partner.data.network.APIClient;
import com.laba.partner.data.network.model.MyOTP;

import java.util.HashMap;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class OTPPresenter<V extends OTPIView> extends BasePresenter<V> implements OTPIPresenter<V> {

    @Override
    public void sendOTP(HashMap<String, Object> obj) {

        Observable modelObservable = APIClient.getAPIClient().sendOtp(obj);
        modelObservable.subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(trendsResponse -> getMvpView().onSuccess((MyOTP) trendsResponse),
                        throwable -> getMvpView().onError((Throwable) throwable));
    }
}
