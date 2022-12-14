package com.laba.partner.ui.activity.wallet;

import com.laba.partner.base.MvpPresenter;

public interface WalletIPresenter<V extends WalletIView> extends MvpPresenter<V> {

    void getWalletData();

    void razoryPaySuccess(String s);
}
