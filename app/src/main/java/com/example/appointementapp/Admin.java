package com.example.appointementapp;

import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
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

        // Setup swipe-to-delete for cancelled appointments
        setupSwipeToDelete();
    }

    /**
     * Setup swipe-to-delete functionality using ItemTouchHelper
     */
    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                                @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Appointment appointment = adapter.getItem(position);

                if (appointment != null && "cancelled".equalsIgnoreCase(appointment.getStatus())) {
                    // Show confirmation dialog before deleting
                    showDeleteConfirmationDialog(appointment, position);
                } else {
                    // Not a cancelled appointment, restore the item
                    adapter.notifyItemChanged(position);
                    Toast.makeText(Admin.this, "Only cancelled appointments can be deleted", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public int getSwipeDirs(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                int position = viewHolder.getAdapterPosition();
                Appointment appointment = adapter.getItem(position);

                // Only allow swipe for cancelled appointments
                if (appointment != null && "cancelled".equalsIgnoreCase(appointment.getStatus())) {
                    return super.getSwipeDirs(recyclerView, viewHolder);
                }
                return 0;
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                  int actionState, boolean isCurrentlyActive) {
                // Only show swipe if it's a cancelled appointment
                int position = viewHolder.getAdapterPosition();
                Appointment appointment = adapter.getItem(position);

                if (appointment != null && "cancelled".equalsIgnoreCase(appointment.getStatus())) {
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                }
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(rvAppointments);
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
     */
    @Override
    public void onItemClick(Appointment appointment) {
        // Optional: Navigate to detailed view
        Toast.makeText(this, "Appointment: " + appointment.getUserName(), Toast.LENGTH_SHORT).show();
    }

    /**
     * Handle delete appointment action
     */
    @Override
    public void onDeleteClick(Appointment appointment, int position) {
        showDeleteConfirmationDialog(appointment, position);
    }

    /**
     * Show confirmation dialog before deleting appointment
     */
    private void showDeleteConfirmationDialog(Appointment appointment, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Appointment")
                .setMessage("Are you sure you want to permanently delete this cancelled appointment for " +
                        appointment.getUserName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteAppointment(appointment, position);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // Restore the item if user cancels
                    adapter.notifyItemChanged(position);
                    dialog.dismiss();
                })
                .setCancelable(false)
                .show();
    }

    /**
     * Delete appointment from Firebase and update UI
     */
    private void deleteAppointment(Appointment appointment, int position) {
        if (appointment.getAppointmentId() == null) {
            Toast.makeText(this, "Error: Invalid appointment ID", Toast.LENGTH_SHORT).show();
            adapter.notifyItemChanged(position);
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mDatabase.child("Appointments").child(appointment.getAppointmentId())
                .removeValue()
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);

                    // Remove from both lists
                    appointmentList.remove(appointment);
                    filteredList.remove(appointment);

                    // Notify adapter
                    adapter.notifyItemRemoved(position);
                    adapter.notifyItemRangeChanged(position, filteredList.size());

                    Toast.makeText(Admin.this, "Appointment deleted successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    adapter.notifyItemChanged(position);
                    Toast.makeText(Admin.this, "Error deleting appointment: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
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


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (valueEventListener != null) {
            mDatabase.child("Appointments").removeEventListener(valueEventListener);
        }
    }
}
