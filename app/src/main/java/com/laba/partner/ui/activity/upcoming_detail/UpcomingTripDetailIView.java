package com.laba.partner.ui.activity.upcoming_detail;


import com.laba.partner.base.MvpView;
import com.laba.partner.data.network.model.HistoryDetail;

public interface UpcomingTripDetailIView extends MvpView {

    void onSuccess(HistoryDetail historyDetail);

    void onError(Throwable e);
}
