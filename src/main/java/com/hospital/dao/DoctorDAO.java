package com.hospital.dao;

import com.hospital.config.DatabaseConnection;
import com.hospital.model.Doctor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for 'doctors' table.
 * Handles all JDBC operations for Doctor entities.
 */
public class DoctorDAO {

    /**
     * Inserts a new doctor into the database.
     * Sets the auto-generated doctorId on the passed doctor.
     *
     * @param doctor Doctor to insert
     * @return generated doctor_id
     * @throws SQLException on database error
     */
    public int insert(Doctor doctor) throws SQLException {
        String sql = "INSERT INTO doctors (name, specialization, phone, email) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, doctor.getName());
            ps.setString(2, doctor.getSpecialization());
            ps.setString(3, doctor.getPhone());
            ps.setString(4, doctor.getEmail());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int generatedId = rs.getInt(1);
                    doctor.setDoctorId(generatedId);
                    return generatedId;
                }
            }
        }
        return -1;
    }

    /**
     * Updates an existing doctor's record.
     *
     * @param doctor Doctor with updated details and valid doctorId
     * @return true if updated, false if not found
     * @throws SQLException on database error
     */
    public boolean update(Doctor doctor) throws SQLException {
        String sql = "UPDATE doctors SET name = ?, specialization = ?, phone = ?, email = ? WHERE doctor_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, doctor.getName());
            ps.setString(2, doctor.getSpecialization());
            ps.setString(3, doctor.getPhone());
            ps.setString(4, doctor.getEmail());
            ps.setInt(5, doctor.getDoctorId());

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Deletes a doctor by doctorId.
     *
     * @param doctorId ID of doctor to delete
     * @return true if deleted, false if not found
     * @throws SQLException on database error
     */
    public boolean delete(int doctorId) throws SQLException {
        String sql = "DELETE FROM doctors WHERE doctor_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, doctorId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Finds a doctor by ID.
     *
     * @param doctorId doctor ID
     * @return Doctor if found, or null
     * @throws SQLException on database error
     */
    public Doctor findById(int doctorId) throws SQLException {
        String sql = "SELECT doctor_id, name, specialization, phone, email FROM doctors WHERE doctor_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, doctorId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /**
     * Retrieves all doctors ordered by name.
     *
     * @return list of doctors
     * @throws SQLException on database error
     */
    public List<Doctor> findAll() throws SQLException {
        List<Doctor> list = new ArrayList<>();
        String sql = "SELECT doctor_id, name, specialization, phone, email FROM doctors ORDER BY name ASC";
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
     * Searches doctors by partial or full name.
     *
     * @param nameQuery name search term
     * @return matching doctors
     * @throws SQLException on database error
     */
    public List<Doctor> searchByName(String nameQuery) throws SQLException {
        List<Doctor> list = new ArrayList<>();
        String sql = "SELECT doctor_id, name, specialization, phone, email FROM doctors WHERE LOWER(name) LIKE ? ORDER BY name ASC";
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
     * Searches doctors by specialization.
     *
     * @param specQuery specialization search term
     * @return matching doctors
     * @throws SQLException on database error
     */
    public List<Doctor> searchBySpecialization(String specQuery) throws SQLException {
        List<Doctor> list = new ArrayList<>();
        String sql = "SELECT doctor_id, name, specialization, phone, email FROM doctors WHERE LOWER(specialization) LIKE ? ORDER BY name ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + specQuery.toLowerCase().trim() + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /**
     * Checks if a doctor has any existing appointments.
     * Used to prevent unsafe deletions.
     *
     * @param doctorId doctor ID
     * @return true if doctor has appointments, false otherwise
     * @throws SQLException on database error
     */
    public boolean hasAppointments(int doctorId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM appointments WHERE doctor_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, doctorId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Returns the total count of registered doctors.
     *
     * @return total count
     * @throws SQLException on database error
     */
    public int countDoctors() throws SQLException {
        String sql = "SELECT COUNT(*) FROM doctors";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    private Doctor mapRow(ResultSet rs) throws SQLException {
        return new Doctor(
                rs.getInt("doctor_id"),
                rs.getString("name"),
                rs.getString("specialization"),
                rs.getString("phone"),
                rs.getString("email")
        );
    }
}

