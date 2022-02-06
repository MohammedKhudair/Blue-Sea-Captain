package com.barmej.blueseacaptain.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.DatePicker;
import android.widget.Toast;

import com.barmej.blueseacaptain.Constants;
import com.barmej.blueseacaptain.R;
import com.barmej.blueseacaptain.callback.AddNewTrip;
import com.barmej.blueseacaptain.callback.CallBack;
import com.barmej.blueseacaptain.databinding.ActivityAddTripBinding;
import com.barmej.blueseacaptain.domain.TripManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddTripActivity extends AppCompatActivity {
    static ActivityAddTripBinding binding;
    String fromCountry;
    String toCountry;
    String availableSeats;
    static String tripDate;

    private double pickUpLat = 0;
    private double pickUpLng = 0;
    private double destinationLat = 0;
    private double destinationLng = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddTripBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle(R.string.add_trip);

        // التاكد من الحقول واضافه الرحله
        binding.addTripButton.setOnClickListener(view -> {
            fromCountry = binding.fromCountryEditText.getText().toString();
            toCountry = binding.toCountryEditText.getText().toString();
            availableSeats = binding.availableSeatsEditText.getText().toString();

            if (TextUtils.isEmpty(fromCountry) || TextUtils.isEmpty(toCountry) ||
                    TextUtils.isEmpty(availableSeats)) {
                Toast.makeText(AddTripActivity.this, R.string.fields_cannotbe_empty, Toast.LENGTH_SHORT).show();

            } else if (TextUtils.isEmpty(tripDate)) {
                Toast.makeText(AddTripActivity.this, R.string.choose_trip_date, Toast.LENGTH_SHORT).show();

            } else if (pickUpLat == 0 || pickUpLng == 0 || destinationLat == 0 || destinationLng == 0) {
                Toast.makeText(AddTripActivity.this, R.string.choose_trip_location, Toast.LENGTH_SHORT).show();
            } else {
                hideForm(true);
                ensureAndAddNewTrip();
            }
        });

        // اضافه التاريخ من ال DatePickerFragment
        binding.datePicker.setOnClickListener(view -> {
            DatePickerFragment datePicker = new DatePickerFragment();
            datePicker.show(getSupportFragmentManager(), "datePecker");
        });

        binding.cardView.setOnClickListener(view -> {
            startActivity(TripDestinationActivity.getStartIntent(AddTripActivity.this));
            finish();
        });

        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            pickUpLat = intent.getDoubleExtra(Constants.PICKUP_EXTRA_LAT, 32.401734);
            pickUpLng = intent.getDoubleExtra(Constants.PICKUP_EXTRA_LNG, 32.401734);
            destinationLat = intent.getDoubleExtra(Constants.DESTINATION_EXTRA_LAT, 32.401734);
            destinationLng = intent.getDoubleExtra(Constants.DESTINATION_EXTRA_LNG, 32.401734);
            binding.textView1.setText(String.valueOf(pickUpLat + "," + pickUpLng));
            binding.textView2.setText(String.valueOf(destinationLat + "," + destinationLng));
        }

    }

    //التاكد من ما اذا كان الكابتن لديه رحله في نفس الوقت او لا
    private void ensureAndAddNewTrip() {
        TripManager.getInstance().ensureAndAddNewTrip(this, tripDate, new AddNewTrip() {
            @Override
            public void addTrip(boolean add) {
                if (add) {
                    tripConfirmationMessage(getString(R.string.you_already_have_one_at_theSameDate), true);
                } else
                    tripConfirmationMessage(getString(R.string.add_new_trip), false);
            }
        });
    }

    // تاكيد اضافه الرحله
    private void tripConfirmationMessage(String status, boolean add) {
        hideForm(false);
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage(status);
        dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                hideForm(true);
                if (!add) {
                    addNewTrip();
                }
                hideForm(false);
            }
        });
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        dialog.show();
    }

    private void addNewTrip() {
        TripManager.getInstance().addTrip(AddTripActivity.this,
                fromCountry, toCountry, availableSeats, tripDate,
                pickUpLat, pickUpLng, destinationLat, destinationLng, new CallBack() {
                    @Override
                    public void onComplete(boolean isSuccessful) {
                        if (isSuccessful) {
                            startActivity(MainActivity.getStartIntent(AddTripActivity.this));
                            Toast.makeText(AddTripActivity.this, "Data loaded", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            hideForm(false);
                            Toast.makeText(AddTripActivity.this, "Failed inserted data", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // اخفاء العناصر
    private void hideForm(boolean b) {
        if (b) {
            binding.progressBar.setVisibility(View.VISIBLE);

            binding.cardView2.setVisibility(View.INVISIBLE);
            binding.cardView.setVisibility(View.INVISIBLE);
            binding.datePicker.setVisibility(View.INVISIBLE);
            binding.dateTextView.setVisibility(View.INVISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);

            binding.cardView.setVisibility(View.VISIBLE);
            binding.cardView2.setVisibility(View.VISIBLE);
            binding.datePicker.setVisibility(View.VISIBLE);
            binding.dateTextView.setVisibility(View.VISIBLE);
        }
    }


    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, day);

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd LLLL yyyy", Locale.US);
            tripDate = simpleDateFormat.format(calendar.getTime());
            binding.dateTextView.setText(tripDate);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(MainActivity.getStartIntent(AddTripActivity.this));
    }
}