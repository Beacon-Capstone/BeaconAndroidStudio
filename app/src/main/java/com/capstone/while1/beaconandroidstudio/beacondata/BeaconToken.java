package com.capstone.while1.beaconandroidstudio.beacondata;

/**
 * Created by Aaron on 3/18/2017.
 *
 */

public class BeaconToken {
    private int value;
    private int correspondingLoginId;

    public BeaconToken(int value, int correspondingLoginId) {
        this.value = value;
        this.correspondingLoginId = correspondingLoginId;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getCorrespondingLoginId() {
        return correspondingLoginId;
    }

    public void setCorrespondingLoginId(int correspondingLoginId) {
        this.correspondingLoginId = correspondingLoginId;
    }
}
