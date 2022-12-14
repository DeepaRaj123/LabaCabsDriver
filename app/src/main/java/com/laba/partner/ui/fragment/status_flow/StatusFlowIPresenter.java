package com.laba.partner.ui.fragment.status_flow;

import com.laba.partner.base.MvpPresenter;

import java.util.HashMap;

public interface StatusFlowIPresenter<V extends StatusFlowIView> extends MvpPresenter<V> {
    void cancelRequest(HashMap<String, Object> obj);

    void statusUpdate(HashMap<String, Object> obj, Integer id);
}
