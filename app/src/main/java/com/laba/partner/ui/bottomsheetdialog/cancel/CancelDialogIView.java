package com.laba.partner.ui.bottomsheetdialog.cancel;

import com.laba.partner.base.MvpView;

public interface CancelDialogIView extends MvpView {

    void onSuccessCancel(Object object);

    void onError(Throwable e);
}
