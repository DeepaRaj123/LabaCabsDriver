package com.laba.partner.ui.activity.summary;


import com.laba.partner.base.MvpPresenter;

public interface SummaryIPresenter<V extends SummaryIView> extends MvpPresenter<V> {

    void getSummary();
}
