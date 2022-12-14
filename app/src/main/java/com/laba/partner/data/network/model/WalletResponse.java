package com.laba.partner.data.network.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class WalletResponse {

    @SerializedName("wallet_balance")
    @Expose
    private double walletBalance;
    @SerializedName("wallet_transation")
    @Expose
    private List<Wallet> walletTransactions;

    public WalletResponse() {
    }

    public double getWalletBalance() {
        return walletBalance;
    }

    public void setWalletBalance(double walletBalance) {
        this.walletBalance = walletBalance;
    }

    public List<Wallet> getWalletTransactions() {
        return walletTransactions;
    }

    public void setWalletTransactions(List<Wallet> walletTransactions) {
        this.walletTransactions = walletTransactions;
    }
}
