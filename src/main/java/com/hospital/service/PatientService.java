package com.hospital.service;

import com.hospital.dao.PatientDAO;
import com.hospital.model.Patient;

import java.sql.SQLException;
import java.util.List;

/**
 * Service class handling business logic and validations for Patient management.
 */
public class PatientService {

    private final PatientDAO patientDAO;

    public PatientService() {
        this.patientDAO = new PatientDAO();
    }

    public PatientService(PatientDAO patientDAO) {
        this.patientDAO = patientDAO;
    }

    /**
     * Registers a new patient after validating input fields.
     */
    public int registerPatient(Patient patient) throws SQLException {
        validatePatient(patient);
        return patientDAO.insert(patient);
    }

    /**
     * Updates an existing patient record after validation.
     */
    public void updatePatient(Patient patient) throws SQLException {
        if (patient.getPatientId() <= 0) {
            throw new IllegalArgumentException("Invalid patient ID for update.");
        }
        validatePatient(patient);
        boolean updated = patientDAO.update(patient);
        if (!updated) {
            throw new IllegalArgumentException("Patient with ID " + patient.getPatientId() + " not found.");
        }
    }

    /**
     * Deletes a patient after checking that they have no existing appointments.
     */
    public void deletePatient(int patientId) throws SQLException {
        if (patientId <= 0) {
            throw new IllegalArgumentException("Invalid patient ID.");
        }

        Patient existing = patientDAO.findById(patientId);
        if (existing == null) {
            throw new IllegalArgumentException("Patient with ID " + patientId + " does not exist.");
        }

        if (patientDAO.hasAppointments(patientId)) {
            throw new IllegalStateException(
                    "Cannot delete patient '" + existing.getName() + "' (ID: " + patientId + "): " +
                    "They have appointment records on file. Deleting is prevented to maintain medical records."
            );
        }

        patientDAO.delete(patientId);
    }

    /**
     * Retrieves a patient by ID.
     */
    public Patient getPatientById(int patientId) throws SQLException {
        return patientDAO.findById(patientId);
    }

    /**
     * Retrieves all registered patients.
     */
    public List<Patient> getAllPatients() throws SQLException {
        return patientDAO.findAll();
    }

    /**
     * Searches patients by ID if numeric, otherwise searches by partial name.
     */
    public List<Patient> searchPatients(String query) throws SQLException {
        if (query == null || query.isBlank()) {
            return getAllPatients();
        }

        String trimmed = query.trim();
        // Check if query is an integer ID
        if (trimmed.matches("\\d+")) {
            Patient p = patientDAO.findById(Integer.parseInt(trimmed));
            return (p != null) ? List.of(p) : List.of();
        }

        return patientDAO.searchByName(trimmed);
    }

    /**
     * Returns total count of registered patients.
     */
    public int getTotalPatientsCount() throws SQLException {
        return patientDAO.countPatients();
    }

    /**
     * Validates patient fields according to hospital business rules.
     */
    private void validatePatient(Patient patient) {
        if (patient == null) {
            throw new IllegalArgumentException("Patient information cannot be null.");
        }

        if (patient.getName() == null || patient.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Patient name is required.");
        }

        if (patient.getName().trim().length() < 2) {
            throw new IllegalArgumentException("Patient name must be at least 2 characters.");
        }

        if (patient.getAge() < 0 || patient.getAge() > 130) {
            throw new IllegalArgumentException("Patient age must be between 0 and 130.");
        }

        String gender = patient.getGender();
        if (gender == null || gender.isBlank() ||
                (!gender.equalsIgnoreCase("Male") &&
                 !gender.equalsIgnoreCase("Female") &&
                 !gender.equalsIgnoreCase("Other"))) {
            throw new IllegalArgumentException("Please select a valid gender (Male, Female, or Other).");
        }

        String phone = patient.getPhone();
        if (phone != null && !phone.isBlank()) {
            // Check reasonable phone number format (between 7 and 15 digits/characters)
            String digitsOnly = phone.replaceAll("[^0-9]", "");
            if (digitsOnly.length() < 7 || digitsOnly.length() > 15) {
                throw new IllegalArgumentException("Phone number must contain between 7 and 15 digits.");
            }
        }
    }
}

