package com.laba.partner.data.network.model;

public class HomeCheck {
    private String document_status;
    private String wallet_status;
    private String sevendays, provider_status;
    private String document_check;
    private String provider_enable_status;

    public String getProvider_enable_status() {
        return provider_enable_status;
    }

    public void setProvider_enable_status(String provider_enable_status) {
        this.provider_enable_status = provider_enable_status;
    }

    public String getDocument_check() {
        return document_check;
    }

    public void setDocument_check(String document_check) {
        this.document_check = document_check;
    }

    public String getDocument_status() {
        return document_status;
    }

    public void setDocument_status(String document_status) {
        this.document_status = document_status;
    }

    public String getWallet_status() {
        return wallet_status;
    }

    public void setWallet_status(String wallet_status) {
        this.wallet_status = wallet_status;
    }

    public String getSevendays() {
        return sevendays;
    }

    public void setSevendays(String sevendays) {
        this.sevendays = sevendays;
    }

    public String getProvider_status() {
        return provider_status;
    }

    public void setProvider_status(String provider_status) {
        this.provider_status = provider_status;
    }
}
