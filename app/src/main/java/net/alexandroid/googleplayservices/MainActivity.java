package net.alexandroid.googleplayservices;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<Status>,
        LocationListener, View.OnClickListener {

    public static final String TAG = "MainACtivity";
    private TextView tvLocation, tvActivity;
    private GoogleApiClient mGoogleApiClient;
    private boolean toggle;
    private ActivityDetectionBroadcastReceiver mBroadcastReceiver;
    private ArrayList<Geofence> mGofenceList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setViewsAndListeners();

        mBroadcastReceiver = new ActivityDetectionBroadcastReceiver();

        buildGoogleApiClient();

        populateGeofenceList();
    }

    private void setViewsAndListeners() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tvLocation = (TextView) findViewById(R.id.tv_location);
        tvActivity = (TextView) findViewById(R.id.tv_activity);
        findViewById(R.id.fab).setOnClickListener(this);
        findViewById(R.id.addGeofences).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
                if (toggle) {
                    removeActivityUpdatesButtonHandler();
                } else {
                    requestActivityUpdatesButtonHandler();
                }
                break;
            case R.id.addGeofences:
                geofencesButtonHandler();
                break;
        }
    }

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }


    @Override
    public void onConnected(Bundle bundle) {
        // Location part 1
        getLocationOnce();
        // Location part 2
        getLocationUpdates();
    }


    // ------------------ GOOGLE API CONNECTION LISTENERS ---------
    @Override
    public void onResult(Status status) {
        Log.i(TAG, "onResult: " + status.getStatusMessage());
        if (status.isSuccess()) {
            Log.e(TAG, "Success");
        } else {
            Log.e(TAG, "Not success");
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "onConnectionFailed: " + connectionResult.toString());
    }


    // -------------------- GET LOCATION ----------------------
    private void getLocationUpdates() {
        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    private void getLocationOnce() {
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            // Do something with last location
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        tvLocation.setText(
                "Latitude: " + Double.toString(location.getLatitude()) + "\n" +
                        "Longitude: " + Double.toString(location.getLongitude()) + "\n" +
                        "Accuracy: " + location.getAccuracy() + "\n" +
                        "Provider: " + location.getProvider() + "\n" +
                        "Altitude: " + location.getAltitude() + "\n" +
                        "Bearing: " + location.getBearing() + "\n" +
                        "Speed: " + location.getSpeed() + "\n" +
                        "Time: " + location.getTime() + "\n"
        );
    }


    // -------------------- ACTIVITY DETECTION ----------------------

    public String getActivityString(int type) {
        Resources resources = this.getResources();
        switch (type) {
            case DetectedActivity.IN_VEHICLE:
                return resources.getString(R.string.in_vehicle);
            case DetectedActivity.ON_BICYCLE:
                return resources.getString(R.string.on_bicycle);
            case DetectedActivity.ON_FOOT:
                return resources.getString(R.string.on_foot);
            case DetectedActivity.RUNNING:
                return resources.getString(R.string.running);
            case DetectedActivity.STILL:
                return resources.getString(R.string.still);
            case DetectedActivity.TILTING:
                return resources.getString(R.string.tilting);
            case DetectedActivity.UNKNOWN:
                return resources.getString(R.string.unknown);
            case DetectedActivity.WALKING:
                return resources.getString(R.string.walking);
            default:
                return resources.getString(R.string.unidentifiable_activity);
        }
    }

    class ActivityDetectionBroadcastReceiver extends BroadcastReceiver {
        public static final String TAG = "ActDetectBroadRec";

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "onReceive");
            ArrayList<DetectedActivity> detectedActivities = intent.getParcelableArrayListExtra(Constatnts.ACTIVITY_EXTRA);
            StringBuilder result = new StringBuilder();
            for (DetectedActivity act : detectedActivities) {
                result.append("Type: ");
                result.append(getActivityString(act.getType()));
                result.append(" - Confidence: ");
                result.append(act.getConfidence());
                result.append("%\n");
            }
            tvActivity.setText(result.toString());
        }

    }

    public void requestActivityUpdatesButtonHandler() {
        if (!mGoogleApiClient.isConnected()) {
            Toast.makeText(getApplicationContext(), getString(R.string.not_connected), Toast.LENGTH_SHORT).show();
            return;
        }
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(mGoogleApiClient, 0, getActivityDetectionPendingIntent())
                .setResultCallback(this);
        toggle = true;
    }

    public void removeActivityUpdatesButtonHandler() {
        if (!mGoogleApiClient.isConnected()) {
            Toast.makeText(getApplicationContext(), getString(R.string.not_connected), Toast.LENGTH_SHORT).show();
            return;
        }
        ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(mGoogleApiClient, getActivityDetectionPendingIntent())
                .setResultCallback(this);
        toggle = false;
        tvActivity.setText("Disabled");
    }

    private PendingIntent getActivityDetectionPendingIntent() {
        Intent intent = new Intent(this, DetectedActivitiesIntentService.class);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }


    // -------------------- GEOFENCE ----------------------

    private void geofencesButtonHandler() {
        if (!mGoogleApiClient.isConnected()) {
            Toast.makeText(this, getString(R.string.not_connected), Toast.LENGTH_SHORT).show();
            return;
        } else {
            Toast.makeText(this, getString(R.string.connected), Toast.LENGTH_SHORT).show();
        }

        try {
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    getGeofencingRequest(),
                    getGeofencePendingIntent()
            ).setResultCallback(this); // Result processed in onResult().
        } catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
        }

    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGofenceList);
        return builder.build();
    }

    private void populateGeofenceList() {
        for (Map.Entry<String, LatLng> entry : Constatnts.BAY_AREA_LANDMARKS.entrySet()) {
            mGofenceList.add(new Geofence.Builder()
                    .setRequestId(entry.getKey())
                    .setCircularRegion(entry.getValue().latitude, entry.getValue().longitude, Constatnts.GEOFENCE_RADIUS_IN_METERS)
                    .setExpirationDuration(Constatnts.GEOFENCE_EXPIRATION_IN_MILLISECONDS)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build());
        }
    }

    private PendingIntent getGeofencePendingIntent() {
        Intent intent = new Intent(this, GeoFeneceTransitionsIntentService.class);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    // --------------------- LIFECYCLE --------------------

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, new IntentFilter(Constatnts.BROADCAST_ACTION));
    }
}
