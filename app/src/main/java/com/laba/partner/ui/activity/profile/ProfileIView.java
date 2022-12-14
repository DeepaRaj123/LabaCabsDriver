package com.laba.partner.ui.activity.profile;

import com.laba.partner.base.MvpView;
import com.laba.partner.data.network.model.UserResponse;

public interface ProfileIView extends MvpView {

    void onSuccess(UserResponse user);

    void onSuccessUpdate(UserResponse object);

    void onError(Throwable e);

}
