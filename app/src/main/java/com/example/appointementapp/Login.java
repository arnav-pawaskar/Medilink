package com.example.appointementapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


 // Handles user authentication with Firebase

public class Login extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin, btnRegister;
    private ProgressBar progressBar;


    private FirebaseAuth mAuth;


    private static final String ADMIN_EMAIL = "admin@gmail.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        mAuth = FirebaseAuth.getInstance();


        initializeViews();

        setupClickListeners();
    }


    private void initializeViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        progressBar = findViewById(R.id.progressBar);
    }


    private void setupClickListeners() {
        // Login button click
        btnLogin.setOnClickListener(v -> loginUser());

        // Register button click
        btnRegister.setOnClickListener(v -> {
            // Navigate to Register activity
            Intent intent = new Intent(Login.this, Register.class);
            startActivity(intent);
        });
    }


    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();


        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.GONE);
                    btnLogin.setEnabled(true);

                    if (task.isSuccessful()) {

                        Toast.makeText(Login.this, "Login successful!", Toast.LENGTH_SHORT).show();


                        if (email.equals(ADMIN_EMAIL)) {

                            Intent intent = new Intent(Login.this, Admin.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        } else {

                            Intent intent = new Intent(Login.this, BookAppointment.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                        finish();
                    } else {

                        Exception exception = task.getException();
                        String errorMessage = exception != null ? exception.getMessage() : "Unknown error";


                        if (errorMessage != null && errorMessage.contains("user not found")) {
                            Toast.makeText(Login.this, "User not found. Please register first.", Toast.LENGTH_LONG).show();
                        } else if (errorMessage != null && errorMessage.contains("password is invalid")) {
                            Toast.makeText(Login.this, "Invalid password. Please try again.", Toast.LENGTH_LONG).show();
                        } else if (errorMessage != null && errorMessage.contains("network")) {
                            Toast.makeText(Login.this, "Network error. Please check your internet connection.", Toast.LENGTH_LONG).show();
                        } else if (errorMessage != null && errorMessage.contains("internal")) {
                            Toast.makeText(Login.this, "Firebase internal error. Check database rules in Firebase Console.", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(Login.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
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
            if (email != null && email.equals(ADMIN_EMAIL)) {
                startActivity(new Intent(Login.this, Admin.class));
            } else {
                startActivity(new Intent(Login.this, BookAppointment.class));
            }
            finish();
        }
    }
}

