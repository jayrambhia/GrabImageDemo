package com.fenchtose.grabimagedemo.controllers.events;

import android.location.Location;

/**
 * Created by Jay Rambhia on 26/06/15.
 */
public class LocationResponseEvent {

    public static final int LAST_KNOWN_LOCATION = 1;
    public static final int LOCATION_UPDATE = 2;

    private int type;
    private Location location;

    public LocationResponseEvent(int type, Location location) {
        this.type = type;
        this.location = location;
    }

    public int getType() {
        return type;
    }

    public Location getLocation() {
        return location;
    }
}
