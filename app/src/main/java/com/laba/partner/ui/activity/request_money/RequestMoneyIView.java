package com.laba.partner.ui.activity.request_money;

import com.laba.partner.base.MvpView;
import com.laba.partner.data.network.model.RequestDataResponse;

public interface RequestMoneyIView extends MvpView {

    void onSuccess(RequestDataResponse response);

    void onSuccess(Object response);

    void onError(Throwable e);

}
