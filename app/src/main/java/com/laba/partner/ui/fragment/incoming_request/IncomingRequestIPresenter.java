package com.laba.partner.ui.fragment.incoming_request;

import com.laba.partner.base.MvpPresenter;

import java.util.HashMap;

public interface IncomingRequestIPresenter<V extends IncomingRequestIView> extends MvpPresenter<V> {

    void accept(Integer id);


    void cancel(Integer id);

    void rate(HashMap<String, Object> obj, Integer id);

    void getProfile();


}
