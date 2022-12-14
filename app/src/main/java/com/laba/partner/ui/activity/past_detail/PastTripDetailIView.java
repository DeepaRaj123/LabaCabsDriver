package com.laba.partner.ui.activity.past_detail;


import com.laba.partner.base.MvpView;
import com.laba.partner.data.network.model.HistoryDetail;

public interface PastTripDetailIView extends MvpView {

    void onSuccess(HistoryDetail historyDetail);

    void onError(Throwable e);
}
