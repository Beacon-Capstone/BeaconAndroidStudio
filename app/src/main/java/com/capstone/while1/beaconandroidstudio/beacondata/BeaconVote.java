package com.capstone.while1.beaconandroidstudio.beacondata;

/**
 * Created by Aaron on 3/18/2017.
 *
 */

public class BeaconVote {
    private int eventId;
    private int userId;
    private int numVotes;

    public BeaconVote(int eventId, int userId, int numVotes) {
        this.eventId = eventId;
        this.userId = userId;
        this.numVotes = numVotes;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getNumVotes() {
        return numVotes;
    }

    public void setNumVotes(int numVotes) {
        this.numVotes = numVotes;
    }
}
