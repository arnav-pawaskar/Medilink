package com.example.appointementapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// Book Appointment Activity - Handles comprehensive appointment booking form

public class BookAppointment extends AppCompatActivity {

    private EditText etPatientName, etEmail, etPhoneNumber, etBloodGroup;
    private EditText etPastProblems, etFamilyHistory, etCurrentProblem;
    private Button btnSelectDate, btnSelectTime, btnSubmitAppointment, btnLogout;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;

    private String selectedDate = "";
    private String selectedTime = "";
    private Calendar calendar;

    private static final int MIN_PHONE_LENGTH = 10;
    private static final int MAX_PHONE_LENGTH = 15;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_appointment);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        currentUser = mAuth.getCurrentUser();
        calendar = Calendar.getInstance();

        initializeViews();

        setupClickListeners();


        if (currentUser != null && currentUser.getEmail() != null) {
            etEmail.setText(currentUser.getEmail());
        }
    }


    private void initializeViews() {
        etPatientName = findViewById(R.id.etPatientName);
        etEmail = findViewById(R.id.etEmail);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etBloodGroup = findViewById(R.id.etBloodGroup);
        etPastProblems = findViewById(R.id.etPastProblems);
        etFamilyHistory = findViewById(R.id.etFamilyHistory);
        etCurrentProblem = findViewById(R.id.etCurrentProblem);
        btnSelectDate = findViewById(R.id.btnSelectDate);
        btnSelectTime = findViewById(R.id.btnSelectTime);
        btnSubmitAppointment = findViewById(R.id.btnSubmitAppointment);
        btnLogout = findViewById(R.id.btnLogout);
        progressBar = findViewById(R.id.progressBar);
    }


    private void setupClickListeners() {
        btnSelectDate.setOnClickListener(v -> showDatePickerDialog());
        btnSelectTime.setOnClickListener(v -> showTimePickerDialog());
        btnSubmitAppointment.setOnClickListener(v -> submitAppointment());
        btnLogout.setOnClickListener(v -> logout());
    }

    // Display DatePickerDialog to select appointment date

    private void showDatePickerDialog() {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                BookAppointment.this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // selectedMonth is 0-indexed, so add 1
                    selectedDate = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear);
                    btnSelectDate.setText("Date: " + selectedDate);
                    btnSelectDate.setTextColor(getResources().getColor(android.R.color.black));
                },
                year, month, day
        );

        // Set minimum date to today
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    //Display TimePickerDialog to select appointment time

    private void showTimePickerDialog() {
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                BookAppointment.this,
                (view, selectedHour, selectedMinute) -> {
                    selectedTime = String.format("%02d:%02d", selectedHour, selectedMinute);
                    btnSelectTime.setText("Time: " + selectedTime);
                    btnSelectTime.setTextColor(getResources().getColor(android.R.color.black));
                },
                hour, minute, true
        );

        timePickerDialog.show();
    }

    //Validate all form inputs before submission

    private boolean validateInputs() {
        String patientName = etPatientName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phoneNumber = etPhoneNumber.getText().toString().trim();
        String bloodGroup = etBloodGroup.getText().toString().trim();
        String pastProblems = etPastProblems.getText().toString().trim();
        String familyHistory = etFamilyHistory.getText().toString().trim();
        String currentProblem = etCurrentProblem.getText().toString().trim();


        if (TextUtils.isEmpty(patientName)) {
            etPatientName.setError("Patient name is required");
            etPatientName.requestFocus();
            return false;
        }


        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email address");
            etEmail.requestFocus();
            return false;
        }


        if (TextUtils.isEmpty(phoneNumber)) {
            etPhoneNumber.setError("Phone number is required");
            etPhoneNumber.requestFocus();
            return false;
        }

        if (phoneNumber.length() < MIN_PHONE_LENGTH || phoneNumber.length() > MAX_PHONE_LENGTH) {
            etPhoneNumber.setError("Phone number must be between " + MIN_PHONE_LENGTH + " and " + MAX_PHONE_LENGTH + " digits");
            etPhoneNumber.requestFocus();
            return false;
        }

        if (!phoneNumber.matches("\\d+")) {
            etPhoneNumber.setError("Phone number must contain only digits");
            etPhoneNumber.requestFocus();
            return false;
        }


        if (TextUtils.isEmpty(bloodGroup)) {
            etBloodGroup.setError("Blood group is required");
            etBloodGroup.requestFocus();
            return false;
        }


        if (TextUtils.isEmpty(currentProblem)) {
            etCurrentProblem.setError("Please describe your current problem");
            etCurrentProblem.requestFocus();
            return false;
        }


        if (TextUtils.isEmpty(selectedDate)) {
            Toast.makeText(this, "Please select an appointment date", Toast.LENGTH_SHORT).show();
            return false;
        }


        if (TextUtils.isEmpty(selectedTime)) {
            Toast.makeText(this, "Please select an appointment time", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    // Submit appointment to Firebase Realtime Database

    private void submitAppointment() {
        // Validate all inputs first
        if (!validateInputs()) {
            return;
        }

        // Show progress bar and disable submit button
        progressBar.setVisibility(View.VISIBLE);
        btnSubmitAppointment.setEnabled(false);

        // Create unique appointment ID
        String appointmentId = UUID.randomUUID().toString();

        // Prepare appointment data
        String patientName = etPatientName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phoneNumber = etPhoneNumber.getText().toString().trim();
        String bloodGroup = etBloodGroup.getText().toString().trim();
        String pastProblems = etPastProblems.getText().toString().trim();
        String familyHistory = etFamilyHistory.getText().toString().trim();
        String currentProblem = etCurrentProblem.getText().toString().trim();

        // Create appointment map
        Map<String, Object> appointmentData = new HashMap<>();
        appointmentData.put("appointmentId", appointmentId);
        appointmentData.put("userName", patientName);
        appointmentData.put("userEmail", email);
        appointmentData.put("phoneNumber", phoneNumber);
        appointmentData.put("bloodGroup", bloodGroup);
        appointmentData.put("pastProblems", pastProblems);
        appointmentData.put("familyMedicalHistory", familyHistory);
        appointmentData.put("problemDescription", currentProblem);
        appointmentData.put("date", selectedDate);
        appointmentData.put("time", selectedTime);
        appointmentData.put("userId", currentUser.getUid());
        appointmentData.put("status", "pending"); // New appointments start as pending

        // Save to Firebase Realtime Database
        mDatabase.child("Appointments").child(appointmentId).setValue(appointmentData)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    btnSubmitAppointment.setEnabled(true);

                    // Show success message
                    Toast.makeText(BookAppointment.this, "Appointment booked successfully!", Toast.LENGTH_SHORT).show();

                    // Navigate to confirmation screen with appointment ID
                    Intent intent = new Intent(BookAppointment.this, AppointmentConfirm.class);
                    intent.putExtra("appointmentId", appointmentId);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnSubmitAppointment.setEnabled(true);

                    // Show error message
                    String errorMessage = "Error: " + (e.getMessage() != null ? e.getMessage() : "Failed to book appointment");
                    Toast.makeText(BookAppointment.this, errorMessage, Toast.LENGTH_LONG).show();
                });
    }

    /**
     * Handle logout functionality
     */
    private void logout() {
        mAuth.signOut();
        Intent intent = new Intent(BookAppointment.this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Check if user is still logged in
     */
    @Override
    protected void onStart() {
        super.onStart();
        if (currentUser == null) {
            startActivity(new Intent(BookAppointment.this, Login.class));
            finish();
        }
    }

    /**
     * Handle back button press
     */
    @Override
    public void onBackPressed() {
        // Allow user to go back to previous activity
        super.onBackPressed();
    }
}

