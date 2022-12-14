package com.laba.partner.ui.fragment.upcoming;

import com.laba.partner.base.MvpView;
import com.laba.partner.data.network.model.HistoryList;

import java.util.List;

public interface UpcomingTripIView extends MvpView {

    void onSuccess(List<HistoryList> historyList);

    void onError(Throwable e);
}
