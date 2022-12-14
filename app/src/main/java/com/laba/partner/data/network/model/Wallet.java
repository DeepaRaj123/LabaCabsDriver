package com.laba.partner.data.network.model;

import com.google.gson.annotations.SerializedName;

public class Wallet {

    @SerializedName("user_id")
    private int userId;
    @SerializedName("transaction_id")
    private int transactionId;
    @SerializedName("transaction_alias")
    private String transactionAlias;
    @SerializedName("transaction_desc")
    private String transactionDesc;
    private String type;
    private double amount;
    @SerializedName("open_balance")
    private double openBalance;
    @SerializedName("close_balance")
    private double closeBalance;
    @SerializedName("created_at")
    private String createdAt;

    public Wallet() {
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public String getTransactionAlias() {
        return transactionAlias;
    }

    public void setTransactionAlias(String transactionAlias) {
        this.transactionAlias = transactionAlias;
    }

    public String getTransactionDesc() {
        return transactionDesc;
    }

    public void setTransactionDesc(String transactionDesc) {
        this.transactionDesc = transactionDesc;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getOpenBalance() {
        return openBalance;
    }

    public void setOpenBalance(double openBalance) {
        this.openBalance = openBalance;
    }

    public double getCloseBalance() {
        return closeBalance;
    }

    public void setCloseBalance(double closeBalance) {
        this.closeBalance = closeBalance;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
