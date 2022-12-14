package com.laba.partner.ui.activity.help;

import com.laba.partner.base.MvpView;
import com.laba.partner.data.network.model.Help;

public interface HelpIView extends MvpView {

    void onSuccess(Help object);

    void onError(Throwable e);
}
