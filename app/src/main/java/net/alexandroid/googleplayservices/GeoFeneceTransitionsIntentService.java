package net.alexandroid.googleplayservices;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;

public class GeoFeneceTransitionsIntentService extends IntentService {
    public static final String TAG = "GeoFeneceTrIntSrvc";

    public GeoFeneceTransitionsIntentService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            String errorMessage = getErrorString(geofencingEvent.getErrorCode());
            Log.e(TAG, errorMessage);
        } else {
            int geoFenceTransition = geofencingEvent.getGeofenceTransition();
            if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER || geoFenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
                List<Geofence> triggeringGeoFences = geofencingEvent.getTriggeringGeofences();
                String geofenceTransitionDetails = getGeofenceTransitionDetails(this, geoFenceTransition, triggeringGeoFences);
                sendNotification(geofenceTransitionDetails);
                Log.i(TAG, geofenceTransitionDetails);
            } else {
                Log.e(TAG, getString(R.string.geofence_transition_invalid_type, geoFenceTransition));
            }

        }

    }

    private void sendNotification(String notificationDetails) {
        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(notificationIntent);
        PendingIntent notifPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);


        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                        .setColor(Color.RED)
                        .setContentTitle(notificationDetails)
                        .setContentText(" :) ")
                        .setContentIntent(notifPendingIntent)
                        .setAutoCancel(true);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, mBuilder.build());
    }


    private String getGeofenceTransitionDetails(Context ctx, int geoFenceTransition, List<Geofence> triggeringGeoFences) {
        String geofenceTransitionString = getTransitionString(geoFenceTransition);
        ArrayList triggerGeofencesIdsList = new ArrayList();
        for (Geofence geofence : triggeringGeoFences) {
            triggerGeofencesIdsList.add(geofence.getRequestId());
        }
        String triggeringGeofencesIdsList = TextUtils.join(", ", triggerGeofencesIdsList);
        return geofenceTransitionString + ": " + triggeringGeofencesIdsList;
    }

    private String getTransitionString(int geoFenceTransition) {
        Resources resources = this.getResources();
        switch (geoFenceTransition) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return resources.getString(R.string.geofence_transition_entered);
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return resources.getString(R.string.geofence_transition_exited);
            default:
                return resources.getString(R.string.geofence_transition_invalid_type);
        }
    }

    private String getErrorString(int type) {
        Resources resources = this.getResources();
        switch (type) {
            case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
                return resources.getString(R.string.geofence_not_available);
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                return resources.getString(R.string.geofence_too_many_geofences);
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                return resources.getString(R.string.geofence_too_many_pending_intents);
            default:
                return resources.getString(R.string.unidentifiable_activity);
        }
    }
}
