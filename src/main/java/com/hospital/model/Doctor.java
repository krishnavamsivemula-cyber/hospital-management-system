package com.hospital.model;

/**
 * Model class representing a Doctor entity in the Hospital Management System.
 * Maps to the MySQL 'doctors' table.
 */
public class Doctor {

    // Maps to column: doctor_id (INT, PRIMARY KEY, AUTO_INCREMENT)
    private int doctorId;

    // Maps to column: name (VARCHAR(100), NOT NULL)
    private String name;

    // Maps to column: specialization (VARCHAR(100), NOT NULL)
    private String specialization;

    // Maps to column: phone (VARCHAR(15))
    private String phone;

    // Maps to column: email (VARCHAR(100))
    private String email;

    /**
     * Default no-argument constructor.
     */
    public Doctor() {
    }

    /**
     * Constructor without doctorId, used when adding a new doctor
     * where the database generates the auto-increment primary key.
     */
    public Doctor(String name, String specialization, String phone, String email) {
        this.name = name;
        this.specialization = specialization;
        this.phone = phone;
        this.email = email;
    }

    /**
     * Full constructor including doctorId, used when retrieving existing
     * doctor records from the database.
     */
    public Doctor(int doctorId, String name, String specialization, String phone, String email) {
        this.doctorId = doctorId;
        this.name = name;
        this.specialization = specialization;
        this.phone = phone;
        this.email = email;
    }

    // --- Getters and Setters ---

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Formatted label for user-friendly display in UI components (like JComboBox).
     */
    public String getDisplayLabel() {
        return "Dr. " + name + " (" + specialization + ", #" + doctorId + ")";
    }

    @Override
    public String toString() {
        return "Doctor{" +
                "doctorId=" + doctorId +
                ", name='" + name + '\'' +
                ", specialization='" + specialization + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}

