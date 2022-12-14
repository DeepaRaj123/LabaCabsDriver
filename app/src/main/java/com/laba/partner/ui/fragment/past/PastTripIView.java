package com.laba.partner.ui.fragment.past;


import com.laba.partner.base.MvpView;
import com.laba.partner.data.network.model.HistoryList;

import java.util.List;

public interface PastTripIView extends MvpView {

    void onSuccess(List<HistoryList> historyList);

    void onError(Throwable e);
}
