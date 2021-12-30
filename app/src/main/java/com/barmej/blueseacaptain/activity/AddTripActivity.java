package com.barmej.blueseacaptain.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.DatePicker;
import android.widget.Toast;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddTripBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle(R.string.add_trip);

        // التاكد من الحقول واضافه الرحله
        binding.addTripButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fromCountry = binding.fromCountryEditText.getText().toString();
                toCountry = binding.toCountryEditText.getText().toString();
                availableSeats = binding.availableSeatsEditText.getText().toString();

                if (TextUtils.isEmpty(fromCountry) || TextUtils.isEmpty(toCountry) ||
                        TextUtils.isEmpty(availableSeats)) {
                    Toast.makeText(AddTripActivity.this, R.string.fields_cannotbe_empty, Toast.LENGTH_SHORT).show();

                } else if (TextUtils.isEmpty(tripDate)) {
                    Toast.makeText(AddTripActivity.this, R.string.choose_trip_date, Toast.LENGTH_SHORT).show();

                } else {
                    hideForm(true);
                    ensureAndAddNewTrip();
                }
            }
        });

        // اضافه التاريخ من ال DatePickerFragment
        binding.datePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerFragment datePicker = new DatePickerFragment();
                datePicker.show(getSupportFragmentManager(), "datePecker");
            }
        });

    }

    //التاكد من ما اذا كان الكابتن لديه رحله في نفس الوقت او لا
    private void ensureAndAddNewTrip() {
        TripManager.getInstance().ensureAndAddNewTrip(this, tripDate, new AddNewTrip() {
            @Override
            public void addTrip(boolean add) {
                if (add) {
                    addNewTrip(getString(R.string.you_already_have_one_at_theSameDate), add);
                } else
                    addNewTrip(getString(R.string.add_new_trip), add);
            }
        });
    }

    // تاكيد اضافه الرحله
    private void addNewTrip(String status, boolean add) {
        hideForm(false);
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage(status);
        dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                hideForm(true);
                if (!add) {
                    TripManager.getInstance().addTrip(AddTripActivity.this, fromCountry, toCountry, availableSeats, tripDate, new CallBack() {
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
                hideForm(false);
            }
        });
        dialog.show();
    }

    // اخفاء العناصر
    private void hideForm(boolean b) {
        if (b) {
            binding.progressBar.setVisibility(View.VISIBLE);

            binding.fromCountryTextInputLayout.setVisibility(View.INVISIBLE);
            binding.toCountryTextInputLayout.setVisibility(View.INVISIBLE);
            binding.availableSeatsTextInputLayout.setVisibility(View.INVISIBLE);
            binding.datePicker.setVisibility(View.INVISIBLE);
            binding.addTripButton.setVisibility(View.INVISIBLE);
            binding.dateTextView.setVisibility(View.INVISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);

            binding.fromCountryTextInputLayout.setVisibility(View.VISIBLE);
            binding.toCountryTextInputLayout.setVisibility(View.VISIBLE);
            binding.availableSeatsTextInputLayout.setVisibility(View.VISIBLE);
            binding.datePicker.setVisibility(View.VISIBLE);
            binding.addTripButton.setVisibility(View.VISIBLE);
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
}