package com.laba.partner.ui.activity.setting;

import com.laba.partner.base.MvpView;

public interface SettingsIView extends MvpView {

    void onSuccess(Object o);

    void onError(Throwable e);

}
