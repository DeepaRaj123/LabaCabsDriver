package com.laba.partner.ui.bottomsheetdialog.rating;

import com.laba.partner.base.MvpView;
import com.laba.partner.data.network.model.Rating;

public interface RatingDialogIView extends MvpView {

    void onSuccess(Rating rating);

    void onError(Throwable e);
}
