package com.example.appointementapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * AppointmentAdapter - Custom RecyclerView adapter for displaying appointments
 */
public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder> {

    private List<Appointment> appointmentList;
    private Context context;
    private OnAppointmentActionListener actionListener;

    /**
     * Interface for appointment action callbacks
     */
    public interface OnAppointmentActionListener {
        void onConfirmClick(Appointment appointment);
        void onCancelClick(Appointment appointment);
        void onItemClick(Appointment appointment);
        void onDeleteClick(Appointment appointment, int position);
    }

    /**
     * Constructor
     */
    public AppointmentAdapter(Context context, List<Appointment> appointmentList,
                            OnAppointmentActionListener actionListener) {
        this.context = context;
        this.appointmentList = appointmentList;
        this.actionListener = actionListener;
    }

    /**
     * Create ViewHolder when RecyclerView needs a new item view
     */
    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_appointment, parent, false);
        return new AppointmentViewHolder(view);
    }

    /**
     * Bind data to ViewHolder at specified position
     */
    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        Appointment appointment = appointmentList.get(position);

        // Set patient name (remove "Patient:" prefix for cleaner look)
        holder.tvPatientName.setText(appointment.getUserName());

        // Set email (remove "Email:" prefix, icon shows this)
        holder.tvEmail.setText(appointment.getUserEmail());

        // Set phone (remove "Phone:" prefix, icon shows this)
        holder.tvPhone.setText(appointment.getPhoneNumber());

        // Set appointment date and time
        holder.tvDateTime.setText(appointment.getDate() + " | " + appointment.getTime());

        // Set status with color coding
        String status = appointment.getStatus() != null ? appointment.getStatus() : "pending";
        holder.tvStatus.setText("● " + status.substring(0, 1).toUpperCase() + status.substring(1));

        if ("confirmed".equalsIgnoreCase(status)) {
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.success));
        } else if ("cancelled".equalsIgnoreCase(status)) {
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.error));
        } else {
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.warning));
        }

        // Disable buttons if appointment is already confirmed or cancelled
        if ("confirmed".equalsIgnoreCase(status) || "cancelled".equalsIgnoreCase(status)) {
            holder.btnConfirm.setEnabled(false);
            holder.btnCancel.setEnabled(false);
            holder.btnConfirm.setAlpha(0.5f);
            holder.btnCancel.setAlpha(0.5f);
        } else {
            holder.btnConfirm.setEnabled(true);
            holder.btnCancel.setEnabled(true);
            holder.btnConfirm.setAlpha(1.0f);
            holder.btnCancel.setAlpha(1.0f);
        }

        // Set button click listeners
        holder.btnConfirm.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onConfirmClick(appointment);
            }
        });

        holder.btnCancel.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onCancelClick(appointment);
            }
        });

        // Set item click listener for detail view
        holder.itemView.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onItemClick(appointment);
            }
        });
    }

    /**
     * Get total number of items
     */
    @Override
    public int getItemCount() {
        return appointmentList.size();
    }

    /**
     * Update the adapter with a new list
     */
    public void updateList(List<Appointment> newList) {
        this.appointmentList = newList;
        notifyDataSetChanged();
    }

    /**
     * Remove item at specific position
     */
    public void removeItem(int position) {
        if (position >= 0 && position < appointmentList.size()) {
            appointmentList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, appointmentList.size());
        }
    }

    /**
     * Get appointment at specific position
     */
    public Appointment getItem(int position) {
        if (position >= 0 && position < appointmentList.size()) {
            return appointmentList.get(position);
        }
        return null;
    }

    /**
     * ViewHolder class for appointment items
     */
    public static class AppointmentViewHolder extends RecyclerView.ViewHolder {
        TextView tvPatientName, tvEmail, tvPhone, tvDateTime, tvStatus;
        Button btnConfirm, btnCancel;

        public AppointmentViewHolder(@NonNull View itemView) {
            super(itemView);

            // Initialize UI components
            tvPatientName = itemView.findViewById(R.id.tvPatientName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvDateTime = itemView.findViewById(R.id.tvDateTime);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnConfirm = itemView.findViewById(R.id.btnConfirm);
            btnCancel = itemView.findViewById(R.id.btnCancel);
        }
    }
}
