package com.barmej.blueseacaptain.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.barmej.blueseacaptain.Constants;
import com.barmej.blueseacaptain.R;
import com.barmej.blueseacaptain.databinding.ActivityTripDestinationBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

public class TripDestinationActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int PERMISSION_REQUEST_ACCESS_LOCATION = 1;
    private ActivityTripDestinationBinding binding;
    private GoogleMap mGoogleMap;
    private LatLng mPickUpLatLng;
    private LatLng mDestinationLatLng;
    private Marker mMarker;
    private int position = 1;
    private FusedLocationProviderClient fusedLocationProviderClient;

    public static Intent getStartIntent(Context context) {
        return new Intent(context, TripDestinationActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTripDestinationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        binding.tripMap.getMapAsync(this);
        binding.tripMap.onCreate(savedInstanceState);

        binding.actionButtonPickUp.setOnClickListener(view -> {
            if (mPickUpLatLng == null) {
                Toast.makeText(this, "حدد موقع البدايه من فضلك", Toast.LENGTH_SHORT).show();
            } else {
                position = 2;
                mMarker = null;
                binding.actionButtonPickUp.setVisibility(View.INVISIBLE);
                binding.actionButtonDestination.setVisibility(View.VISIBLE);
                binding.actionButtonDestination.setBackgroundColor(getResources().getColor(R.color.orange,getTheme()));
            }
        });

        binding.actionButtonDestination.setOnClickListener(view -> {
            if (mDestinationLatLng == null) {
                Toast.makeText(this, "حدد موقع النهايه من فضلك", Toast.LENGTH_SHORT).show();
            } else {
                sendData();
            }
        });

        binding.imageView.setOnClickListener(view -> {
            mGoogleMap.clear();
            mPickUpLatLng = null;
            mDestinationLatLng = null;
            mMarker = null;
            position = 1;
            binding.actionButtonPickUp.setVisibility(View.VISIBLE);
            binding.actionButtonDestination.setVisibility(View.INVISIBLE);
        });

    }

    private void sendData() {
        Intent intent = new Intent(TripDestinationActivity.this, AddTripActivity.class);
        intent.putExtra(Constants.PICKUP_EXTRA_LAT, mPickUpLatLng.latitude);
        intent.putExtra(Constants.PICKUP_EXTRA_LNG, mPickUpLatLng.longitude);
        intent.putExtra(Constants.DESTINATION_EXTRA_LAT, mDestinationLatLng.latitude);
        intent.putExtra(Constants.DESTINATION_EXTRA_LNG, mDestinationLatLng.longitude);
        startActivity(intent);
        finish();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mGoogleMap = googleMap;
        requestLocationPermission();
        setMyLocationEnabled();

        mGoogleMap.setOnMapClickListener(latLng -> {
            if (position == 1) {
                pickUpLocation(latLng);
            } else {
                destinationLocation(latLng);
            }
        });
    }

    private void pickUpLocation(LatLng latLng) {
        mPickUpLatLng = latLng;
        if (mMarker == null) {
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.pickup));
            mMarker = mGoogleMap.addMarker(markerOptions);
        } else {
            mMarker.setPosition(latLng);
        }
    }

    private void destinationLocation(LatLng latLng) {
        mDestinationLatLng = latLng;
        if (mMarker == null) {
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.destination));
            mMarker = mGoogleMap.addMarker(markerOptions);
        } else {
            mMarker.setPosition(latLng);
        }
    }

    //===========================================================

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            requestDeviceCurrentLocation();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_ACCESS_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_ACCESS_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestDeviceCurrentLocation();
            } else
                Toast.makeText(this, R.string.need_permission, Toast.LENGTH_LONG).show();
        }
    }

    @SuppressLint("MissingPermission")
    private void requestDeviceCurrentLocation() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                } else {
                    Toast.makeText(TripDestinationActivity.this, R.string.find_location_needed, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setMyLocationEnabled() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            mGoogleMap.setMyLocationEnabled(true);
        }
    }
    //=============================================================
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        binding.tripMap.onSaveInstanceState(outState);
    }

    @Override
    public void onStart() {
        super.onStart();
        binding.tripMap.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.tripMap.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        binding.tripMap.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        binding.tripMap.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding.tripMap.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, AddTripActivity.class));
        finish();
    }
}