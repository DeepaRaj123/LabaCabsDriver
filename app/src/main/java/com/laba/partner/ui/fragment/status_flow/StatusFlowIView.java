package com.laba.partner.ui.fragment.status_flow;

import com.laba.partner.base.MvpView;

public interface StatusFlowIView extends MvpView {

    void onSuccess(Object object);

    void onSuccessCancel(Object object);

    void onError(Throwable e);
}
