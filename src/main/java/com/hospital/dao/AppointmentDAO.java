package com.hospital.dao;

import com.hospital.config.DatabaseConnection;
import com.hospital.model.Appointment;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for 'appointments' table.
 * Handles all JDBC operations for appointments, including joins with patients and doctors.
 */
public class AppointmentDAO {

    private static final String BASE_SELECT =
            "SELECT a.appointment_id, a.patient_id, a.doctor_id, a.appointment_date, a.appointment_time, " +
            "       a.status, a.reason, " +
            "       p.name AS patient_name, p.phone AS patient_phone, " +
            "       d.name AS doctor_name, d.specialization AS doctor_specialization " +
            "FROM appointments a " +
            "JOIN patients p ON a.patient_id = p.patient_id " +
            "JOIN doctors d ON a.doctor_id = d.doctor_id ";

    /**
     * Inserts a new appointment record.
     *
     * @param appointment Appointment entity
     * @return generated appointment_id
     * @throws SQLException on database error
     */
    public int insert(Appointment appointment) throws SQLException {
        String sql = "INSERT INTO appointments (patient_id, doctor_id, appointment_date, appointment_time, status, reason) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, appointment.getPatientId());
            ps.setInt(2, appointment.getDoctorId());
            ps.setDate(3, Date.valueOf(appointment.getAppointmentDate()));
            ps.setTime(4, Time.valueOf(appointment.getAppointmentTime()));
            ps.setString(5, appointment.getStatus());
            ps.setString(6, appointment.getReason());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int generatedId = rs.getInt(1);
                    appointment.setAppointmentId(generatedId);
                    return generatedId;
                }
            }
        }
        return -1;
    }

    /**
     * Updates an appointment's details (date, time, status, reason, doctor, patient).
     *
     * @param appointment Appointment entity
     * @return true if updated, false if not found
     * @throws SQLException on database error
     */
    public boolean update(Appointment appointment) throws SQLException {
        String sql = "UPDATE appointments SET patient_id = ?, doctor_id = ?, appointment_date = ?, " +
                     "appointment_time = ?, status = ?, reason = ? WHERE appointment_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, appointment.getPatientId());
            ps.setInt(2, appointment.getDoctorId());
            ps.setDate(3, Date.valueOf(appointment.getAppointmentDate()));
            ps.setTime(4, Time.valueOf(appointment.getAppointmentTime()));
            ps.setString(5, appointment.getStatus());
            ps.setString(6, appointment.getReason());
            ps.setInt(7, appointment.getAppointmentId());

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Updates the status of an appointment (e.g. to CANCELLED or COMPLETED).
     *
     * @param appointmentId ID of appointment
     * @param newStatus new status string
     * @return true if updated
     * @throws SQLException on database error
     */
    public boolean updateStatus(int appointmentId, String newStatus) throws SQLException {
        String sql = "UPDATE appointments SET status = ? WHERE appointment_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newStatus);
            ps.setInt(2, appointmentId);

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Finds an appointment by ID including joined patient and doctor details.
     *
     * @param appointmentId appointment ID
     * @return Appointment or null
     * @throws SQLException on database error
     */
    public Appointment findById(int appointmentId) throws SQLException {
        String sql = BASE_SELECT + "WHERE a.appointment_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, appointmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /**
     * Retrieves all appointments ordered by date descending, time descending.
     *
     * @return list of appointments
     * @throws SQLException on database error
     */
    public List<Appointment> findAll() throws SQLException {
        List<Appointment> list = new ArrayList<>();
        String sql = BASE_SELECT + "ORDER BY a.appointment_date DESC, a.appointment_time DESC";
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
     * Retrieves today's appointments ordered by time ascending.
     *
     * @return list of today's appointments
     * @throws SQLException on database error
     */
    public List<Appointment> findTodayAppointments() throws SQLException {
        List<Appointment> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE a.appointment_date = CURRENT_DATE() " +
                     "ORDER BY a.appointment_time ASC";
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
     * Retrieves upcoming scheduled appointments.
     *
     * @param limit maximum records to fetch
     * @return list of upcoming appointments
     * @throws SQLException on database error
     */
    public List<Appointment> findUpcomingAppointments(int limit) throws SQLException {
        List<Appointment> list = new ArrayList<>();
        String sql = BASE_SELECT +
                     "WHERE (a.appointment_date > CURRENT_DATE() " +
                     "       OR (a.appointment_date = CURRENT_DATE() AND a.appointment_time >= CURRENT_TIME())) " +
                     "  AND a.status = 'SCHEDULED' " +
                     "ORDER BY a.appointment_date ASC, a.appointment_time ASC " +
                     "LIMIT ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /**
     * Filters appointments by optional date, status, and search query.
     *
     * @param date specific date (or null for all dates)
     * @param status specific status (or null/empty/"ALL" for all statuses)
     * @param query search text for patient/doctor/reason (or null/empty)
     * @return filtered list
     * @throws SQLException on database error
     */
    public List<Appointment> filter(LocalDate date, String status, String query) throws SQLException {
        List<Appointment> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(BASE_SELECT).append("WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (date != null) {
            sql.append("AND a.appointment_date = ? ");
            params.add(Date.valueOf(date));
        }

        if (status != null && !status.isBlank() && !status.equalsIgnoreCase("ALL")) {
            sql.append("AND a.status = ? ");
            params.add(status.toUpperCase().trim());
        }

        if (query != null && !query.isBlank()) {
            sql.append("AND (LOWER(p.name) LIKE ? OR LOWER(d.name) LIKE ? OR LOWER(a.reason) LIKE ?) ");
            String term = "%" + query.toLowerCase().trim() + "%";
            params.add(term);
            params.add(term);
            params.add(term);
        }

        sql.append("ORDER BY a.appointment_date DESC, a.appointment_time DESC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /**
     * Checks if a doctor already has an active (non-cancelled) appointment at the specified date and time.
     *
     * @param doctorId doctor ID
     * @param date appointment date
     * @param time appointment time
     * @param excludeAppointmentId appointment ID to exclude (e.g. when updating existing appointment), or null
     * @return true if conflict exists, false otherwise
     * @throws SQLException on database error
     */
    public boolean hasConflict(int doctorId, LocalDate date, LocalTime time, Integer excludeAppointmentId) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM appointments " +
                "WHERE doctor_id = ? AND appointment_date = ? AND appointment_time = ? AND status != 'CANCELLED' "
        );

        if (excludeAppointmentId != null) {
            sql.append("AND appointment_id != ?");
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            ps.setInt(1, doctorId);
            ps.setDate(2, Date.valueOf(date));
            ps.setTime(3, Time.valueOf(time));

            if (excludeAppointmentId != null) {
                ps.setInt(4, excludeAppointmentId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Counts the total number of appointments in the system.
     *
     * @return total count
     * @throws SQLException on database error
     */
    public int countTotalAppointments() throws SQLException {
        String sql = "SELECT COUNT(*) FROM appointments";
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
     * Counts the number of appointments scheduled for today.
     *
     * @return today's appointment count
     * @throws SQLException on database error
     */
    public int countTodayAppointments() throws SQLException {
        String sql = "SELECT COUNT(*) FROM appointments WHERE appointment_date = CURRENT_DATE()";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    private Appointment mapRow(ResultSet rs) throws SQLException {
        Appointment apt = new Appointment();
        apt.setAppointmentId(rs.getInt("appointment_id"));
        apt.setPatientId(rs.getInt("patient_id"));
        apt.setDoctorId(rs.getInt("doctor_id"));

        Date date = rs.getDate("appointment_date");
        if (date != null) {
            apt.setAppointmentDate(date.toLocalDate());
        }

        Time time = rs.getTime("appointment_time");
        if (time != null) {
            apt.setAppointmentTime(time.toLocalTime());
        }

        apt.setStatus(rs.getString("status"));
        apt.setReason(rs.getString("reason"));

        // Joined fields
        apt.setPatientName(rs.getString("patient_name"));
        apt.setPatientPhone(rs.getString("patient_phone"));
        apt.setDoctorName(rs.getString("doctor_name"));
        apt.setDoctorSpecialization(rs.getString("doctor_specialization"));

        return apt;
    }
}

