package com.laba.partner.ui.bottomsheetdialog.cancel;

import com.laba.partner.base.MvpPresenter;

import java.util.HashMap;

public interface CancelDialogIPresenter<V extends CancelDialogIView> extends MvpPresenter<V> {

    void cancelRequest(HashMap<String, Object> obj);
}
