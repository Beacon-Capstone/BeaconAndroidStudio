package com.capstone.while1.beaconandroidstudio.beacondata;

/**
 * Created by Jared on 4/14/2017.
 */

public class Event {
    public Integer id;
    public Integer creatorId;
    public String name;
    public String description;
    public String timeLastUpdated;
    public Double latitude;
    public Double longitude;
    public Integer voteCount;
    public String timeCreated;
    public Boolean deleted;


    public Event(Integer id, Integer creatorId, String name, String description, String timeLastUpdated,
                 Double latitude, Double longitude, Integer voteCount, String timeCreated, Boolean deleted) {
        this.id = id;
        this.creatorId = creatorId;
        this.name = name;
        this.description = description;
        this.timeLastUpdated = timeLastUpdated;
        this.latitude = latitude;
        this.longitude = longitude;
        this.voteCount = voteCount;
        this.timeCreated = timeCreated;
        this.deleted = deleted;
    }

    public Event() {

    }
}
