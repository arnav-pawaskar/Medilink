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

/**
 * Register Activity - Handles user account creation with Firebase
 * Validates inputs and creates new user accounts
 */
public class Register extends AppCompatActivity {

    // UI Components
    private EditText etEmail, etPassword, etConfirmPassword;
    private Button btnRegister, btnLogin;
    private ProgressBar progressBar;

    // Firebase Authentication
    private FirebaseAuth mAuth;

    // Constants
    private static final int MIN_PASSWORD_LENGTH = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize UI components
        initializeViews();

        // Set click listeners
        setupClickListeners();
    }

    /**
     * Initialize all UI components by binding them from layout
     */
    private void initializeViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);
    }

    /**
     * Setup click listeners for buttons and links
     */
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

    /**
     * Validate all inputs and create new user account with Firebase
     */
    private void registerUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validate email
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

        // Validate password
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

        // Validate confirm password
        if (TextUtils.isEmpty(confirmPassword)) {
            etConfirmPassword.setError("Please confirm your password");
            etConfirmPassword.requestFocus();
            return;
        }

        // Check if passwords match
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        // Show progress bar and disable button
        progressBar.setVisibility(View.VISIBLE);
        btnRegister.setEnabled(false);

        // Create user with Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.GONE);
                    btnRegister.setEnabled(true);

                    if (task.isSuccessful()) {
                        // Registration successful
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(Register.this, "Registration successful! Please login.", Toast.LENGTH_SHORT).show();

                        // Navigate to Login screen
                        Intent intent = new Intent(Register.this, Login.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        // Registration failed - get detailed error
                        Exception exception = task.getException();
                        String errorMessage = exception != null ? exception.getMessage() : "Unknown error";


                        // Handle specific Firebase errors
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

    /**
     * Check if user is already logged in
     * If yes, redirect to appropriate screen
     */
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // User already logged in, redirect accordingly
            String email = currentUser.getEmail();
            if (email != null && email.equals("admin@vitaltech.com")) {
                startActivity(new Intent(Register.this, Admin.class));
            } else {
                startActivity(new Intent(Register.this, BookAppointment.class));
            }
            finish();
        }
    }

    /**
     * Handle back button press
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(Register.this, Login.class);
        startActivity(intent);
        finish();
    }
}

