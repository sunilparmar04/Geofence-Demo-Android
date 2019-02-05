package com.nobroker.nbgeo;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class AddGeoFenceActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {
    GoogleMap googlemap;
    private LatLng myLocation;
    TextView tv_Address;
    private Address address;
    public static String strAddress = null, s_address;
    private LocationManager locationManager;
    private static final long MIN_TIME = 400;
    private static final float MIN_DISTANCE = 1000;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 35;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_geofence);
        tv_Address = (TextView) findViewById(R.id.tv_Address);


        if (Util.isPermissionRequired(this)) {
            Util.requestPermission(this, REQUEST_PERMISSIONS_REQUEST_CODE);
        } else {
            initMap();
        }


        findViewById(R.id.tv_set_reminder).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_OK);
                finish();
            }
        });

    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this); //You can also use LocationManager.GPS_PROVIDER and LocationManager.PASSIVE_PROVIDER
        } else {
            Util.requestPermission(this, REQUEST_PERMISSIONS_REQUEST_CODE);
        }


    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        if (googleMap != null) {


            this.googlemap = googleMap;
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                googleMap.setMyLocationEnabled(true);
            } else {
                // Show rationale and request permission.
            }

            googleMap.getUiSettings().setMyLocationButtonEnabled(true);
            Log.v("onActivityResult", " nullllllllll--" + myLocation);

            googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                @Override
                public void onCameraIdle() {


                    getAddressGEOCODE(googleMap.getCameraPosition().target);

                }
            });

        }


    }

    public void getAddressGEOCODE(final LatLng currenLatLng) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                Geocoder gCoder = new Geocoder(AddGeoFenceActivity.this);
                try {
                    final List<Address> list = gCoder.getFromLocation(
                            currenLatLng.latitude, currenLatLng.longitude, 1);
                    if (list != null && list.size() > 0) {
                        address = list.get(0);
                        StringBuilder sb = new StringBuilder();
                        if (address.getAddressLine(0) != null) {
                            if (address.getMaxAddressLineIndex() > 0) {
                                for (int i = 0; i < address
                                        .getMaxAddressLineIndex(); i++) {
                                    sb.append(address.getAddressLine(i))
                                            .append("\n");
                                }
                                sb.append(",");
                                sb.append(address.getCountryName());
                            } else {
                                sb.append(address.getAddressLine(0));
                            }
                        }

                        strAddress = sb.toString();
                        strAddress = strAddress.replace(",null", "");
                        strAddress = strAddress.replace("null", "");
                        strAddress = strAddress.replace("Unnamed", "");


                    }
                    Log.v("location_add", "strAddress:" + strAddress);

                    if (list.size() < 1) {


                    } else {


                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (!TextUtils.isEmpty(strAddress)) {
                                    tv_Address.setFocusable(false);
                                    tv_Address.setFocusableInTouchMode(false);
                                    tv_Address.setText(strAddress);

                                    tv_Address.setText(strAddress);

                                    myLocation = currenLatLng;

                                    s_address = strAddress;


                                    SharedPreferences pre = getSharedPreferences(
                                            "source_destination",
                                            Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = pre
                                            .edit();
                                    editor.putString("event_address",
                                            strAddress);
                                    editor.putString("latitude_source",
                                            String.valueOf(currenLatLng.latitude));
                                    editor.putString("logtitude_source",
                                            String.valueOf(currenLatLng.longitude));
                                    editor.commit();

                                } else {

                                }

                            }
                        });

                    }


                } catch (Exception exc) {
                    exc.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null & googlemap != null) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
            googlemap.animateCamera(cameraUpdate);
            locationManager.removeUpdates(this);
        }
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


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (!Util.isPermissionRequired(AddGeoFenceActivity.this)) {
                initMap();
            }
        }

    }

}
