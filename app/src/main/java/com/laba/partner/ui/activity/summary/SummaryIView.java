package com.laba.partner.ui.activity.summary;


import com.laba.partner.base.MvpView;
import com.laba.partner.data.network.model.Summary;

public interface SummaryIView extends MvpView {

    void onSuccess(Summary object);

    void onError(Throwable e);
}
