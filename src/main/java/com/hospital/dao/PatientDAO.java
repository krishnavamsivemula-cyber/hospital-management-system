package com.hospital.dao;

import com.hospital.config.DatabaseConnection;
import com.hospital.model.Patient;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for 'patients' table.
 * Handles all JDBC operations for Patient entities.
 */
public class PatientDAO {

    /**
     * Inserts a new patient record into the database.
     * Sets the auto-generated patientId onto the provided patient object.
     *
     * @param patient Patient to insert
     * @return generated patient_id
     * @throws SQLException on database error
     */
    public int insert(Patient patient) throws SQLException {
        String sql = "INSERT INTO patients (name, age, gender, phone) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, patient.getName());
            ps.setInt(2, patient.getAge());
            ps.setString(3, patient.getGender());
            ps.setString(4, patient.getPhone());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int generatedId = rs.getInt(1);
                    patient.setPatientId(generatedId);
                    return generatedId;
                }
            }
        }
        return -1;
    }

    /**
     * Updates an existing patient record.
     *
     * @param patient Patient with updated information and valid patientId
     * @return true if updated, false if not found
     * @throws SQLException on database error
     */
    public boolean update(Patient patient) throws SQLException {
        String sql = "UPDATE patients SET name = ?, age = ?, gender = ?, phone = ? WHERE patient_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, patient.getName());
            ps.setInt(2, patient.getAge());
            ps.setString(3, patient.getGender());
            ps.setString(4, patient.getPhone());
            ps.setInt(5, patient.getPatientId());

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Deletes a patient by patientId.
     *
     * @param patientId ID of the patient to delete
     * @return true if deleted, false if not found
     * @throws SQLException on database error
     */
    public boolean delete(int patientId) throws SQLException {
        String sql = "DELETE FROM patients WHERE patient_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, patientId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Finds a single patient by ID.
     *
     * @param patientId patient ID
     * @return Patient if found, or null
     * @throws SQLException on database error
     */
    public Patient findById(int patientId) throws SQLException {
        String sql = "SELECT patient_id, name, age, gender, phone FROM patients WHERE patient_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /**
     * Retrieves all patients ordered by patient_id descending.
     *
     * @return list of all patients
     * @throws SQLException on database error
     */
    public List<Patient> findAll() throws SQLException {
        List<Patient> list = new ArrayList<>();
        String sql = "SELECT patient_id, name, age, gender, phone FROM patients ORDER BY patient_id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    /**
     * Searches patients by full or partial name (case-insensitive).
     *
     * @param nameQuery name search term
     * @return matching patients
     * @throws SQLException on database error
     */
    public List<Patient> searchByName(String nameQuery) throws SQLException {
        List<Patient> list = new ArrayList<>();
        String sql = "SELECT patient_id, name, age, gender, phone FROM patients WHERE LOWER(name) LIKE ? ORDER BY name ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + nameQuery.toLowerCase().trim() + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /**
     * Checks if a patient has any existing appointments.
     * Used to prevent unsafe deletions.
     *
     * @param patientId patient ID
     * @return true if patient has appointments, false otherwise
     * @throws SQLException on database error
     */
    public boolean hasAppointments(int patientId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM appointments WHERE patient_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Returns the total count of patients in the database.
     *
     * @return total patient count
     * @throws SQLException on database error
     */
    public int countPatients() throws SQLException {
        String sql = "SELECT COUNT(*) FROM patients";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * Helper method to map a ResultSet row to a Patient entity.
     */
    private Patient mapRow(ResultSet rs) throws SQLException {
        return new Patient(
                rs.getInt("patient_id"),
                rs.getString("name"),
                rs.getInt("age"),
                rs.getString("gender"),
                rs.getString("phone")
        );
    }
}

