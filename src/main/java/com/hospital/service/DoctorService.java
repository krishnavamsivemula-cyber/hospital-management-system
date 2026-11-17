package com.hospital.service;

import com.hospital.dao.DoctorDAO;
import com.hospital.model.Doctor;

import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Service class handling business logic and validations for Doctor management.
 */
public class DoctorService {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$");

    private final DoctorDAO doctorDAO;

    public DoctorService() {
        this.doctorDAO = new DoctorDAO();
    }

    public DoctorService(DoctorDAO doctorDAO) {
        this.doctorDAO = doctorDAO;
    }

    /**
     * Adds a new doctor after validating input.
     */
    public int addDoctor(Doctor doctor) throws SQLException {
        validateDoctor(doctor);
        return doctorDAO.insert(doctor);
    }

    /**
     * Updates an existing doctor after validating input.
     */
    public void updateDoctor(Doctor doctor) throws SQLException {
        if (doctor.getDoctorId() <= 0) {
            throw new IllegalArgumentException("Invalid doctor ID for update.");
        }
        validateDoctor(doctor);
        boolean updated = doctorDAO.update(doctor);
        if (!updated) {
            throw new IllegalArgumentException("Doctor with ID " + doctor.getDoctorId() + " not found.");
        }
    }

    /**
     * Deletes a doctor if they do not have existing appointments.
     */
    public void deleteDoctor(int doctorId) throws SQLException {
        if (doctorId <= 0) {
            throw new IllegalArgumentException("Invalid doctor ID.");
        }

        Doctor existing = doctorDAO.findById(doctorId);
        if (existing == null) {
            throw new IllegalArgumentException("Doctor with ID " + doctorId + " does not exist.");
        }

        if (doctorDAO.hasAppointments(doctorId)) {
            throw new IllegalStateException(
                    "Cannot delete doctor 'Dr. " + existing.getName() + "' (ID: " + doctorId + "): " +
                    "They have associated appointment records. Cancel or reassign appointments first."
            );
        }

        doctorDAO.delete(doctorId);
    }

    /**
     * Retrieves a doctor by ID.
     */
    public Doctor getDoctorById(int doctorId) throws SQLException {
        return doctorDAO.findById(doctorId);
    }

    /**
     * Retrieves all doctors.
     */
    public List<Doctor> getAllDoctors() throws SQLException {
        return doctorDAO.findAll();
    }

    /**
     * Searches doctors by name or specialization.
     *
     * @param query search text
     * @param searchType "Name" or "Specialization"
     * @return matching doctors
     */
    public List<Doctor> searchDoctors(String query, String searchType) throws SQLException {
        if (query == null || query.isBlank()) {
            return getAllDoctors();
        }

        String trimmed = query.trim();
        if ("Specialization".equalsIgnoreCase(searchType)) {
            return doctorDAO.searchBySpecialization(trimmed);
        } else {
            // Default to search by name (or ID if purely numeric)
            if (trimmed.matches("\\d+")) {
                Doctor doc = doctorDAO.findById(Integer.parseInt(trimmed));
                return (doc != null) ? List.of(doc) : List.of();
            }
            return doctorDAO.searchByName(trimmed);
        }
    }

    /**
     * Returns total count of registered doctors.
     */
    public int getTotalDoctorsCount() throws SQLException {
        return doctorDAO.countDoctors();
    }

    /**
     * Validates doctor fields according to hospital rules.
     */
    private void validateDoctor(Doctor doctor) {
        if (doctor == null) {
            throw new IllegalArgumentException("Doctor information cannot be null.");
        }

        if (doctor.getName() == null || doctor.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Doctor name is mandatory.");
        }

        if (doctor.getName().trim().length() < 2) {
            throw new IllegalArgumentException("Doctor name must be at least 2 characters.");
        }

        if (doctor.getSpecialization() == null || doctor.getSpecialization().trim().isEmpty()) {
            throw new IllegalArgumentException("Doctor specialization is mandatory.");
        }

        String phone = doctor.getPhone();
        if (phone != null && !phone.isBlank()) {
            String digitsOnly = phone.replaceAll("[^0-9]", "");
            if (digitsOnly.length() < 7 || digitsOnly.length() > 15) {
                throw new IllegalArgumentException("Phone number must contain between 7 and 15 digits.");
            }
        }

        String email = doctor.getEmail();
        if (email != null && !email.isBlank()) {
            if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
                throw new IllegalArgumentException("Please enter a valid email address (e.g. doctor@hospital.com).");
            }
        }
    }
}

