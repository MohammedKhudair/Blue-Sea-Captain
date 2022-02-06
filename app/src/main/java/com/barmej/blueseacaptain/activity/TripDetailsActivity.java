package com.barmej.blueseacaptain.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import com.barmej.blueseacaptain.R;
import com.barmej.blueseacaptain.callback.TripUpdates;
import com.barmej.blueseacaptain.databinding.ActivityTripDetailsBinding;
import com.barmej.blueseacaptain.domain.TripManager;
import com.barmej.blueseacaptain.domain.entity.Trip;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class TripDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {
    private ActivityTripDetailsBinding binding;
    private static String TRIP_REF_PATH = "trips";
    private static final String TRIP_EXTRA = "TRIP_EXTRA";
    private static final int PERMISSION_REQUEST_ACCESS_LOCATION = 1;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private LocationRequest mLocationRequest;
    private Marker marker;

    Trip trip;
    private GoogleMap mGoogleMap;
    private boolean mLocationPermissionGranted;
    private FirebaseDatabase firebaseDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTripDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle(R.string.details);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        binding.tripMapView.onCreate(savedInstanceState);
        binding.tripMapView.getMapAsync(this);


        // زر بدء الرحله وارسال تحديثات الموقع
        binding.startTripButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View view) {
                // التاكد من الحصول على بيانات الموقع
                fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(TripDetailsActivity.this);
                fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(@NonNull Location location) {
                        if (location == null) {
                            Snackbar.make(binding.getRoot(), R.string.failed_accesslocation, Snackbar.LENGTH_LONG).show();
                        } else {
                            startLocationUpdates();
                        }
                    }
                });
            }
        });

        // زر ايقاف الرحله
        binding.arrivedTripButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tripArrived();
            }
        });

        // الحصول على بيانات الرحله من intent
        Intent intent = getIntent();
        if (intent != null) {
            trip = (Trip) intent.getSerializableExtra(TRIP_EXTRA);

            firebaseDatabase = FirebaseDatabase.getInstance();
            firebaseDatabase.getReference(TRIP_REF_PATH).child(trip.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    trip = snapshot.getValue(Trip.class);
                    binding.dateTextView.setText(trip.getDate());
                    binding.fromCountryTextView.setText(trip.getFromCountry());
                    binding.toCountryTextView.setText(trip.getToCountry());
                    binding.availableSeatsTextView.setText(String.valueOf(trip.getAvailableSeats()));
                    binding.reservedSeatsTextView.setText(String.valueOf(trip.getReservedSeats()));

                    if (trip.getStatus().equals(Trip.Status.ON_TRIP.name())) {
                        startLocationUpdates();

                    } else if (trip.getStatus().equals(Trip.Status.ARRIVED.name())) {
                        binding.startTripButton.setEnabled(false);
                        binding.startTripButton.setText("انتهت الرحلة");

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        // الاستماع الي تحديثات الرحله
        listenToTripUpdates();
    }

    // الاستماع الي تحديثات الرحله و تحديث المقاعد المتاحه و المحجوزه
    private void listenToTripUpdates() {
        TripManager.getInstance().listenToTripUpdates(trip, trip -> {
            binding.availableSeatsTextView.setText(String.valueOf(trip.getAvailableSeats()));
            binding.reservedSeatsTextView.setText(String.valueOf(trip.getReservedSeats()));
        });
    }

    // تحديث الرحله وايقاف المستمع
    private void tripArrived() {
        stopLocationUpdates();
        TripManager.getInstance().updateTripToArrived(trip);
        binding.startTripButton.setVisibility(View.VISIBLE);
        binding.arrivedTripButton.setVisibility(View.INVISIBLE);
        binding.startTripButton.setEnabled(false);
        binding.startTripButton.setText("انتهت الرحلة");
    }

    //ارسال تحديثات الموقع
    protected void startLocationUpdates() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        setMyLocationEnabled(false);
        setMarkersLocation();

        // Create the location request to start receiving updates
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(5 * 1000);
        mLocationRequest.setFastestInterval(2000);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                onLocationChanged(locationResult.getLastLocation());
            }
        };

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, locationCallback, Looper.getMainLooper());
        }
    }

    //استلام تحديثات الموقع من ال locationCallback وتحديث الرحله
    private void onLocationChanged(Location lastLocation) {
        LatLng latLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
        binding.startTripButton.setVisibility(View.INVISIBLE);
        binding.arrivedTripButton.setVisibility(View.VISIBLE);

        if (marker == null) {
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
            BitmapDescriptor descriptor = BitmapDescriptorFactory.fromResource(R.drawable.sailingboat3);
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.icon(descriptor);
            markerOptions.title("Boat location");
            marker = mGoogleMap.addMarker(markerOptions);
        } else {
            marker.setPosition(latLng);
        }

        TripManager.getInstance().updateCurrentLocation(lastLocation.getLatitude(), lastLocation.getLongitude(), trip);
    }

    //وضع موقع علامة البدايه والنهايه
    private void setMarkersLocation() {
        LatLng pickUpLatLng = new LatLng(trip.getPickUpLat(), trip.getPickUpLng());
        LatLng destinationLatLng = new LatLng(trip.getDestinationLat(), trip.getDestinationLng());

        BitmapDescriptor descriptor1 = BitmapDescriptorFactory.fromResource(R.drawable.pickup);
        MarkerOptions markerOptions1 = new MarkerOptions();
        markerOptions1.position(pickUpLatLng);
        markerOptions1.icon(descriptor1);
        mGoogleMap.addMarker(markerOptions1);

        BitmapDescriptor descriptor2 = BitmapDescriptorFactory.fromResource(R.drawable.destination);
        MarkerOptions markerOptions2 = new MarkerOptions();
        markerOptions2.position(destinationLatLng);
        markerOptions2.icon(descriptor2);
        mGoogleMap.addMarker(markerOptions2);
    }

    //======================================================================
    //طلب صلاحيه الموقع
    private void requestLocationPermission() {
        mLocationPermissionGranted = false;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
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
                mLocationPermissionGranted = true;
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
                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));
                    setMyLocationEnabled(true);
                } else {
                    Toast.makeText(TripDetailsActivity.this, R.string.find_location_needed, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //========================================================================
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mGoogleMap = googleMap;
        requestLocationPermission();
        requestDeviceCurrentLocation();
        setMyLocationEnabled(true);
    }

    private void setMyLocationEnabled(boolean enabled) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            mGoogleMap.setMyLocationEnabled(enabled);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        binding.tripMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onStart() {
        super.onStart();
        binding.tripMapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.tripMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        binding.tripMapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        binding.tripMapView.onStop();
        stopLocationUpdates();
        TripManager.getInstance().stopListeningToTripUpdates(trip.getId());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding.tripMapView.onDestroy();
        stopLocationUpdates();
    }

    private void stopLocationUpdates() {
        if (fusedLocationProviderClient != null && locationCallback != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
            locationCallback = null;
            mLocationRequest = null;
            fusedLocationProviderClient = null;
        }
    }

}