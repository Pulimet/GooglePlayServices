package net.alexandroid.googleplayservices;


import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;

public class Constatnts {
    public static final String PACKAGE_NAME = "net.alexandroid.googleplayservices";
    public static final String BROADCAST_ACTION = PACKAGE_NAME + ".BROADCAST_ACTION";
    public static final String ACTIVITY_EXTRA = PACKAGE_NAME + ".ACTIVITY_EXTRA";

    public static final String SHARED_PREFERENCES_NAME = PACKAGE_NAME + ".SHARED_PREFERENCES_NAME";
    public static final String GEOFENCES_ADDED_KEY = PACKAGE_NAME + ".GEOFENCES_ADDED_KEY";

    public static final long GEOFENCE_EXPIRATION_IN_HOURS = 12;
    public static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS = GEOFENCE_EXPIRATION_IN_HOURS * 60 * 60 * 1000;
    public static final float GEOFENCE_RADIUS_IN_METERS = 1; // 1 mile, 1.6 km


    public static final HashMap<String, LatLng> BAY_AREA_LANDMARKS = new HashMap<>();

    static {
        BAY_AREA_LANDMARKS.put("WORK", new LatLng(32.0604473, 34.7821697));
        BAY_AREA_LANDMARKS.put("HOME", new LatLng(32.0242456, 34.7591935));
        BAY_AREA_LANDMARKS.put("PARENTS", new LatLng(31.9359943, 34.8373441));
        BAY_AREA_LANDMARKS.put("WOLFSON", new LatLng(32.0361652, 34.7635518));
    }
}
