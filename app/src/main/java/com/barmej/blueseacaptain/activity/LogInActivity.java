package com.barmej.blueseacaptain.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.barmej.blueseacaptain.R;
import com.barmej.blueseacaptain.data.SharedPreferencesHelper;
import com.barmej.blueseacaptain.databinding.ActivityLogInBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LogInActivity extends AppCompatActivity {

    ActivityLogInBinding binding;

    public static Intent getStartIntent(Context context) {
        return new Intent(context, LogInActivity.class);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLogInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.logInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logIn();
            }
        });


        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            SharedPreferencesHelper.setCaptainId(firebaseUser.getUid(), this);
            startActivity(MainActivity.getStartIntent(LogInActivity.this));
            finish();
        }
    }

    private void logIn() {
        String email = binding.emailEditText.getText().toString();
        String password = binding.passwordEditText.getText().toString();

        if (!isValidEmail(email)) {
            binding.emailTextInputLayout.setError(getString(R.string.invalid_email));
            return;
        }

        if (password.length() < 6) {
            binding.passwordTextInputLayout.setError(getString(R.string.invalid_password));
            return;
        }
        hideForm(true);

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).
                addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            String uid = task.getResult().getUser().getUid();
                            if (uid.equals(getString(R.string.uid))){
                                SharedPreferencesHelper.setCaptainId(uid, LogInActivity.this);
                                startActivity(MainActivity.getStartIntent(LogInActivity.this));
                                finish();
                            } else {
                                Snackbar.make(binding.getRoot(),R.string.captain_account,Snackbar.LENGTH_INDEFINITE).show();
                                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                                mAuth.signOut();
                                hideForm(false);
                            }

                        } else {
                            hideForm(false);
                            Toast.makeText(LogInActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    private boolean isValidEmail(CharSequence email) {
        return (!TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches());
    }

    private void hideForm(boolean hide) {
        if (hide) {
            binding.progressBar.setVisibility(View.VISIBLE);

            binding.emailTextInputLayout.setVisibility(View.INVISIBLE);
            binding.passwordTextInputLayout.setVisibility(View.INVISIBLE);
            binding.logInButton.setVisibility(View.INVISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);

            binding.emailTextInputLayout.setVisibility(View.VISIBLE);
            binding.passwordTextInputLayout.setVisibility(View.VISIBLE);
            binding.logInButton.setVisibility(View.VISIBLE);
        }
    }
}