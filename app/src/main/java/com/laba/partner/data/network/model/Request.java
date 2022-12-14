package com.laba.partner.data.network.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Request {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("request_id")
    @Expose
    private Integer requestId;
    @SerializedName("provider_id")
    @Expose
    private Integer providerId;
    @SerializedName("status")
    @Expose
    private Integer status;
    @SerializedName("time_left_to_respond")
    @Expose
    private Integer timeLeftToRespond;
    @SerializedName("request")
    @Expose
    private Request_ request;
    @SerializedName("payment")
    @Expose
    private Payment payment;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getRequestId() {
        return requestId;
    }

    public void setRequestId(Integer requestId) {
        this.requestId = requestId;
    }

    public Integer getProviderId() {
        return providerId;
    }

    public void setProviderId(Integer providerId) {
        this.providerId = providerId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getTimeLeftToRespond() {
        return timeLeftToRespond;
    }

    public void setTimeLeftToRespond(Integer timeLeftToRespond) {
        this.timeLeftToRespond = timeLeftToRespond;
    }

    public Request_ getRequest() {
        return request;
    }

    public void setRequest(Request_ request) {
        this.request = request;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }
}
