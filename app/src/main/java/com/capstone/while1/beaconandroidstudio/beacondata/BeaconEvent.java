package com.capstone.while1.beaconandroidstudio.beacondata;

import android.location.Location;

import java.sql.Timestamp;

/**
 * Created by Aaron on 3/18/2017.
 *
 */

public class BeaconEvent {
    private int id;
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
    }

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

    private void setLatitude(double latitude) {
        this.latitude = location.getLatitude();
    }

    public double getLongitude() {
        return longitude;
    }

    private void setLongitude(double longitude) {
        this.longitude = location.getLongitude();
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
