package com.barmej.blueseacaptain.domain;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.barmej.blueseacaptain.callback.AddNewTrip;
import com.barmej.blueseacaptain.callback.CallBack;
import com.barmej.blueseacaptain.data.SharedPreferencesHelper;
import com.barmej.blueseacaptain.domain.entity.Captain;
import com.barmej.blueseacaptain.domain.entity.Trip;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class TripManager {

    private static TripManager INSTANCE;
    private static final String TRIP_REF_PATH = "trips";
    private static final String CAPTAIN_REF_PATH = "captain";

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private Trip trip;
    private Captain captain;

    private TripManager() {
        firebaseDatabase = FirebaseDatabase.getInstance();
    }

    public static TripManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TripManager();
        }
        return INSTANCE;
    }


    public void ensureAndAddNewTrip(Context context, String tripDate, AddNewTrip addNewTrip) {
        // get the captain id  from SharedPreferences.
        String captainId = SharedPreferencesHelper.getCaptainId(context);

        firebaseDatabase.getReference(CAPTAIN_REF_PATH).child(captainId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                captain = snapshot.getValue(Captain.class);

                if (captain.getAssignedTrip().equals("")) {
                    addNewTrip.addTrip(false);

                } else {
                    firebaseDatabase.getReference(TRIP_REF_PATH).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            boolean add = false;
                            for (DataSnapshot s : snapshot.getChildren()) {
                                trip = s.getValue(Trip.class);

                                assert trip != null;
                                if (trip.getDate().equals(tripDate)) {
                                    addNewTrip.addTrip(true);
                                    add = true;
                                }
                            }
                            if (!add) {
                                addNewTrip.addTrip(false);
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void addTrip(Context context, String fromCountry, String toCountry, String availableSeats, String date, CallBack callBack) {
        String captainId = SharedPreferencesHelper.getCaptainId(context);
        String id = UUID.randomUUID().toString();

        trip = new Trip();
        trip.setId(id);
        trip.setCaptainId(captainId);
        trip.setFromCountry(fromCountry);
        trip.setToCountry(toCountry);
        trip.setAvailableSeats(Integer.parseInt(availableSeats));
        trip.setDate(date);
        trip.setStatus(Trip.Status.AVAILABLE.name());

        captain = new Captain();
        captain.setId(captainId);
        captain.setStatus(Captain.Status.AVAILABLE.name());
        captain.setAssignedTrip(id);
        firebaseDatabase.getReference(CAPTAIN_REF_PATH).child(captainId).setValue(captain);

        databaseReference = firebaseDatabase.getReference(TRIP_REF_PATH).child(id);
        databaseReference.setValue(trip).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                callBack.onComplete(task.isSuccessful());
            }
        });
    }

    public void updateCurrentLocation(double lat, double lng, Trip trip) {
        trip.setCurrentLat(lat);
        trip.setCurrentLng(lng);
        trip.setStatus(Trip.Status.ON_TRIP.name());
        firebaseDatabase.getReference(TRIP_REF_PATH).child(trip.getId()).setValue(trip);
        firebaseDatabase.getReference(CAPTAIN_REF_PATH).child(trip.getCaptainId()).child("status").setValue(Captain.Status.ON_TRIP.name());
        firebaseDatabase.getReference(CAPTAIN_REF_PATH).child(trip.getCaptainId()).child("assignedTrip").setValue(trip.getId());
    }

    public void updateTripToArrived(Trip trip) {
        trip.setStatus(Trip.Status.ARRIVED.name());
        firebaseDatabase.getReference(TRIP_REF_PATH).child(trip.getId()).setValue(trip);
        firebaseDatabase.getReference(CAPTAIN_REF_PATH).child(trip.getCaptainId()).child("status").setValue(Captain.Status.ARRIVED.name());
    }


}
