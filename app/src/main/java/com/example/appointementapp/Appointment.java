package com.example.appointementapp;

/**
 * Appointment Model Class
 */
public class Appointment {
    private String appointmentId;
    private String userName;
    private String userEmail;
    private String phoneNumber;
    private String bloodGroup;
    private String pastProblems;
    private String familyMedicalHistory;
    private String problemDescription;
    private String date;
    private String time;
    private String userId;
    private String status; // pending, confirmed, cancelled

    /**
     * Default constructor (required by Firebase)
     */
    public Appointment() {
    }

    public Appointment(String appointmentId, String userName, String userEmail,
                       String phoneNumber, String bloodGroup, String pastProblems,
                       String familyMedicalHistory, String problemDescription,
                       String date, String time, String userId, String status) {
        this.appointmentId = appointmentId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.phoneNumber = phoneNumber;
        this.bloodGroup = bloodGroup;
        this.pastProblems = pastProblems;
        this.familyMedicalHistory = familyMedicalHistory;
        this.problemDescription = problemDescription;
        this.date = date;
        this.time = time;
        this.userId = userId;
        this.status = status;
    }

    // Getters and Setters

    public String getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(String appointmentId) {
        this.appointmentId = appointmentId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getPastProblems() {
        return pastProblems;
    }

    public void setPastProblems(String pastProblems) {
        this.pastProblems = pastProblems;
    }

    public String getFamilyMedicalHistory() {
        return familyMedicalHistory;
    }

    public void setFamilyMedicalHistory(String familyMedicalHistory) {
        this.familyMedicalHistory = familyMedicalHistory;
    }

    public String getProblemDescription() {
        return problemDescription;
    }

    public void setProblemDescription(String problemDescription) {
        this.problemDescription = problemDescription;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Override toString for debugging
     */
    @Override
    public String toString() {
        return "Appointment{" +
                "appointmentId='" + appointmentId + '\'' +
                ", userName='" + userName + '\'' +
                ", userEmail='" + userEmail + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", bloodGroup='" + bloodGroup + '\'' +
                ", date='" + date + '\'' +
                ", time='" + time + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}

