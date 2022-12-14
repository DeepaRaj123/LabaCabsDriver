package com.laba.partner.ui.activity.wallet;

import com.laba.partner.base.MvpView;
import com.laba.partner.data.network.model.WalletResponse;

public interface WalletIView extends MvpView {

    void onSuccess(WalletResponse response);

    void onSuccess(Void success);

    void onError(Throwable e);
}
