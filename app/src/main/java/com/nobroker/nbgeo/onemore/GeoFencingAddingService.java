package com.nobroker.nbgeo.onemore;

import android.Manifest;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.nobroker.nbgeo.AndyConstants;
import com.nobroker.nbgeo.reciever.GeofenceBroadcastReceiver;
import com.nobroker.nbgeo.utilitis.GeofenceErrorMessages;

import java.util.ArrayList;


public class GeoFencingAddingService extends IntentService implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status> {
    private static final String TAG = "one_more_service";


    protected GoogleApiClient mGoogleApiClient;

    protected ArrayList<Geofence> mGeofenceList;

    private boolean mGeofencesAdded;

    private PendingIntent mGeofencePendingIntent;

    private SharedPreferences mSharedPreferences;

    public GeoFencingAddingService() {
        super(TAG);
        Log.d(TAG, "GeoFencingAddingService just got created");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "GeoFencingAddingService just got onHandleIntent");
        // Empty list for storing geofences.

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mGeofenceList = new ArrayList<Geofence>();

        // Initially set the PendingIntent used in addGeofences() and removeGeofences() to null.
        mGeofencePendingIntent = null;

        Bundle bundle = intent.getExtras();

        if (bundle == null)
            return;

        populateGeofenceList(bundle.getString("id"), bundle.getString("lat"), bundle.getString("lng"));
        buildGoogleApiClient();
    }


    private boolean checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            return true;
        } else
            return false;

    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mGoogleApiClient.connect();

    }


    @Override
    public void onConnected(Bundle connectionHint) {
        android.util.Log.i(TAG, "Connected to GoogleApiClient");

        addGeofencesButtonHandler();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        android.util.Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason.
        android.util.Log.i(TAG, "Connection suspended");

        // onConnected() will be called again automatically when the service reconnects
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();

        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);

        builder.addGeofences(mGeofenceList);

        return builder.build();
    }

    public void addGeofencesButtonHandler() {

        android.util.Log.v("deekshant", "addGeofencesButtonHandler is called");
        if (!mGoogleApiClient.isConnected()) {
            //    Toast.makeText(this, "not connected ", Toast.LENGTH_SHORT).show();
            return;
        }


        if (mGeofenceList != null && mGeofenceList.size() > 0 && checkPermission())
            try {
                LocationServices.GeofencingApi.addGeofences(
                        mGoogleApiClient,
                        getGeofencingRequest(),
                        getGeofencePendingIntent()
                ).setResultCallback(this); // Result processed in onResult().
            } catch (Exception e) {
                // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
            }
    }

    public void onResult(Status status) {


        android.util.Log.v("deekshant", "onResult status " + status);
        if (status.isSuccess()) {


        } else {
            // Get the status code for the error and log it using a user-friendly message.
            String errorMessage = GeofenceErrorMessages.getErrorString(this,
                    status.getStatusCode());
            android.util.Log.e(TAG, errorMessage);
        }
    }


    /**
     * Gets a PendingIntent to send with the request to add or remove Geofences. Location Services
     * issues the Intent inside this PendingIntent whenever a geofence transition occurs for the
     * current list of geofences.
     *
     * @return A PendingIntent for the IntentService that handles geofence transitions.
     */
    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        return PendingIntent.getBroadcast(this, 0, new Intent(this, GeofenceBroadcastReceiver.class), PendingIntent.FLAG_UPDATE_CURRENT);

    }

    /**
     * This sample hard codes geofence data. A real app might dynamically create geofences based on
     * the user's location.
     */
    public void populateGeofenceList(String id, String lat, String lng) {

        mGeofenceList.add(new Geofence.Builder()
                .setRequestId(id)

                .setCircularRegion(
                        Double.parseDouble(lat),
                        Double.parseDouble(lng),
                        AndyConstants.GEOFENCE_RADIUS_IN_METERS
                )

                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setLoiteringDelay(1000)

                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_DWELL |
                        Geofence.GEOFENCE_TRANSITION_EXIT | Geofence.GEOFENCE_TRANSITION_ENTER)

                .build());

    }

}
