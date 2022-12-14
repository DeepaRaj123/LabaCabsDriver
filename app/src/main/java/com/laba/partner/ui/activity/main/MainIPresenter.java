package com.laba.partner.ui.activity.main;

import com.laba.partner.base.MvpPresenter;

import java.util.HashMap;

public interface MainIPresenter<V extends MainIView> extends MvpPresenter<V> {

    void getProfile();

    void logout(HashMap<String, Object> obj);

    void getTrip(HashMap<String, Object> params);

    void providerAvailable(HashMap<String, Object> obj);

//    void sendFCM(JsonObject jsonObject);

    void getTripLocationUpdate(HashMap<String, Object> params);

    void settings();

    void check();


}
