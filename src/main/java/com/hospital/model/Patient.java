package com.hospital.model;

/**
 * Model class representing a Patient entity in the Hospital Management System.
 * Maps directly to the MySQL 'patients' table.
 */
public class Patient {

    // Maps to column: patient_id (INT, PRIMARY KEY, AUTO_INCREMENT)
    private int patientId;

    // Maps to column: name (VARCHAR(100), NOT NULL)
    private String name;

    // Maps to column: age (INT)
    private int age;

    // Maps to column: gender (VARCHAR(10))
    private String gender;

    // Maps to column: phone (VARCHAR(15))
    private String phone;

    /**
     * Default no-argument constructor.
     */
    public Patient() {
    }

    /**
     * Constructor without patientId, used when creating a new patient
     * where the database generates the auto-increment primary key.
     *
     * @param name   Patient's full name
     * @param age    Patient's age
     * @param gender Patient's gender
     * @param phone  Patient's contact phone number
     */
    public Patient(String name, int age, String gender, String phone) {
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.phone = phone;
    }

    /**
     * Full constructor including patientId, used when retrieving existing
     * patient records from the database.
     *
     * @param patientId Database primary key
     * @param name      Patient's full name
     * @param age       Patient's age
     * @param gender    Patient's gender
     * @param phone     Patient's contact phone number
     */
    public Patient(int patientId, String name, int age, String gender, String phone) {
        this.patientId = patientId;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.phone = phone;
    }

    // --- Getters and Setters ---

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    // --- toString ---

    @Override
    public String toString() {
        return "Patient{" +
                "patientId=" + patientId +
                ", name='" + name + '\'' +
                ", age=" + age +
                ", gender='" + gender + '\'' +
                ", phone='" + phone + '\'' +
                '}';
    }
}

