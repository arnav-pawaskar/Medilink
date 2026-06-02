package com.example.appointementapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;


public class Admin extends AppCompatActivity implements AppointmentAdapter.OnAppointmentActionListener {


    private RecyclerView rvAppointments;
    private EditText etSearchPatient;
    private Button btnLogout;
    private FloatingActionButton btnRefresh;
    private ProgressBar progressBar;


    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;


    private AppointmentAdapter adapter;
    private List<Appointment> appointmentList;
    private List<Appointment> filteredList;
    private ValueEventListener valueEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);


        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();


        initializeViews();


        setupRecyclerView();


        setupSearchFunctionality();


        setupClickListeners();


        loadAppointments();
    }


    private void initializeViews() {
        rvAppointments = findViewById(R.id.rvAppointments);
        etSearchPatient = findViewById(R.id.etSearchPatient);
        btnLogout = findViewById(R.id.btnLogout);
        btnRefresh = findViewById(R.id.btnRefresh);
        progressBar = findViewById(R.id.progressBar);
    }


    private void setupRecyclerView() {
        appointmentList = new ArrayList<>();
        filteredList = new ArrayList<>();

        adapter = new AppointmentAdapter(this, filteredList, this);
        rvAppointments.setLayoutManager(new LinearLayoutManager(this));
        rvAppointments.setAdapter(adapter);


        setupSwipeToDelete();
    }


    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                                @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();
                Appointment appointment = adapter.getItem(position);

                if (appointment != null && "cancelled".equalsIgnoreCase(appointment.getStatus())) {

                    showDeleteConfirmationDialog(appointment, position);
                } else {

                    adapter.notifyItemChanged(position);
                    Toast.makeText(Admin.this, "Only cancelled appointments can be deleted", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public int getSwipeDirs(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                int position = viewHolder.getBindingAdapterPosition();
                Appointment appointment = adapter.getItem(position);


                if (appointment != null && "cancelled".equalsIgnoreCase(appointment.getStatus())) {
                    return super.getSwipeDirs(recyclerView, viewHolder);
                }
                return 0;
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                  int actionState, boolean isCurrentlyActive) {

                int position = viewHolder.getBindingAdapterPosition();
                Appointment appointment = adapter.getItem(position);

                if (appointment != null && "cancelled".equalsIgnoreCase(appointment.getStatus())) {
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                }
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(rvAppointments);
    }


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


    private void setupClickListeners() {
        btnLogout.setOnClickListener(v -> logout());
        btnRefresh.setOnClickListener(v -> loadAppointments());
    }

    private void loadAppointments() {
        progressBar.setVisibility(View.VISIBLE);


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

                    Collections.sort(appointmentList, (a1, a2) -> {
                        int priority1 = getStatusPriority(a1.getStatus());
                        int priority2 = getStatusPriority(a2.getStatus());
                        return Integer.compare(priority1, priority2);
                    });

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


        mDatabase.child("Appointments").addValueEventListener(valueEventListener);
    }


    private int getStatusPriority(String status) {
        if (status == null || "pending".equalsIgnoreCase(status)) {
            return 1;
        } else if ("confirmed".equalsIgnoreCase(status)) {
            return 2;
        } else {
            return 3;
        }
    }



    @Override
    public void onConfirmClick(Appointment appointment) {
        if (appointment.getAppointmentId() != null) {
            progressBar.setVisibility(View.VISIBLE);

            // Update status in Firebase using child().setValue() for specific field
            mDatabase.child("Appointments").child(appointment.getAppointmentId())
                    .child("status")
                    .setValue("confirmed")
                    .addOnSuccessListener(aVoid -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(Admin.this, "Appointment confirmed!", Toast.LENGTH_SHORT).show();
                        // No need to manually update - ValueEventListener will handle it
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(Admin.this, "Error confirming appointment: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
        }
    }


    @Override
    public void onCancelClick(Appointment appointment) {
        if (appointment.getAppointmentId() != null) {
            progressBar.setVisibility(View.VISIBLE);

            // Update status in Firebase using child().setValue() for specific field
            mDatabase.child("Appointments").child(appointment.getAppointmentId())
                    .child("status")
                    .setValue("cancelled")
                    .addOnSuccessListener(aVoid -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(Admin.this, "Appointment cancelled!", Toast.LENGTH_SHORT).show();
                        // No need to manually update - ValueEventListener will handle it
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(Admin.this, "Error cancelling appointment: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
        }
    }


    @Override
    public void onItemClick(Appointment appointment) {
        // Optional: Navigate to detailed view
        Toast.makeText(this, "Appointment: " + appointment.getUserName(), Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onDeleteClick(Appointment appointment, int position) {
        showDeleteConfirmationDialog(appointment, position);
    }

    @Override
    public void onRescheduleClick(Appointment appointment) {
        showRescheduleDialog(appointment);
    }


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


                    appointmentList.remove(appointment);
                    filteredList.remove(appointment);


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


    private void logout() {
        mAuth.signOut();
        Intent intent = new Intent(Admin.this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showRescheduleDialog(Appointment appointment) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_reschedule_appointment, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        TextView tvPatientInfo = dialogView.findViewById(R.id.tvPatientInfo);
        TextView tvCurrentDateTime = dialogView.findViewById(R.id.tvCurrentDateTime);
        Button btnSelectNewDate = dialogView.findViewById(R.id.btnSelectNewDate);
        Button btnSelectNewTime = dialogView.findViewById(R.id.btnSelectNewTime);
        Button btnCancelReschedule = dialogView.findViewById(R.id.btnCancelReschedule);
        Button btnConfirmReschedule = dialogView.findViewById(R.id.btnConfirmReschedule);

        tvPatientInfo.setText("Patient: " + appointment.getUserName());
        tvCurrentDateTime.setText("Current: " + appointment.getDate() + " | " + appointment.getTime());

        final String[] newDate = {""};
        final String[] newTime = {""};


        Calendar calendar = Calendar.getInstance();


        btnSelectNewDate.setOnClickListener(v -> {
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    Admin.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        newDate[0] = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear);
                        btnSelectNewDate.setText(newDate[0]);
                        btnSelectNewDate.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.success)));
                    },
                    year, month, day
            );

            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            datePickerDialog.show();
        });

        btnSelectNewTime.setOnClickListener(v -> {
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    Admin.this,
                    (view, selectedHour, selectedMinute) -> {
                        String amPm = selectedHour >= 12 ? "PM" : "AM";
                        int hour12 = selectedHour % 12;
                        if (hour12 == 0) hour12 = 12;
                        newTime[0] = String.format("%02d:%02d %s", hour12, selectedMinute, amPm);
                        btnSelectNewTime.setText(newTime[0]);
                        btnSelectNewTime.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getColor(R.color.success)));
                    },
                    hour, minute, false
            );
            timePickerDialog.show();
        });

        btnCancelReschedule.setOnClickListener(v -> dialog.dismiss());

        btnConfirmReschedule.setOnClickListener(v -> {
            if (newDate[0].isEmpty() || newTime[0].isEmpty()) {
                Toast.makeText(Admin.this, "Please select both date and time", Toast.LENGTH_SHORT).show();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);
            appointment.setDate(newDate[0]);
            appointment.setTime(newTime[0]);

            mDatabase.child("Appointments").child(appointment.getAppointmentId())
                    .setValue(appointment)
                    .addOnSuccessListener(aVoid -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(Admin.this, "Appointment rescheduled successfully!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(Admin.this, "Error rescheduling appointment: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
        });

        dialog.show();
    }


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
