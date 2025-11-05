package com.example.appointementapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


// Register Activity - Handles user account creation with Firebase

public class Register extends AppCompatActivity {


    private EditText etEmail, etPassword, etConfirmPassword;
    private Button btnRegister, btnLogin;
    private ProgressBar progressBar;


    private FirebaseAuth mAuth;


    private static final int MIN_PASSWORD_LENGTH = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        mAuth = FirebaseAuth.getInstance();


        initializeViews();


        setupClickListeners();
    }


    private void initializeViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);
    }


    private void setupClickListeners() {
        // Register button click
        btnRegister.setOnClickListener(v -> registerUser());

        // Login button click - navigate back to Login activity
        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(Register.this, Login.class);
            startActivity(intent);
            finish();
        });
    }


     //Validate all inputs and create new user account with Firebase

    private void registerUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();


        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email address");
            etEmail.requestFocus();
            return;
        }


        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < MIN_PASSWORD_LENGTH) {
            etPassword.setError("Password must be at least " + MIN_PASSWORD_LENGTH + " characters");
            etPassword.requestFocus();
            return;
        }


        if (TextUtils.isEmpty(confirmPassword)) {
            etConfirmPassword.setError("Please confirm your password");
            etConfirmPassword.requestFocus();
            return;
        }


        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }


        progressBar.setVisibility(View.VISIBLE);
        btnRegister.setEnabled(false);


        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.GONE);
                    btnRegister.setEnabled(true);

                    if (task.isSuccessful()) {

                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(Register.this, "Registration successful! Please login.", Toast.LENGTH_SHORT).show();


                        Intent intent = new Intent(Register.this, Login.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {

                        Exception exception = task.getException();
                        String errorMessage = exception != null ? exception.getMessage() : "Unknown error";



                        if (errorMessage.contains("already in use")) {
                            Toast.makeText(Register.this, "This email is already registered. Please login or use another email.", Toast.LENGTH_LONG).show();
                        } else if (errorMessage.contains("weak password")) {
                            Toast.makeText(Register.this, "Password is too weak. Please use a stronger password.", Toast.LENGTH_LONG).show();
                        } else if (errorMessage.contains("network")) {
                            Toast.makeText(Register.this, "Network error. Please check your internet connection.", Toast.LENGTH_LONG).show();
                        } else if (errorMessage.contains("internal")) {
                            Toast.makeText(Register.this, "Firebase internal error. Check database rules in Firebase Console.", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(Register.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String email = currentUser.getEmail();
            if (email != null && email.equals("admin@vitaltech.com")) {
                startActivity(new Intent(Register.this, Admin.class));
            } else {
                startActivity(new Intent(Register.this, BookAppointment.class));
            }
            finish();
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(Register.this, Login.class);
        startActivity(intent);
        finish();
    }
}

