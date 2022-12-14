package com.laba.partner.ui.fragment.past;


import com.laba.partner.base.MvpPresenter;

public interface PastTripIPresenter<V extends PastTripIView> extends MvpPresenter<V> {

    void getHistory();

}
