package com.laba.partner.ui.activity.earnings;


import com.laba.partner.base.MvpView;
import com.laba.partner.data.network.model.EarningsList;

public interface EarningsIView extends MvpView {

    void onSuccess(EarningsList earningsLists);

    void onError(Throwable e);
}
