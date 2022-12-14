package com.laba.partner.ui.activity.help;


import com.laba.partner.base.MvpPresenter;

public interface HelpIPresenter<V extends HelpIView> extends MvpPresenter<V> {

    void getHelp();
}
