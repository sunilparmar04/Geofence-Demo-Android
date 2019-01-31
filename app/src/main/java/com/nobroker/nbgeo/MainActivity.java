package com.nobroker.nbgeo;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements OnCompleteListener<Void>, OnMapReadyCallback {
    public static HashMap<String, LatLng> reminder_address = new HashMap<>();
    private ArrayList<Geofence> mGeofenceList = new ArrayList<>();
    private PendingIntent mGeofencePendingIntent;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private GeofencingClient mGeofencingClient;
    private GoogleMap googleMap;
    private CircleOptions mCircleOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGeofencePendingIntent = null;

        mGeofencingClient = LocationServices.getGeofencingClient(this);

        requestPermissions();

        findViewById(R.id.btnAddGeofence).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*if (!checkPermissions()) {
                    requestPermissions();
                } else {
                    addGeofences();
                }*/

                Intent intent = new Intent(MainActivity.this, AddGeoFenceActivity.class);
                startActivityForResult(intent, 124);
            }
        });


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        addLocationReminder();
    }

    private void addLocationReminder() {
        reminder_address.clear();
        SharedPreferences pre = getSharedPreferences(
                "source_destination",
                Context.MODE_PRIVATE);


        String latitude_source = pre.getString("latitude_source", "");
        String logtitude_source = pre.getString("logtitude_source", "");

        if (!TextUtils.isEmpty(latitude_source) && !TextUtils.isEmpty(logtitude_source)) {
            reminder_address.put("meeting_1", new LatLng(Double.valueOf(latitude_source), Double.valueOf(logtitude_source)));
        }

        if (reminder_address.size() > 0) {
            populateGeofenceList();

        }

    }


    private void populateGeofenceList() {
        mGeofenceList.clear();
        for (Map.Entry<String, LatLng> entry : reminder_address.entrySet()) {

            mGeofenceList.add(new Geofence.Builder()
                    // Set the request ID of the geofence. This is a string to identify this
                    // geofence.
                    .setRequestId(entry.getKey())

                    // Set the circular region of this geofence.
                    .setCircularRegion(
                            entry.getValue().latitude,
                            entry.getValue().longitude,
                            AndyConstants.GEOFENCE_RADIUS_IN_METERS
                    )

                    // Set the expiration duration of the geofence. This geofence gets automatically
                    // removed after this period of time.
                    .setExpirationDuration(AndyConstants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)

                    // Set the transition types of interest. Alerts are only generated for these
                    // transition. We track entry and exit transitions in this sample.
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                            Geofence.GEOFENCE_TRANSITION_EXIT)

                    // Create the geofence.
                    .build());
        }

        addGeofences();
    }


    @SuppressWarnings("MissingPermission")
    private void addGeofences() {
        if (!checkPermissions()) {

            Toast.makeText(this, R.string.insufficient_permissions, Toast.LENGTH_SHORT).show();
            return;
        }

        mGeofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                .addOnCompleteListener(this);
    }

    /**
     * Return the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }


    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        } else {

            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();

        // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
        // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
        // is already inside that geofence.
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);

        // Add the geofences to be monitored by geofencing service.
        builder.addGeofences(mGeofenceList);

        // Return a GeofencingRequest.
        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onComplete(@NonNull Task<Void> task) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        if (googleMap != null) {
            this.googleMap = googleMap;
            googleMap.setMyLocationEnabled(true);
            drawcircle();
        }


    }


    private void drawcircle() {
        googleMap.clear();
        for (Map.Entry entry : reminder_address.entrySet()) {
            Log.v("TrackReminderActivity", "reminder dtails==" + entry.getKey() + entry.getValue());

            mCircleOptions = new CircleOptions()
                    .center(reminder_address.get(entry.getKey())).radius(AndyConstants.GEOFENCE_RADIUS_IN_METERS).fillColor(0x40ff0000)
                    .strokeColor(Color.TRANSPARENT).strokeWidth(2);
            googleMap.addCircle(mCircleOptions);


            MarkerOptions mo = new MarkerOptions();
            mo.icon(BitmapDescriptorFactory
                    .fromResource(R.drawable.des_pin));
            mo.position(reminder_address.get(entry.getKey()));

            mo.anchor(0.5f, 0.5f);
            mo.flat(true);
            googleMap.addMarker(mo);

        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == 124) {

            addLocationReminder();
            drawcircle();


        }
    }
}
