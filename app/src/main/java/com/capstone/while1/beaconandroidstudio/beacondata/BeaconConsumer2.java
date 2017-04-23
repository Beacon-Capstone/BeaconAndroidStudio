package com.capstone.while1.beaconandroidstudio.beacondata;

/**
 * Created by Jared on 4/22/2017.
 */

public interface BeaconConsumer2<T, U> {
    void accept(T obj1, U obj2);
}