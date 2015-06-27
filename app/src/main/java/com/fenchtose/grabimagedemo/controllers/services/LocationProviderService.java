package com.fenchtose.grabimagedemo.controllers.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.fenchtose.grabimagedemo.controllers.events.LocationRequestEvent;
import com.fenchtose.grabimagedemo.controllers.events.LocationResponseEvent;

import de.greenrobot.event.EventBus;

/**
 * Created by Jay Rambhia on 26/06/15.
 */
public class LocationProviderService extends Service {

    private static final String TAG = "LocationProviderService";
    private LocationManager mLocationManager;

    private Location mLatestLocationHighAccuracy;
    private Location mLatestLocationLowAccuracy;

    private boolean gpsEnabled = false;

    private EventBus mEventBus;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {

        mEventBus = EventBus.getDefault();
        mEventBus.register(this);

        mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        Criteria highAccuracyCriteria = new Criteria();
        highAccuracyCriteria.setPowerRequirement(Criteria.POWER_HIGH);
        highAccuracyCriteria.setSpeedRequired(true);
        highAccuracyCriteria.setAccuracy(Criteria.ACCURACY_FINE);

        String mProvider1 = mLocationManager.getBestProvider(highAccuracyCriteria, true);
        mLatestLocationHighAccuracy = mLocationManager.getLastKnownLocation(mProvider1);
        Log.i(TAG, "mProvider1: " + mProvider1);
        if (mLatestLocationHighAccuracy != null) {
            Log.i(TAG, "last known location high accuracy: " + mLatestLocationHighAccuracy);
        }
        if (mProvider1 == null || mProvider1.equals(LocationManager.PASSIVE_PROVIDER)) {
            gpsEnabled = false;
            return;
        }

        Criteria lowAccuracyCriteria = new Criteria();
        lowAccuracyCriteria.setPowerRequirement(Criteria.POWER_LOW);
        lowAccuracyCriteria.setSpeedRequired(true);
        lowAccuracyCriteria.setAccuracy(Criteria.ACCURACY_COARSE);

        String mProvider2 = mLocationManager.getBestProvider(lowAccuracyCriteria, true);
        mLatestLocationLowAccuracy = mLocationManager.getLastKnownLocation(mProvider2);
        if (mLatestLocationLowAccuracy != null) {
            Log.i(TAG, "last known location high accuracy: " + mLatestLocationLowAccuracy);
        }
        Log.i(TAG, "mProvider2: " + mProvider2);
        if (mProvider2 == null || mProvider1.equals(LocationManager.PASSIVE_PROVIDER)) {
            gpsEnabled = false;
            return;
        }

        mLocationManager.requestLocationUpdates(mProvider1, 0, 0, mLocationListener1);
        mLocationManager.requestLocationUpdates(mProvider2, 0, 0, mLocationListener2);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }


    private LocationListener mLocationListener1 = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.i(TAG, "onLocationChanged1: " + location.toString());
            mEventBus.post(new LocationResponseEvent(LocationResponseEvent.LOCATION_UPDATE,
                    location));
            mLatestLocationHighAccuracy = location;
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    private LocationListener mLocationListener2 = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.i(TAG, "onLocationChanged2: " + location.toString());
            mEventBus.post(new LocationResponseEvent(LocationResponseEvent.LOCATION_UPDATE,
                    location));
            mLatestLocationLowAccuracy = location;
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    private void stopAndQuit() {
        mLocationManager.removeUpdates(mLocationListener1);
        mLocationManager.removeUpdates(mLocationListener2);
        stopSelf();
    }

    private void sendLastKnownLocation() {
        if (mLatestLocationHighAccuracy != null) {
            mEventBus.post(new LocationResponseEvent(LocationResponseEvent.LAST_KNOWN_LOCATION,
                    mLatestLocationHighAccuracy));
        }

        if (mLatestLocationLowAccuracy != null) {
            mEventBus.post(new LocationResponseEvent(LocationResponseEvent.LAST_KNOWN_LOCATION,
                    mLatestLocationLowAccuracy));
        }

    }

    public void onEvent(LocationRequestEvent event) {
        switch (event.getType()) {
            case LocationRequestEvent.REQUEST_STOP:
            case LocationRequestEvent.REQUEST_STOP_SELF:
                stopAndQuit();
                break;

            case LocationRequestEvent.REQUEST_LOCATION:
                sendLastKnownLocation();
                break;
        }
    }
}
