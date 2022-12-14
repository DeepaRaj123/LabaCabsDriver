package com.laba.partner.ui.activity.add_card;

import com.laba.partner.base.MvpView;

public interface AddCardIView extends MvpView {

    void onSuccess(Object card);

    void onError(Throwable e);
}
