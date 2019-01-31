package com.nobroker.nbgeo;

public class AndyConstants {
    public static final long GEOFENCE_EXPIRATION_IN_HOURS = 12;

    /**
     * For this sample, geofences expire after twelve hours.
     */
    public  static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS =
            GEOFENCE_EXPIRATION_IN_HOURS * 60 * 60 * 1000;
    public  static final float GEOFENCE_RADIUS_IN_METERS = 500; // 1 mile, 1.6 km
}
