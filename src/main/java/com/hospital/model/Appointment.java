package com.hospital.model;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Model class representing an Appointment entity in the Hospital Management System.
 * Maps to the MySQL 'appointments' table and includes joined helper fields for display.
 */
public class Appointment {

    // Maps to column: appointment_id (INT, PRIMARY KEY, AUTO_INCREMENT)
    private int appointmentId;

    // Maps to column: patient_id (INT, FOREIGN KEY)
    private int patientId;

    // Maps to column: doctor_id (INT, FOREIGN KEY)
    private int doctorId;

    // Maps to column: appointment_date (DATE, NOT NULL)
    private LocalDate appointmentDate;

    // Maps to column: appointment_time (TIME, NOT NULL)
    private LocalTime appointmentTime;

    // Maps to column: status (VARCHAR(20), NOT NULL DEFAULT 'SCHEDULED')
    // Common values: SCHEDULED, COMPLETED, CANCELLED
    private String status;

    // Maps to column: reason (VARCHAR(255))
    private String reason;

    // Joined fields for display convenience in tables and UI
    private String patientName;
    private String patientPhone;
    private String doctorName;
    private String doctorSpecialization;

    /**
     * Default no-argument constructor.
     */
    public Appointment() {
        this.status = "SCHEDULED";
    }

    /**
     * Constructor without appointmentId, used when booking a new appointment.
     */
    public Appointment(int patientId, int doctorId, LocalDate appointmentDate, LocalTime appointmentTime, String reason) {
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.status = "SCHEDULED";
        this.reason = reason;
    }

    /**
     * Constructor without appointmentId, with explicit status.
     */
    public Appointment(int patientId, int doctorId, LocalDate appointmentDate, LocalTime appointmentTime, String status, String reason) {
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.status = (status == null || status.isBlank()) ? "SCHEDULED" : status;
        this.reason = reason;
    }

    /**
     * Full constructor including appointmentId.
     */
    public Appointment(int appointmentId, int patientId, int doctorId, LocalDate appointmentDate, LocalTime appointmentTime, String status, String reason) {
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.status = status;
        this.reason = reason;
    }

    // --- Getters and Setters ---

    public int getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(int appointmentId) {
        this.appointmentId = appointmentId;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(LocalDate appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public LocalTime getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(LocalTime appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientPhone() {
        return patientPhone;
    }

    public void setPatientPhone(String patientPhone) {
        this.patientPhone = patientPhone;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getDoctorSpecialization() {
        return doctorSpecialization;
    }

    public void setDoctorSpecialization(String doctorSpecialization) {
        this.doctorSpecialization = doctorSpecialization;
    }

    @Override
    public String toString() {
        return "Appointment{" +
                "appointmentId=" + appointmentId +
                ", patientId=" + patientId +
                ", doctorId=" + doctorId +
                ", appointmentDate=" + appointmentDate +
                ", appointmentTime=" + appointmentTime +
                ", status='" + status + '\'' +
                ", reason='" + reason + '\'' +
                '}';
    }
}

