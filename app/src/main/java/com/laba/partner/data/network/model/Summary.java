package com.laba.partner.data.network.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by suthakar@appoets.com on 29-06-2018.
 */
public class Summary {

    @SerializedName("rides")
    @Expose
    private Integer rides;
    @SerializedName("revenue")
    @Expose
    private Float revenue;
    @SerializedName("cancel_rides")
    @Expose
    private Integer cancelRides;
    @SerializedName("scheduled_rides")
    @Expose
    private Integer scheduledRides;

    public Integer getRides() {
        return rides;
    }

    public void setRides(Integer rides) {
        this.rides = rides;
    }

    public Float getRevenue() {
        return revenue;
    }

    public void setRevenue(Float revenue) {
        this.revenue = revenue;
    }

    public Integer getCancelRides() {
        return cancelRides;
    }

    public void setCancelRides(Integer cancelRides) {
        this.cancelRides = cancelRides;
    }

    public Integer getScheduledRides() {
        return scheduledRides;
    }

    public void setScheduledRides(Integer scheduledRides) {
        this.scheduledRides = scheduledRides;
    }
}
