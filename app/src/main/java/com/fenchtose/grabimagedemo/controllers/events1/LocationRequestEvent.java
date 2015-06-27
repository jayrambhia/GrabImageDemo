package com.fenchtose.grabimagedemo.controllers.events;

/**
 * Created by Jay Rambhia on 26/06/15.
 */
public class LocationRequestEvent {

    public static final int REQUEST_LOCATION  = 1;
    public static final int REQUEST_START = 2;
    public static final int REQUEST_STOP = 3;
    public static final int REQUEST_STOP_SELF = 4;

    private int type;

    public LocationRequestEvent(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
