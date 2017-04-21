package com.capstone.while1.beaconandroidstudio.beacondata;

import android.location.Location;

import java.sql.Timestamp;

/**
 * Created by Aaron on 3/18/2017.
 *
 */

public class BeaconEvent {
    private int id;
    private String originalName; //only needed for testing with no database (i don't have an autoincrement id system like a DB does
    private String name;
    private String description;
    private Timestamp timeLastUpdated;
    private String creatorName;
    private double latitude;
    private double longitude;
    private Location location;

    public BeaconEvent(int id, String name, String description, Timestamp timeLastUpdated, String creatorName, double latitude, double longitude, Location location) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.timeLastUpdated = timeLastUpdated;
        this.creatorName = creatorName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.location = location;

        this.originalName = name;
    }

    public String getOriginalName() {return this.originalName;}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Timestamp getTimeLastUpdated() {
        return timeLastUpdated;
    }

    public void setTimeLastUpdated(Timestamp timeLastUpdated) {
        this.timeLastUpdated = timeLastUpdated;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
