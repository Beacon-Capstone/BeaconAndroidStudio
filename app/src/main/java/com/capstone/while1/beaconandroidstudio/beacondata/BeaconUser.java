package com.capstone.while1.beaconandroidstudio.beacondata;

/**
 * Created by Aaron on 3/18/2017.
 *
 */

public class BeaconUser {
    private int id;
    private int firstName;
    private int lastName;
    private String hashedPassword;
    private String salt;
    private String userName;
    private int currentAttendedEventId;

    public BeaconUser(int id, int firstName, int lastName, String hashedPassword, String salt, String userName, int currentAttendedEventId) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.hashedPassword = hashedPassword;
        this.salt = salt;
        this.userName = userName;
        this.currentAttendedEventId = currentAttendedEventId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getFirstName() {
        return firstName;
    }

    public void setFirstName(int firstName) {
        this.firstName = firstName;
    }

    public int getLastName() {
        return lastName;
    }

    public void setLastName(int lastName) {
        this.lastName = lastName;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getCurrentAttendedEventId() {
        return currentAttendedEventId;
    }

    public void setCurrentAttendedEventid(int currentAttendedEventId) {
        this.currentAttendedEventId = currentAttendedEventId;
    }
}
