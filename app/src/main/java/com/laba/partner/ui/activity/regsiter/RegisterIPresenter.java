package com.laba.partner.ui.activity.regsiter;

import com.laba.partner.base.MvpPresenter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.http.FieldMap;
import retrofit2.http.Part;
import retrofit2.http.PartMap;

public interface RegisterIPresenter<V extends RegisterIView> extends MvpPresenter<V> {

    void register(@PartMap Map<String, RequestBody> params, @Part List<MultipartBody.Part> file);

    void verifyEmail(@FieldMap HashMap<String, Object> params);

    void sendOTP(HashMap<String, Object> map);

}
