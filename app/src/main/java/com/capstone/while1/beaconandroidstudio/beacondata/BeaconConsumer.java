package com.capstone.while1.beaconandroidstudio.beacondata;

/**
 * Created by Jared on 4/22/2017.
 */

public interface BeaconConsumer<T> {
    void accept(T obj);
}