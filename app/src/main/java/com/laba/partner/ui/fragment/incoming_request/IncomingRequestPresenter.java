package com.laba.partner.ui.fragment.incoming_request;

import com.laba.partner.base.BasePresenter;
import com.laba.partner.data.network.APIClient;

import java.util.HashMap;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class IncomingRequestPresenter<V extends IncomingRequestIView>
        extends BasePresenter<V>
        implements IncomingRequestIPresenter<V> {

    @Override
    public void accept(Integer id) {
        getCompositeDisposable().add(
                APIClient
                        .getAPIClient()
                        .acceptRequest("", id)
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(getMvpView()::onSuccessAccept,
                                getMvpView()::onError));
    }


    @Override
    public void cancel(Integer id) {
        getCompositeDisposable().add(
                APIClient
                        .getAPIClient()
                        .rejectRequest(id)
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(getMvpView()::onSuccessCancel,
                                getMvpView()::onError));
    }

    @Override
    public void rate(HashMap<String, Object> obj, Integer id) {
        getCompositeDisposable().add(
                APIClient
                        .getAPIClient()
                        .ratingRequest(obj, id)
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                trendsResponse -> getMvpView().onSuccess(trendsResponse),
                                throwable -> getMvpView().onError(throwable)
                        )
        );
    }

    @Override
    public void getProfile() {
        getCompositeDisposable().add(APIClient
                .getAPIClient()
                .getProfile()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(getMvpView()::onSuccess, getMvpView()::onError));
    }


}
