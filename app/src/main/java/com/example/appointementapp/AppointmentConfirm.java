package com.example.appointementapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.auth.FirebaseAuth;

// Appointment Confirmation Activity

public class AppointmentConfirm extends AppCompatActivity {

    private TextView tvPatientName, tvEmail, tvPhone, tvDate, tvTime;
    private Button btnChatbot, btnLogout;
    private ProgressBar progressBar;


    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;


    private String appointmentId;
    private Appointment currentAppointment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_confirm);


        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        initializeViews();


        appointmentId = getIntent().getStringExtra("appointmentId");

        if (appointmentId != null) {

            fetchAppointmentDetails();
        } else {
            Toast.makeText(this, "Error: Appointment ID not found", Toast.LENGTH_SHORT).show();
            finish();
        }


        setupClickListeners();
    }



    private void initializeViews() {
        tvPatientName = findViewById(R.id.tvPatientName);
        tvEmail = findViewById(R.id.tvEmail);
        tvPhone = findViewById(R.id.tvPhone);
        tvDate = findViewById(R.id.tvDate);
        tvTime = findViewById(R.id.tvTime);
        btnChatbot = findViewById(R.id.btnChatbot);
        btnLogout = findViewById(R.id.btnLogout);
        progressBar = findViewById(R.id.progressBar);
    }


    private void setupClickListeners() {
        btnChatbot.setOnClickListener(v -> launchChatbot());
        btnLogout.setOnClickListener(v -> logout());
    }

    // Fetch appointment details from Firebase Realtime Database

    private void fetchAppointmentDetails() {
        progressBar.setVisibility(View.VISIBLE);

        mDatabase.child("Appointments").child(appointmentId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        progressBar.setVisibility(View.GONE);

                        if (dataSnapshot.exists()) {
                            currentAppointment = dataSnapshot.getValue(Appointment.class);
                            if (currentAppointment != null) {
                                displayAppointmentDetails();
                            } else {
                                Toast.makeText(AppointmentConfirm.this, "Error: Could not load appointment data", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(AppointmentConfirm.this, "Appointment not found", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(AppointmentConfirm.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    //Display fetched appointment details in UI

    private void displayAppointmentDetails() {

        tvPatientName.setText(currentAppointment.getUserName());
        tvEmail.setText(currentAppointment.getUserEmail());
        tvPhone.setText(currentAppointment.getPhoneNumber());


        tvDate.setText(currentAppointment.getDate());
        tvTime.setText(currentAppointment.getTime());


        btnChatbot.setEnabled(true);
    }

    // Launch Chatbot Activity with appointment data

    private void launchChatbot() {
        if (currentAppointment != null) {
            Intent intent = new Intent(AppointmentConfirm.this, Chatbot.class);
            intent.putExtra("appointmentId", appointmentId);
            intent.putExtra("patientName", currentAppointment.getUserName());
            intent.putExtra("patientEmail", currentAppointment.getUserEmail());
            intent.putExtra("bloodGroup", currentAppointment.getBloodGroup());
            intent.putExtra("pastProblems", currentAppointment.getPastProblems());
            intent.putExtra("familyHistory", currentAppointment.getFamilyMedicalHistory());
            intent.putExtra("problemDescription", currentAppointment.getProblemDescription());
            startActivity(intent);
        }
    }


    private void logout() {
        mAuth.signOut();
        Intent intent = new Intent(AppointmentConfirm.this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(AppointmentConfirm.this, Login.class));
            finish();
        }
    }


}

