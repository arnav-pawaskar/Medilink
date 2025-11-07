package com.example.appointementapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;


public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder> {

    private List<Appointment> appointmentList;
    private Context context;
    private OnAppointmentActionListener actionListener;


    public interface OnAppointmentActionListener {
        void onConfirmClick(Appointment appointment);
        void onCancelClick(Appointment appointment);
        void onItemClick(Appointment appointment);
        void onDeleteClick(Appointment appointment, int position);
        void onRescheduleClick(Appointment appointment);
    }


    public AppointmentAdapter(Context context, List<Appointment> appointmentList,
                            OnAppointmentActionListener actionListener) {
        this.context = context;
        this.appointmentList = appointmentList;
        this.actionListener = actionListener;
    }


    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_appointment, parent, false);
        return new AppointmentViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        Appointment appointment = appointmentList.get(position);


        holder.tvPatientName.setText(appointment.getUserName());


        holder.tvEmail.setText(appointment.getUserEmail());


        holder.tvPhone.setText(appointment.getPhoneNumber());


        holder.tvDateTime.setText(appointment.getDate() + " | " + appointment.getTime());

        // Display specialist if available
        String specialist = appointment.getRecommendedSpecialist();
        if (specialist != null && !specialist.isEmpty()) {
            holder.tvSpecialist.setText(specialist);
            holder.llSpecialistContainer.setVisibility(View.VISIBLE);
        } else {
            holder.llSpecialistContainer.setVisibility(View.GONE);
        }

        String status = appointment.getStatus() != null ? appointment.getStatus() : "pending";
        holder.tvStatus.setText("● " + status.substring(0, 1).toUpperCase() + status.substring(1));

        if ("confirmed".equalsIgnoreCase(status)) {
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.success));
        } else if ("cancelled".equalsIgnoreCase(status)) {
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.error));
        } else {
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.warning));
        }


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

        // Show reschedule button only for confirmed appointments
        if ("confirmed".equalsIgnoreCase(status)) {
            holder.btnReschedule.setVisibility(View.VISIBLE);
            holder.btnReschedule.setEnabled(true);
            holder.btnReschedule.setAlpha(1.0f);
        } else {
            holder.btnReschedule.setVisibility(View.GONE);
        }


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

        holder.btnReschedule.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onRescheduleClick(appointment);
            }
        });


        holder.itemView.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onItemClick(appointment);
            }
        });
    }


    @Override
    public int getItemCount() {
        return appointmentList.size();
    }


    public void updateList(List<Appointment> newList) {
        this.appointmentList = newList;
        notifyDataSetChanged();
    }


    public void removeItem(int position) {
        if (position >= 0 && position < appointmentList.size()) {
            appointmentList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, appointmentList.size());
        }
    }


    public Appointment getItem(int position) {
        if (position >= 0 && position < appointmentList.size()) {
            return appointmentList.get(position);
        }
        return null;
    }


    public static class AppointmentViewHolder extends RecyclerView.ViewHolder {
        TextView tvPatientName, tvEmail, tvPhone, tvDateTime, tvStatus, tvSpecialist;
        Button btnConfirm, btnCancel, btnReschedule;
        LinearLayout llSpecialistContainer;

        public AppointmentViewHolder(@NonNull View itemView) {
            super(itemView);


            tvPatientName = itemView.findViewById(R.id.tvPatientName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvDateTime = itemView.findViewById(R.id.tvDateTime);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvSpecialist = itemView.findViewById(R.id.tvSpecialist);
            llSpecialistContainer = itemView.findViewById(R.id.llSpecialistContainer);
            btnConfirm = itemView.findViewById(R.id.btnConfirm);
            btnCancel = itemView.findViewById(R.id.btnCancel);
            btnReschedule = itemView.findViewById(R.id.btnReschedule);
        }
    }
}
