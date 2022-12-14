package com.laba.partner.ui.activity.add_card;

import com.laba.partner.base.BasePresenter;
import com.laba.partner.data.network.APIClient;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class AddCardPresenter<V extends AddCardIView> extends BasePresenter<V> implements AddCardIPresenter<V> {

    @Override
    public void addCard(String stripeToken) {

        getCompositeDisposable().add(APIClient.getAPIClient().addcard(stripeToken)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io()).
                        subscribe(card -> getMvpView().onSuccess(card),
                                throwable -> getMvpView().onError(throwable)));

    }
}
