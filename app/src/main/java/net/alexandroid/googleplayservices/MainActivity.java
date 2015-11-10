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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setViewsAndListeners();
        mBroadcastReceiver = new ActivityDetectionBroadcastReceiver();
        buildGoogleApiClient();
    }

    private void setViewsAndListeners() {
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

                break;
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

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

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


    @Override
    public void onConnected(Bundle bundle) {
        // Location part 1
        getLocationOnce();
        // Location part 2
        getLocationUpdates();
    }

    @Override
    public void onResult(Status status) {
        Log.i(TAG, "onResult: " + status.getStatusMessage());
        if (status.isSuccess()) {
            Log.e(TAG, "Success (added or removed activity detection)");
        } else {
            Log.e(TAG, "Not success (Error adding or removing activity detection)");
        }
    }

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
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "onConnectionFailed: " + connectionResult.toString());
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


    // MENU
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

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
}
