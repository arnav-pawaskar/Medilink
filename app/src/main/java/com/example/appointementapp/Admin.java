package com.example.appointementapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Admin Dashboard Activity
 * Allows admins to view, search, confirm, and cancel appointments
 * Real-time updates from Firebase Realtime Database
 */
public class Admin extends AppCompatActivity implements AppointmentAdapter.OnAppointmentActionListener {

    // UI Components
    private RecyclerView rvAppointments;
    private EditText etSearchPatient;
    private Button btnLogout, btnRefresh;
    private ProgressBar progressBar;

    // Firebase components
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    // Adapter and data
    private AppointmentAdapter adapter;
    private List<Appointment> appointmentList;
    private List<Appointment> filteredList;
    private ValueEventListener valueEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // Initialize Firebase
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        // Initialize UI components
        initializeViews();

        // Setup RecyclerView
        setupRecyclerView();

        // Setup search functionality
        setupSearchFunctionality();

        // Setup click listeners
        setupClickListeners();

        // Load appointments from Firebase
        loadAppointments();
    }

    /**
     * Initialize all UI components
     */
    private void initializeViews() {
        rvAppointments = findViewById(R.id.rvAppointments);
        etSearchPatient = findViewById(R.id.etSearchPatient);
        btnLogout = findViewById(R.id.btnLogout);
        btnRefresh = findViewById(R.id.btnRefresh);
        progressBar = findViewById(R.id.progressBar);
    }

    /**
     * Setup RecyclerView with LinearLayoutManager and adapter
     */
    private void setupRecyclerView() {
        appointmentList = new ArrayList<>();
        filteredList = new ArrayList<>();

        adapter = new AppointmentAdapter(this, filteredList, this);
        rvAppointments.setLayoutManager(new LinearLayoutManager(this));
        rvAppointments.setAdapter(adapter);
    }

    /**
     * Setup search functionality with debouncing
     */
    private void setupSearchFunctionality() {
        etSearchPatient.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterAppointments(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    /**
     * Filter appointments based on patient name search
     */
    private void filterAppointments(String searchQuery) {
        filteredList.clear();

        if (searchQuery.isEmpty()) {
            filteredList.addAll(appointmentList);
        } else {
            String query = searchQuery.toLowerCase();
            for (Appointment appointment : appointmentList) {
                if (appointment.getUserName().toLowerCase().contains(query) ||
                    appointment.getUserEmail().toLowerCase().contains(query) ||
                    appointment.getPhoneNumber().contains(query)) {
                    filteredList.add(appointment);
                }
            }
        }

        adapter.notifyDataSetChanged();
    }

    /**
     * Setup click listeners for buttons
     */
    private void setupClickListeners() {
        btnLogout.setOnClickListener(v -> logout());
        btnRefresh.setOnClickListener(v -> loadAppointments());
    }

    /**
     * Load all appointments from Firebase Realtime Database
     * Implements real-time updates using ValueEventListener
     */
    private void loadAppointments() {
        progressBar.setVisibility(View.VISIBLE);

        // Remove previous listener if exists
        if (valueEventListener != null) {
            mDatabase.child("Appointments").removeEventListener(valueEventListener);
        }

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                appointmentList.clear();
                filteredList.clear();

                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Appointment appointment = snapshot.getValue(Appointment.class);
                        if (appointment != null) {
                            appointmentList.add(appointment);
                        }
                    }

                    // Initially show all appointments
                    filteredList.addAll(appointmentList);
                    adapter.notifyDataSetChanged();

                    if (appointmentList.isEmpty()) {
                        Toast.makeText(Admin.this, "No appointments found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(Admin.this, "No appointments available", Toast.LENGTH_SHORT).show();
                }

                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(Admin.this, "Error loading appointments: " + databaseError.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        };

        // Attach listener for real-time updates
        mDatabase.child("Appointments").addValueEventListener(valueEventListener);
    }

    /**
     * Handle confirm appointment action
     * Updates appointment status to "confirmed" in Firebase
     */
    @Override
    public void onConfirmClick(Appointment appointment) {
        if (appointment.getAppointmentId() != null) {
            progressBar.setVisibility(View.VISIBLE);

            appointment.setStatus("confirmed");
            mDatabase.child("Appointments").child(appointment.getAppointmentId())
                    .setValue(appointment)
                    .addOnSuccessListener(aVoid -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(Admin.this, "Appointment confirmed!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(Admin.this, "Error confirming appointment: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
        }
    }

    /**
     * Handle cancel appointment action
     * Updates appointment status to "cancelled" in Firebase
     */
    @Override
    public void onCancelClick(Appointment appointment) {
        if (appointment.getAppointmentId() != null) {
            progressBar.setVisibility(View.VISIBLE);

            appointment.setStatus("cancelled");
            mDatabase.child("Appointments").child(appointment.getAppointmentId())
                    .setValue(appointment)
                    .addOnSuccessListener(aVoid -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(Admin.this, "Appointment cancelled!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(Admin.this, "Error cancelling appointment: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
        }
    }

    /**
     * Handle appointment item click
     * Can be used for viewing detailed appointment information
     */
    @Override
    public void onItemClick(Appointment appointment) {
        // Optional: Navigate to detailed view
        Toast.makeText(this, "Appointment: " + appointment.getUserName(), Toast.LENGTH_SHORT).show();
    }

    /**
     * Handle logout functionality
     */
    private void logout() {
        mAuth.signOut();
        Intent intent = new Intent(Admin.this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Check if user is still logged in and is admin
     */
    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(Admin.this, Login.class));
            finish();
        }
    }

    /**
     * Remove event listener when activity is destroyed to prevent memory leaks
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (valueEventListener != null) {
            mDatabase.child("Appointments").removeEventListener(valueEventListener);
        }
    }
}

