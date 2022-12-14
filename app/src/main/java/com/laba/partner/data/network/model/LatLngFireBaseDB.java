package com.laba.partner.data.network.model;

public class LatLngFireBaseDB {

    private double lat, lng;

    public LatLngFireBaseDB() {
    }

    public LatLngFireBaseDB(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }
}
