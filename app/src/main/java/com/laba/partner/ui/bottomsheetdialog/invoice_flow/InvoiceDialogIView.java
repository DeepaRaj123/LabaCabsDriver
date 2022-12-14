package com.laba.partner.ui.bottomsheetdialog.invoice_flow;

import com.laba.partner.base.MvpView;

public interface InvoiceDialogIView extends MvpView {

    void onSuccess(Object object);

    void onError(Throwable e);


}
