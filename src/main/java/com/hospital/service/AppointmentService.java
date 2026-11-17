package com.hospital.service;

import com.hospital.dao.AppointmentDAO;
import com.hospital.dao.DoctorDAO;
import com.hospital.dao.PatientDAO;
import com.hospital.model.Appointment;
import com.hospital.model.Doctor;
import com.hospital.model.Patient;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service class enforcing business rules and validations for Appointments.
 */
public class AppointmentService {

    private final AppointmentDAO appointmentDAO;
    private final PatientDAO patientDAO;
    private final DoctorDAO doctorDAO;

    public AppointmentService() {
        this.appointmentDAO = new AppointmentDAO();
        this.patientDAO = new PatientDAO();
        this.doctorDAO = new DoctorDAO();
    }

    public AppointmentService(AppointmentDAO appointmentDAO, PatientDAO patientDAO, DoctorDAO doctorDAO) {
        this.appointmentDAO = appointmentDAO;
        this.patientDAO = patientDAO;
        this.doctorDAO = doctorDAO;
    }

    /**
     * Books a new appointment after verifying all business rules.
     *
     * @param appointment appointment entity
     * @return generated appointment ID
     * @throws SQLException on database error
     */
    public int bookAppointment(Appointment appointment) throws SQLException {
        validateAppointment(appointment, true);
        return appointmentDAO.insert(appointment);
    }

    /**
     * Updates an existing appointment after verifying business rules.
     *
     * @param appointment appointment entity
     * @throws SQLException on database error
     */
    public void updateAppointment(Appointment appointment) throws SQLException {
        if (appointment.getAppointmentId() <= 0) {
            throw new IllegalArgumentException("Invalid appointment ID for update.");
        }
        validateAppointment(appointment, false);
        boolean updated = appointmentDAO.update(appointment);
        if (!updated) {
            throw new IllegalArgumentException("Appointment #" + appointment.getAppointmentId() + " not found.");
        }
    }

    /**
     * Cancels an appointment by updating its status to 'CANCELLED',
     * preserving medical record history.
     *
     * @param appointmentId appointment ID
     * @throws SQLException on database error
     */
    public void cancelAppointment(int appointmentId) throws SQLException {
        if (appointmentId <= 0) {
            throw new IllegalArgumentException("Invalid appointment ID.");
        }

        Appointment existing = appointmentDAO.findById(appointmentId);
        if (existing == null) {
            throw new IllegalArgumentException("Appointment #" + appointmentId + " not found.");
        }

        if ("CANCELLED".equalsIgnoreCase(existing.getStatus())) {
            throw new IllegalStateException("Appointment #" + appointmentId + " is already cancelled.");
        }

        appointmentDAO.updateStatus(appointmentId, "CANCELLED");
    }

    /**
     * Marks an appointment as 'COMPLETED'.
     *
     * @param appointmentId appointment ID
     * @throws SQLException on database error
     */
    public void completeAppointment(int appointmentId) throws SQLException {
        if (appointmentId <= 0) {
            throw new IllegalArgumentException("Invalid appointment ID.");
        }

        Appointment existing = appointmentDAO.findById(appointmentId);
        if (existing == null) {
            throw new IllegalArgumentException("Appointment #" + appointmentId + " not found.");
        }

        if ("CANCELLED".equalsIgnoreCase(existing.getStatus())) {
            throw new IllegalStateException("Cannot complete a cancelled appointment.");
        }

        appointmentDAO.updateStatus(appointmentId, "COMPLETED");
    }

    public Appointment getAppointmentById(int appointmentId) throws SQLException {
        return appointmentDAO.findById(appointmentId);
    }

    public List<Appointment> getAllAppointments() throws SQLException {
        return appointmentDAO.findAll();
    }

    public List<Appointment> getTodayAppointments() throws SQLException {
        return appointmentDAO.findTodayAppointments();
    }

    public List<Appointment> getUpcomingAppointments(int limit) throws SQLException {
        return appointmentDAO.findUpcomingAppointments(limit);
    }

    public List<Appointment> filterAppointments(LocalDate date, String status, String searchQuery) throws SQLException {
        return appointmentDAO.filter(date, status, searchQuery);
    }

    public int getTotalAppointmentsCount() throws SQLException {
        return appointmentDAO.countTotalAppointments();
    }

    public int getTodayAppointmentsCount() throws SQLException {
        return appointmentDAO.countTodayAppointments();
    }

    /**
     * Validates business rules:
     * 1. Patient exists
     * 2. Doctor exists
     * 3. Appointment is not in the past
     * 4. Doctor has no conflicting non-cancelled appointment at the same slot
     * 5. Mandatory fields are present
     */
    private void validateAppointment(Appointment appointment, boolean isNew) throws SQLException {
        if (appointment == null) {
            throw new IllegalArgumentException("Appointment details cannot be empty.");
        }

        // Rule 1: Patient exists
        if (appointment.getPatientId() <= 0) {
            throw new IllegalArgumentException("Please select a valid patient.");
        }
        Patient patient = patientDAO.findById(appointment.getPatientId());
        if (patient == null) {
            throw new IllegalArgumentException("The selected patient (ID: " + appointment.getPatientId() + ") does not exist.");
        }

        // Rule 2: Doctor exists
        if (appointment.getDoctorId() <= 0) {
            throw new IllegalArgumentException("Please select a valid doctor.");
        }
        Doctor doctor = doctorDAO.findById(appointment.getDoctorId());
        if (doctor == null) {
            throw new IllegalArgumentException("The selected doctor (ID: " + appointment.getDoctorId() + ") does not exist.");
        }

        // Rule 5: Mandatory fields
        if (appointment.getAppointmentDate() == null) {
            throw new IllegalArgumentException("Appointment date is required.");
        }
        if (appointment.getAppointmentTime() == null) {
            throw new IllegalArgumentException("Appointment time is required.");
        }

        // Rule 4: Prevent booking in the past (unless existing record is completed/historical)
        if (isNew || "SCHEDULED".equalsIgnoreCase(appointment.getStatus())) {
            LocalDateTime appointmentDateTime = LocalDateTime.of(
                    appointment.getAppointmentDate(),
                    appointment.getAppointmentTime()
            );
            // Allow a small grace margin of 5 minutes for immediate appointments
            if (appointmentDateTime.isBefore(LocalDateTime.now().minusMinutes(5))) {
                throw new IllegalArgumentException("Cannot book or schedule an appointment in the past.");
            }
        }

        // Rule 3: Conflict detection (doctor cannot have two non-cancelled appointments at same time)
        if (!"CANCELLED".equalsIgnoreCase(appointment.getStatus())) {
            Integer excludeId = isNew ? null : appointment.getAppointmentId();
            boolean hasConflict = appointmentDAO.hasConflict(
                    appointment.getDoctorId(),
                    appointment.getAppointmentDate(),
                    appointment.getAppointmentTime(),
                    excludeId
            );
            if (hasConflict) {
                throw new IllegalStateException(
                        "Doctor 'Dr. " + doctor.getName() + "' already has a scheduled appointment on " +
                        appointment.getAppointmentDate() + " at " + appointment.getAppointmentTime() + ". " +
                        "Please choose a different time slot."
                );
            }
        }

        // Status validation
        String status = appointment.getStatus();
        if (status == null || status.isBlank()) {
            appointment.setStatus("SCHEDULED");
        } else if (!status.equalsIgnoreCase("SCHEDULED") &&
                   !status.equalsIgnoreCase("COMPLETED") &&
                   !status.equalsIgnoreCase("CANCELLED")) {
            throw new IllegalArgumentException("Invalid status: " + status + ". Allowed: SCHEDULED, COMPLETED, CANCELLED.");
        }
    }
}

