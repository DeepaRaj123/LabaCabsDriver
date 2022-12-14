package com.laba.partner.common.countrypicker;

public interface CountryPickerListener {
    void onSelectCountry(String name, String code, String dialCode, int flagDrawableResID);
}
