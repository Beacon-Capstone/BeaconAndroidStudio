package com.capstone.while1.beaconandroidstudio.beacondata;

/**
 * Created by Jared on 4/14/2017.
 */

public class Update
{
    private enum Type { Vote, Something };
    private boolean _wasSuccessful = false;

    public boolean getWasSuccessful()
    {
        return _wasSuccessful;
    }

    public Update(boolean wasSuccessful)
    {
        _wasSuccessful = wasSuccessful;
    }
}
