package com.laba.partner.ui.activity.sociallogin;

import com.laba.partner.base.MvpView;
import com.laba.partner.data.network.model.Token;

public interface SocialLoginIView extends MvpView {

    void onSuccess(Token token);

    void onError(Throwable e);
}
