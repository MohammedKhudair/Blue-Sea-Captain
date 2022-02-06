package com.barmej.blueseacaptain.fragment;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.barmej.blueseacaptain.TripAdapter;
import com.barmej.blueseacaptain.activity.AddTripActivity;
import com.barmej.blueseacaptain.activity.TripDetailsActivity;
import com.barmej.blueseacaptain.databinding.FragmentTripsListBinding;
import com.barmej.blueseacaptain.domain.entity.Trip;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class TripsListFragment extends Fragment implements TripAdapter.OnTripItemClickListener {
    private static final String TRIP_EXTRA = "TRIP_EXTRA";
    private FragmentTripsListBinding binding;
    FirebaseDatabase firebaseDatabase;
    private static String TRIP_REF_PATH = "trips";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentTripsListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.addTripButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), AddTripActivity.class));
                getActivity().onBackPressed();

            }
        });

        hideForm(true);
        ArrayList<Trip> tripArrayList = new ArrayList<Trip>();
        TripAdapter adapter = new TripAdapter(tripArrayList, this);
        binding.recyclerview.setAdapter(adapter);
        binding.recyclerview.setLayoutManager(new LinearLayoutManager(getContext()));

        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseDatabase.getReference(TRIP_REF_PATH).orderByChild("date")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot d : snapshot.getChildren()) {
                            tripArrayList.add(d.getValue(Trip.class));
                        }
                        adapter.notifyDataSetChanged();
                        hideForm(false);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void hideForm(boolean b) {
        if (b) {
            binding.tripsProgressBar.setVisibility(View.VISIBLE);
            binding.addTripButton.setVisibility(View.INVISIBLE);

        } else {
            binding.tripsProgressBar.setVisibility(View.INVISIBLE);
            binding.addTripButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onTripItemClicked(Trip trip) {
        Intent intent = new Intent(getActivity(), TripDetailsActivity.class);
        intent.putExtra(TRIP_EXTRA, trip);
        startActivity(intent);
    }
}
