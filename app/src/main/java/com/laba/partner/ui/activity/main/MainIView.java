package com.laba.partner.ui.activity.main;

import com.laba.partner.base.MvpView;
import com.laba.partner.data.network.model.HomeCheck;
import com.laba.partner.data.network.model.InitSettingsResponse;
import com.laba.partner.data.network.model.TripResponse;
import com.laba.partner.data.network.model.UserResponse;

public interface MainIView extends MvpView {
    void onSuccess(UserResponse user);

    void onError(Throwable e);

    void onSuccessLogout(Object object);

    void onSuccess(TripResponse tripResponse);

    void onSuccessProviderAvailable(Object object);

    void onSuccessCheck(HomeCheck homeCheck);

    void onSuccessFCM(Object object);

    void onSuccessLocationUpdate(TripResponse tripResponse);

    void onSuccess(InitSettingsResponse initSettingsResponse);


}
