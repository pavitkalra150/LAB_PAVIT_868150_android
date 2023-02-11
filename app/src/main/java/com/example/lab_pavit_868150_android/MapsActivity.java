package com.example.lab_pavit_868150_android;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Geocoder geocoder;
    //private MarkerOptions markerOptions;
    private static final int REQUEST_CODE = 1;
    private FusedLocationProviderClient fusedClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private ArrayList<String> addressesList = new ArrayList<>();
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
//        getActionBar().setDisplayHomeAsUpEnabled(true);
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
                        String markerAddress = addresses.get(0).getAddressLine(0);
                        // Update the title of the marker
                        updatedMarker.setTitle(markerAddress);
                        addressesList.add(markerAddress);
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("markerAddress", markerAddress);
                        setResult(RESULT_OK, resultIntent);
//                        finish();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //mMap.getUiSettings().setZoomControlsEnabled(true);
        if (!hasLocationPermission())
            requestLocationPermission();
        else
            startUpdateLocation();
        mMap.setOnMapLongClickListener(mMapLongClickListener);
        // Adding a default marker at the user's current location
        Location location = getLastKnownLocation();
        if (location != null) {
            LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.addMarker(new MarkerOptions().position(userLocation).title("You're here"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
        }
    }
    private Location getLastKnownLocation() {
        final Location[] location = {null};
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        fusedClient.getLastLocation().addOnSuccessListener(loc -> {
            if (loc != null) {
                LatLng latLng = new LatLng(loc.getLatitude(), loc.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                location[0] = loc;
            }
        });
        return location[0];
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
            locationRequest.setInterval(2000);
            locationRequest.setFastestInterval(1000);

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

//        @Override
//        public boolean onOptionsItemSelected(MenuItem item) {
//            switch (item.getItemId()) {
//                case android.R.id.home:
//                    Intent intent = new Intent(this, LocationListActivity.class);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                    startActivity(intent);
//                    return true;
//            }
//            return super.onOptionsItemSelected(item);
//        }

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