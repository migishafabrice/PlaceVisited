package com.myapp.placevisitedapp;

import java.util.Date;

public class Place {
    private long id;
    private String title;
    private double latitude;
    private double longitude;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    private String date;

    public Place(long id, String title, double latitude, double longitude,String date) {
        this.id = id;
        this.title = title;
        this.latitude = latitude;
        this.longitude = longitude;
        this.date=date;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Override
    public String toString() {
        return title;
    }
}