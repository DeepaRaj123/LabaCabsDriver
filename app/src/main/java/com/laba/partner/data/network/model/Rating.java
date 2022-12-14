package com.laba.partner.data.network.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Rating {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("request_id")
    @Expose
    private Integer requestId;
    @SerializedName("user_id")
    @Expose
    private Integer userId;
    @SerializedName("provider_id")
    @Expose
    private Integer providerId;
    @SerializedName("user_rating")
    @Expose
    private Integer userRating;
    @SerializedName("provider_rating")
    @Expose
    private Integer providerRating;
    @SerializedName("user_comment")
    @Expose
    private String userComment;
    @SerializedName("provider_comment")
    @Expose
    private String providerComment;

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

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getProviderId() {
        return providerId;
    }

    public void setProviderId(Integer providerId) {
        this.providerId = providerId;
    }

    public Integer getUserRating() {
        return userRating;
    }

    public void setUserRating(Integer userRating) {
        this.userRating = userRating;
    }

    public Integer getProviderRating() {
        return providerRating;
    }

    public void setProviderRating(Integer providerRating) {
        this.providerRating = providerRating;
    }

    public String getUserComment() {
        return userComment;
    }

    public void setUserComment(String userComment) {
        this.userComment = userComment;
    }

    public String getProviderComment() {
        return providerComment;
    }

    public void setProviderComment(String providerComment) {
        this.providerComment = providerComment;
    }

}
