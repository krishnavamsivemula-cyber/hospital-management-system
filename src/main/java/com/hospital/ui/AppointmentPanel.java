package com.hospital.ui;

import com.hospital.model.Appointment;
import com.hospital.model.Doctor;
import com.hospital.model.Patient;
import com.hospital.service.AppointmentService;
import com.hospital.service.DoctorService;
import com.hospital.service.PatientService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Appointment Management Panel:
 * Supports booking, rescheduling, filtering by date/status,
 * completing, and cancelling appointments with conflict prevention.
 */
public class AppointmentPanel extends JPanel {

    private final AppointmentService appointmentService;
    private final PatientService patientService;
    private final DoctorService doctorService;
    private final Runnable onDataChanged;

    private DefaultTableModel tableModel;
    private JTable appointmentTable;

    private JTextField searchField;
    private JComboBox<String> statusFilterCombo;
    private JTextField dateFilterField;

    private List<Appointment> currentAppointments = new ArrayList<>();

    public AppointmentPanel(AppointmentService appointmentService,
                            PatientService patientService,
                            DoctorService doctorService,
                            Runnable onDataChanged) {
        this.appointmentService = appointmentService;
        this.patientService = patientService;
        this.doctorService = doctorService;
        this.onDataChanged = onDataChanged;

        setLayout(new BorderLayout());
        setBackground(UITheme.BG_LIGHT);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        initUI();
        loadAppointments();
    }

    private void initUI() {
        // --- Top Header ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, 16, 0));

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);

        JLabel titleLabel = new JLabel("Appointment Management");
        titleLabel.setFont(UITheme.FONT_TITLE);
        titleLabel.setForeground(UITheme.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Schedule consultations, manage calendar slots, and track visit status");
        subtitleLabel.setFont(UITheme.FONT_BODY);
        subtitleLabel.setForeground(UITheme.TEXT_SECONDARY);

        titleBlock.add(titleLabel);
        titleBlock.add(Box.createVerticalStrut(4));
        titleBlock.add(subtitleLabel);

        JButton bookBtn = UITheme.createPrimaryButton("➕ Book Appointment");
        bookBtn.addActionListener(e -> showAppointmentDialog(null));

        headerPanel.add(titleBlock, BorderLayout.WEST);
        headerPanel.add(bookBtn, BorderLayout.EAST);

        // --- Controls Panel (Filters & Actions) ---
        JPanel controlsCard = new JPanel(new BorderLayout(16, 0));
        controlsCard.setBackground(UITheme.CARD_BG);
        controlsCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                new EmptyBorder(12, 16, 12, 16)
        ));

        // Filter Controls
        JPanel filtersPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filtersPanel.setOpaque(false);

        JLabel searchIcon = new JLabel("🔍");
        searchField = UITheme.createTextField(14);
        searchField.putClientProperty("JTextField.placeholderText", "Search name or reason...");

        JLabel statusLabel = new JLabel("Status:");
        statusLabel.setFont(UITheme.FONT_BODY);
        statusFilterCombo = new JComboBox<>(new String[]{"ALL", "SCHEDULED", "COMPLETED", "CANCELLED"});
        statusFilterCombo.setFont(UITheme.FONT_BODY);

        JLabel dateLabel = new JLabel("Date:");
        dateLabel.setFont(UITheme.FONT_BODY);
        dateFilterField = UITheme.createTextField(9);
        dateFilterField.setToolTipText("YYYY-MM-DD or leave blank for all dates");

        JButton todayBtn = UITheme.createSecondaryButton("Today");
        todayBtn.addActionListener(e -> {
            dateFilterField.setText(LocalDate.now().toString());
            loadAppointments();
        });

        JButton applyFilterBtn = UITheme.createSecondaryButton("Filter");
        applyFilterBtn.addActionListener(e -> loadAppointments());

        JButton clearBtn = UITheme.createSecondaryButton("Clear");
        clearBtn.addActionListener(e -> {
            searchField.setText("");
            statusFilterCombo.setSelectedIndex(0);
            dateFilterField.setText("");
            loadAppointments();
        });

        filtersPanel.add(searchIcon);
        filtersPanel.add(searchField);
        filtersPanel.add(statusLabel);
        filtersPanel.add(statusFilterCombo);
        filtersPanel.add(dateLabel);
        filtersPanel.add(dateFilterField);
        filtersPanel.add(todayBtn);
        filtersPanel.add(applyFilterBtn);
        filtersPanel.add(clearBtn);

        // Action Buttons for selected row
        JPanel tableActionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        tableActionsPanel.setOpaque(false);

        JButton editBtn = UITheme.createSecondaryButton("✏️ Edit");
        editBtn.addActionListener(e -> onEditSelected());

        JButton completeBtn = UITheme.createSecondaryButton("✅ Complete");
        completeBtn.addActionListener(e -> onCompleteSelected());

        JButton cancelBtn = UITheme.createDangerButton("🚫 Cancel");
        cancelBtn.addActionListener(e -> onCancelSelected());

        tableActionsPanel.add(editBtn);
        tableActionsPanel.add(completeBtn);
        tableActionsPanel.add(cancelBtn);

        controlsCard.add(filtersPanel, BorderLayout.WEST);
        controlsCard.add(tableActionsPanel, BorderLayout.EAST);

        // --- Table Panel ---
        String[] cols = {"ID", "Date", "Time", "Patient", "Doctor", "Specialization", "Status", "Reason"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        appointmentTable = new JTable(tableModel);
        UITheme.styleTable(appointmentTable);

        appointmentTable.getColumnModel().getColumn(0).setMaxWidth(60);
        appointmentTable.getColumnModel().getColumn(1).setPreferredWidth(95);
        appointmentTable.getColumnModel().getColumn(2).setPreferredWidth(75);
        appointmentTable.getColumnModel().getColumn(6).setPreferredWidth(110);

        applyStatusBadgeRenderer(appointmentTable, 6);

        JScrollPane scrollPane = new JScrollPane(appointmentTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        scrollPane.getViewport().setBackground(Color.WHITE);

        JPanel centerContainer = new JPanel(new BorderLayout(0, 16));
        centerContainer.setOpaque(false);
        centerContainer.add(controlsCard, BorderLayout.NORTH);
        centerContainer.add(scrollPane, BorderLayout.CENTER);

        add(headerPanel, BorderLayout.NORTH);
        add(centerContainer, BorderLayout.CENTER);
    }

    private void applyStatusBadgeRenderer(JTable table, int statusColIndex) {
        table.getColumnModel().getColumn(statusColIndex).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
                String status = (value != null) ? value.toString() : "";
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(UITheme.FONT_BOLD);

                if (!isSelected) {
                    if ("SCHEDULED".equalsIgnoreCase(status)) {
                        c.setForeground(UITheme.WARNING);
                    } else if ("COMPLETED".equalsIgnoreCase(status)) {
                        c.setForeground(UITheme.SUCCESS);
                    } else if ("CANCELLED".equalsIgnoreCase(status)) {
                        c.setForeground(UITheme.DANGER);
                    } else {
                        c.setForeground(UITheme.TEXT_SECONDARY);
                    }
                }
                return c;
            }
        });
    }

    /**
     * Loads filtered appointments via SwingWorker.
     */
    public void loadAppointments() {
        String query = searchField.getText().trim();
        String status = (String) statusFilterCombo.getSelectedItem();
        String dateStr = dateFilterField.getText().trim();

        LocalDate filterDate = null;
        if (!dateStr.isEmpty()) {
            try {
                filterDate = LocalDate.parse(dateStr);
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this,
                        "Invalid date format: '" + dateStr + "'. Please use YYYY-MM-DD.",
                        "Filter Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        final LocalDate finalDate = filterDate;
        SwingWorker<List<Appointment>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Appointment> doInBackground() throws Exception {
                return appointmentService.filterAppointments(finalDate, status, query);
            }

            @Override
            protected void done() {
                try {
                    currentAppointments = get();
                    tableModel.setRowCount(0);
                    for (Appointment a : currentAppointments) {
                        tableModel.addRow(new Object[]{
                                a.getAppointmentId(),
                                a.getAppointmentDate() != null ? a.getAppointmentDate().toString() : "",
                                a.getAppointmentTime() != null ? a.getAppointmentTime().toString() : "",
                                a.getPatientName() != null ? a.getPatientName() : "ID #" + a.getPatientId(),
                                a.getDoctorName() != null ? "Dr. " + a.getDoctorName() : "ID #" + a.getDoctorId(),
                                a.getDoctorSpecialization() != null ? a.getDoctorSpecialization() : "",
                                a.getStatus(),
                                a.getReason() != null ? a.getReason() : ""
                        });
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(AppointmentPanel.this,
                            "Failed to load appointments: " + ex.getMessage(),
                            "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void onEditSelected() {
        int row = appointmentTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select an appointment from the table to edit.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Appointment selected = currentAppointments.get(row);
        showAppointmentDialog(selected);
    }

    private void onCompleteSelected() {
        int row = appointmentTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select an appointment to mark as completed.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Appointment selected = currentAppointments.get(row);
        if ("COMPLETED".equalsIgnoreCase(selected.getStatus())) {
            JOptionPane.showMessageDialog(this,
                    "This appointment is already marked as COMPLETED.",
                    "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Mark Appointment #" + selected.getAppointmentId() + " as COMPLETED?",
                "Complete Appointment",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    appointmentService.completeAppointment(selected.getAppointmentId());
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        JOptionPane.showMessageDialog(AppointmentPanel.this,
                                "Appointment marked as completed.",
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                        loadAppointments();
                        if (onDataChanged != null) {
                            onDataChanged.run();
                        }
                    } catch (Exception ex) {
                        String msg = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
                        JOptionPane.showMessageDialog(AppointmentPanel.this,
                                msg, "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            worker.execute();
        }
    }

    private void onCancelSelected() {
        int row = appointmentTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select an appointment to cancel.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Appointment selected = currentAppointments.get(row);
        if ("CANCELLED".equalsIgnoreCase(selected.getStatus())) {
            JOptionPane.showMessageDialog(this,
                    "This appointment is already CANCELLED.",
                    "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to CANCEL Appointment #" + selected.getAppointmentId() + "?\n\n" +
                "Note: The historical record will be preserved with 'CANCELLED' status.",
                "Confirm Cancellation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    appointmentService.cancelAppointment(selected.getAppointmentId());
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        JOptionPane.showMessageDialog(AppointmentPanel.this,
                                "Appointment successfully cancelled.",
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                        loadAppointments();
                        if (onDataChanged != null) {
                            onDataChanged.run();
                        }
                    } catch (Exception ex) {
                        String msg = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
                        JOptionPane.showMessageDialog(AppointmentPanel.this,
                                msg, "Cancellation Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            worker.execute();
        }
    }

    /**
     * Shows modal Add or Edit Appointment dialog with populated patient and doctor dropdowns.
     */
    private void showAppointmentDialog(Appointment existingAppointment) {
        boolean isEdit = (existingAppointment != null);
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                isEdit ? "Edit Appointment Details" : "Book New Appointment", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(520, 520);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        // Fetch patients and doctors for combo boxes
        List<Patient> patients;
        List<Doctor> doctors;
        try {
            patients = patientService.getAllPatients();
            doctors = doctorService.getAllDoctors();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Unable to load patient or doctor lists: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (patients.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No patients registered yet. Please register a patient before booking an appointment.",
                    "No Patients Found", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (doctors.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No doctors registered yet. Please add a doctor before booking an appointment.",
                    "No Doctors Found", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(24, 28, 24, 28));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 4, 6, 4);

        // Patient ComboBox
        JComboBox<PatientItem> patientCombo = new JComboBox<>();
        PatientItem selectedPatientItem = null;
        for (Patient p : patients) {
            PatientItem item = new PatientItem(p);
            patientCombo.addItem(item);
            if (isEdit && p.getPatientId() == existingAppointment.getPatientId()) {
                selectedPatientItem = item;
            }
        }
        if (selectedPatientItem != null) {
            patientCombo.setSelectedItem(selectedPatientItem);
        }

        // Doctor ComboBox
        JComboBox<DoctorItem> doctorCombo = new JComboBox<>();
        DoctorItem selectedDoctorItem = null;
        for (Doctor d : doctors) {
            DoctorItem item = new DoctorItem(d);
            doctorCombo.addItem(item);
            if (isEdit && d.getDoctorId() == existingAppointment.getDoctorId()) {
                selectedDoctorItem = item;
            }
        }
        if (selectedDoctorItem != null) {
            doctorCombo.setSelectedItem(selectedDoctorItem);
        }

        // Date input with helper buttons
        JTextField dateField = UITheme.createTextField(12);
        LocalDate initialDate = isEdit ? existingAppointment.getAppointmentDate() : LocalDate.now();
        dateField.setText(initialDate != null ? initialDate.toString() : LocalDate.now().toString());

        JPanel dateInputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        dateInputPanel.setOpaque(false);
        dateInputPanel.add(dateField);

        JButton dateTodayBtn = UITheme.createSecondaryButton("Today");
        dateTodayBtn.addActionListener(e -> dateField.setText(LocalDate.now().toString()));

        JButton dateTomorrowBtn = UITheme.createSecondaryButton("Tomorrow");
        dateTomorrowBtn.addActionListener(e -> dateField.setText(LocalDate.now().plusDays(1).toString()));

        dateInputPanel.add(dateTodayBtn);
        dateInputPanel.add(dateTomorrowBtn);

        // Time ComboBox (standard 30-min intervals)
        String[] timeSlots = {
                "09:00", "09:30", "10:00", "10:30", "11:00", "11:30",
                "12:00", "14:00", "14:30", "15:00", "15:30", "16:00", "16:30", "17:00"
        };
        JComboBox<String> timeCombo = new JComboBox<>(timeSlots);
        timeCombo.setEditable(true);
        if (isEdit && existingAppointment.getAppointmentTime() != null) {
            timeCombo.setSelectedItem(existingAppointment.getAppointmentTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        }

        // Status ComboBox
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"SCHEDULED", "COMPLETED", "CANCELLED"});
        if (isEdit && existingAppointment.getStatus() != null) {
            statusCombo.setSelectedItem(existingAppointment.getStatus());
        }

        // Reason field
        JTextField reasonField = UITheme.createTextField(20);
        if (isEdit && existingAppointment.getReason() != null) {
            reasonField.setText(existingAppointment.getReason());
        }

        int row = 0;
        addFormRow(formPanel, gbc, row++, "Select Patient *:", patientCombo);
        addFormRow(formPanel, gbc, row++, "Select Doctor *:", doctorCombo);
        addFormRow(formPanel, gbc, row++, "Date (YYYY-MM-DD) *:", dateInputPanel);
        addFormRow(formPanel, gbc, row++, "Time Slot *:", timeCombo);
        if (isEdit) {
            addFormRow(formPanel, gbc, row++, "Status *:", statusCombo);
        }
        addFormRow(formPanel, gbc, row++, "Reason for Visit:", reasonField);

        // Dialog Footer Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 14));
        buttonPanel.setBackground(new Color(248, 250, 252));
        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER_COLOR));

        JButton cancelDialogBtn = UITheme.createSecondaryButton("Cancel");
        cancelDialogBtn.addActionListener(e -> dialog.dispose());

        JButton saveBtn = UITheme.createPrimaryButton(isEdit ? "Update Appointment" : "Confirm Booking");
        saveBtn.addActionListener(e -> {
            PatientItem patientItem = (PatientItem) patientCombo.getSelectedItem();
            DoctorItem doctorItem = (DoctorItem) doctorCombo.getSelectedItem();
            String dateText = dateField.getText().trim();
            Object timeObj = timeCombo.getSelectedItem();
            String timeText = (timeObj != null) ? timeObj.toString().trim() : "";
            String status = isEdit ? (String) statusCombo.getSelectedItem() : "SCHEDULED";
            String reason = reasonField.getText().trim();

            if (patientItem == null || doctorItem == null) {
                JOptionPane.showMessageDialog(dialog, "Please select both a patient and a doctor.",
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            LocalDate appointmentDate;
            try {
                appointmentDate = LocalDate.parse(dateText);
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid date format. Use YYYY-MM-DD.",
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            LocalTime appointmentTime;
            try {
                if (timeText.length() == 5) {
                    appointmentTime = LocalTime.parse(timeText + ":00");
                } else {
                    appointmentTime = LocalTime.parse(timeText);
                }
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid time format. Use HH:mm (e.g. 10:30).",
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                if (isEdit) {
                    existingAppointment.setPatientId(patientItem.patient.getPatientId());
                    existingAppointment.setDoctorId(doctorItem.doctor.getDoctorId());
                    existingAppointment.setAppointmentDate(appointmentDate);
                    existingAppointment.setAppointmentTime(appointmentTime);
                    existingAppointment.setStatus(status);
                    existingAppointment.setReason(reason);
                    appointmentService.updateAppointment(existingAppointment);
                } else {
                    Appointment newApt = new Appointment(
                            patientItem.patient.getPatientId(),
                            doctorItem.doctor.getDoctorId(),
                            appointmentDate,
                            appointmentTime,
                            status,
                            reason
                    );
                    appointmentService.bookAppointment(newApt);
                }

                dialog.dispose();
                JOptionPane.showMessageDialog(this,
                        isEdit ? "Appointment updated successfully." : "Appointment booked successfully.",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                loadAppointments();
                if (onDataChanged != null) {
                    onDataChanged.run();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog,
                        ex.getMessage(),
                        "Booking Conflict / Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        buttonPanel.add(cancelDialogBtn);
        buttonPanel.add(saveBtn);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, JComponent field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.35;
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(UITheme.FONT_BOLD);
        lbl.setForeground(UITheme.TEXT_PRIMARY);
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        panel.add(field, gbc);
    }

    // Helper wrapper classes for JComboBox display
    private static class PatientItem {
        final Patient patient;

        PatientItem(Patient patient) {
            this.patient = patient;
        }

        @Override
        public String toString() {
            return patient.getDisplayLabel();
        }
    }

    private static class DoctorItem {
        final Doctor doctor;

        DoctorItem(Doctor doctor) {
            this.doctor = doctor;
        }

        @Override
        public String toString() {
            return doctor.getDisplayLabel();
        }
    }
}

