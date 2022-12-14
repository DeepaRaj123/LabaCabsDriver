package com.laba.partner.ui.activity.add_card;

import com.laba.partner.base.MvpPresenter;

public interface AddCardIPresenter<V extends AddCardIView> extends MvpPresenter<V> {

    void addCard(String stripeToken);
}
