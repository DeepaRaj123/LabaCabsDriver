package com.laba.partner.ui.activity.regsiter;

import com.laba.partner.base.BasePresenter;
import com.laba.partner.data.network.APIClient;
import com.laba.partner.data.network.model.MyOTP;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.http.Part;
import retrofit2.http.PartMap;

public class RegisterPresenter<V extends RegisterIView> extends BasePresenter<V> implements RegisterIPresenter<V> {

    @Override
    public void register(@PartMap Map<String, RequestBody> params, @Part List<MultipartBody.Part> files) {
        getCompositeDisposable().add(
                APIClient
                        .getAPIClient()
                        .register(params, files)
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(getMvpView()::onSuccess, getMvpView()::onError));
    }

    @Override
    public void verifyEmail(HashMap<String, Object> params) {
        getCompositeDisposable().add(
                APIClient
                        .getAPIClient()
                        .verifyEmail(params)
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(getMvpView()::onSuccess, getMvpView()::onError));
    }

    @Override
    public void sendOTP(HashMap<String, Object> obj) {

        Observable modelObservable = APIClient.getAPIClient().sendOtp(obj);
        modelObservable.subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(trendsResponse -> getMvpView().onSuccess((MyOTP) trendsResponse),
                        throwable -> getMvpView().onError((Throwable) throwable));
    }
}
