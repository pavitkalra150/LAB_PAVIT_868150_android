package com.example.lab_pavit_868150_android;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Geocoder geocoder;
    private MarkerOptions markerOptions;
    private static final int REQUEST_CODE = 1;
    private FusedLocationProviderClient fusedClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        geocoder = new Geocoder(this, Locale.getDefault());
        fusedClient = LocationServices.getFusedLocationProviderClient(this);
    }
    private GoogleMap.OnMapLongClickListener mMapLongClickListener = new GoogleMap.OnMapLongClickListener() {
        @Override
        public void onMapLongClick(LatLng latLng) {
            stopUpdateLocation();
            // Clearing all the markers from the Google Map
            mMap.clear();

            ArrayList<Marker> markers = new ArrayList<>();

// Add a marker to the map
            Marker marker = mMap.addMarker(new MarkerOptions().position(latLng));
            markers.add(marker);
            // Adding a new marker to the Google Map
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title("Loading address...");
            mMap.addMarker(markerOptions);
            Marker updatedMarker = markers.get(0);
            // Reverse geocoding to get the address for the given LatLng
            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                if (addresses.size() > 0) {
                    String address = addresses.get(0).getAddressLine(0);
                    // Update the title of the marker
                    updatedMarker.setTitle(address);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //mMap.getUiSettings().setZoomControlsEnabled(true);
        if (!hasLocationPermission())
            requestLocationPermission();
        else
            startUpdateLocation();
        mMap.setOnMapLongClickListener(mMapLongClickListener);
        mMap.getUiSettings().setZoomControlsEnabled(true);
    }
//    @Override
//    public void onMapLongClick(LatLng latLng) {
//        List<Address> addresses;
//        try {
//            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
//        } catch (IOException e) {
//            e.printStackTrace();
//            return;
//        }
//        if (addresses != null && addresses.size() > 0) {
//            String address = addresses.get(0).getAddressLine(0);
//            MarkerOptions marker = new MarkerOptions().position(latLng).title(address);
//            mMap.addMarker(marker);
//        }
//    }
    private void startUpdateLocation() {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                mMap.clear();
                if (locationResult != null) {
                    Location location = locationResult.getLastLocation();
                    LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(userLocation).title("your location!"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
    }

    private boolean hasLocationPermission() {
        return ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
    private void stopUpdateLocation() {
        if (fusedClient != null) {
            fusedClient.removeLocationUpdates(locationCallback);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setMessage("The permission is mandatory")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
                            }
                        }).create().show();
            } else
                startUpdateLocation();
        }
    }
}