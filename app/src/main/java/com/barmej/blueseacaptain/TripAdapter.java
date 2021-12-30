package com.barmej.blueseacaptain;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.barmej.blueseacaptain.databinding.ItemTripBinding;
import com.barmej.blueseacaptain.domain.entity.Trip;

import java.util.ArrayList;

public class TripAdapter extends RecyclerView.Adapter<TripAdapter.TripViewHolder> {

    private ArrayList<Trip> tripArrayList = new ArrayList<Trip>();
    private OnTripItemClickListener itemClickListener;

    public TripAdapter(ArrayList<Trip> tripArrayList, OnTripItemClickListener itemClickListener) {
        this.tripArrayList = tripArrayList;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public TripAdapter.TripViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTripBinding binding = ItemTripBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new TripViewHolder(binding, itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull TripAdapter.TripViewHolder holder, int position) {
        Trip trip = tripArrayList.get(position);
        holder.bind(trip);
    }

    @Override
    public int getItemCount() {
        if (tripArrayList != null) {
            return tripArrayList.size();
        } else
            return 0;
    }

    public static class TripViewHolder extends RecyclerView.ViewHolder {
        private ItemTripBinding binding;
        private Trip trip;

        public TripViewHolder(@NonNull ItemTripBinding binding, OnTripItemClickListener itemClickListener) {
            super(binding.getRoot());
            this.binding = binding;

            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    itemClickListener.onTripItemClicked(trip);
                }
            });
        }

        void bind(Trip trip) {
            this.trip = trip;
            binding.dateTextView.setText(trip.getDate());
            binding.fromCountryTextView.setText(trip.getFromCountry());
            binding.toCountryTextView.setText(trip.getToCountry());
            binding.availableSeatsTextView.setText(String.valueOf(trip.getAvailableSeats()));
            binding.reservedSeatsTextView.setText(String.valueOf(trip.getReservedSeats()));
        }

    }

    public interface OnTripItemClickListener {
        void onTripItemClicked(Trip trip);
    }
}
