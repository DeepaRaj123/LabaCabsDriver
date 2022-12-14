package com.laba.partner.ui.fragment.incoming_request;

import com.laba.partner.base.MvpView;
import com.laba.partner.data.network.model.Rating;
import com.laba.partner.data.network.model.UserResponse;
import com.laba.partner.ui.RideModel;

public interface IncomingRequestIView extends MvpView {

    void onSuccessAccept(RideModel responseBody);

    void onSuccessCancel(Object object);

    void onError(Throwable e);

    void onSuccess(Rating trendsResponse);

    void onSuccess(UserResponse userResponse);

}
