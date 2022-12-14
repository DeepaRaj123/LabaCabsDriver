package com.laba.partner.ui.fragment.offline;

import com.laba.partner.base.MvpView;

public interface OfflineIView extends MvpView {

    void onSuccess(Object object);

    void onError(Throwable e);
}
