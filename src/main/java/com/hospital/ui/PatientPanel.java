package com.hospital.ui;

import com.hospital.model.Patient;
import com.hospital.service.PatientService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Patient Management Panel:
 * Allows searching, listing, registering, editing, and safely deleting patients.
 */
public class PatientPanel extends JPanel {

    private final PatientService patientService;
    private final Runnable onDataChanged;

    private DefaultTableModel tableModel;
    private JTable patientTable;
    private JTextField searchField;
    private List<Patient> currentPatients = new ArrayList<>();

    public PatientPanel(PatientService patientService, Runnable onDataChanged) {
        this.patientService = patientService;
        this.onDataChanged = onDataChanged;

        setLayout(new BorderLayout());
        setBackground(UITheme.BG_LIGHT);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        initUI();
        loadPatients(null);
    }

    private void initUI() {
        // --- Top Header ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, 16, 0));

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);

        JLabel titleLabel = new JLabel("Patient Management");
        titleLabel.setFont(UITheme.FONT_TITLE);
        titleLabel.setForeground(UITheme.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Register and manage patient records and contact details");
        subtitleLabel.setFont(UITheme.FONT_BODY);
        subtitleLabel.setForeground(UITheme.TEXT_SECONDARY);

        titleBlock.add(titleLabel);
        titleBlock.add(Box.createVerticalStrut(4));
        titleBlock.add(subtitleLabel);

        JButton addPatientBtn = UITheme.createPrimaryButton("➕ Register Patient");
        addPatientBtn.addActionListener(e -> showPatientDialog(null));

        headerPanel.add(titleBlock, BorderLayout.WEST);
        headerPanel.add(addPatientBtn, BorderLayout.EAST);

        // --- Controls Panel (Search & Actions) ---
        JPanel controlsCard = new JPanel(new BorderLayout(16, 0));
        controlsCard.setBackground(UITheme.CARD_BG);
        controlsCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                new EmptyBorder(12, 16, 12, 16)
        ));

        // Search bar
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchPanel.setOpaque(false);

        JLabel searchIcon = new JLabel("🔍");
        searchField = UITheme.createTextField(22);
        searchField.putClientProperty("JTextField.placeholderText", "Search by ID or name...");

        JButton searchBtn = UITheme.createSecondaryButton("Search");
        searchBtn.addActionListener(e -> loadPatients(searchField.getText()));
        searchField.addActionListener(e -> loadPatients(searchField.getText()));

        JButton clearBtn = UITheme.createSecondaryButton("Clear");
        clearBtn.addActionListener(e -> {
            searchField.setText("");
            loadPatients(null);
        });

        searchPanel.add(searchIcon);
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);
        searchPanel.add(clearBtn);

        // Action Buttons for selected row
        JPanel tableActionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        tableActionsPanel.setOpaque(false);

        JButton editBtn = UITheme.createSecondaryButton("✏️ Edit Selected");
        editBtn.addActionListener(e -> onEditSelected());

        JButton deleteBtn = UITheme.createDangerButton("🗑️ Delete Selected");
        deleteBtn.addActionListener(e -> onDeleteSelected());

        tableActionsPanel.add(editBtn);
        tableActionsPanel.add(deleteBtn);

        controlsCard.add(searchPanel, BorderLayout.WEST);
        controlsCard.add(tableActionsPanel, BorderLayout.EAST);

        // --- Table Panel ---
        String[] cols = {"Patient ID", "Name", "Age", "Gender", "Phone Number"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        patientTable = new JTable(tableModel);
        UITheme.styleTable(patientTable);

        // Center align ID, Age, Gender
        patientTable.getColumnModel().getColumn(0).setMaxWidth(100);
        patientTable.getColumnModel().getColumn(2).setMaxWidth(80);
        patientTable.getColumnModel().getColumn(3).setMaxWidth(110);

        JScrollPane scrollPane = new JScrollPane(patientTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        scrollPane.getViewport().setBackground(Color.WHITE);

        // Combine into main layout
        JPanel centerContainer = new JPanel(new BorderLayout(0, 16));
        centerContainer.setOpaque(false);
        centerContainer.add(controlsCard, BorderLayout.NORTH);
        centerContainer.add(scrollPane, BorderLayout.CENTER);

        add(headerPanel, BorderLayout.NORTH);
        add(centerContainer, BorderLayout.CENTER);
    }

    /**
     * Loads patients from database using SwingWorker.
     */
    public void loadPatients(String searchQuery) {
        SwingWorker<List<Patient>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Patient> doInBackground() throws Exception {
                return patientService.searchPatients(searchQuery);
            }

            @Override
            protected void done() {
                try {
                    currentPatients = get();
                    tableModel.setRowCount(0);
                    for (Patient p : currentPatients) {
                        tableModel.addRow(new Object[]{
                                p.getPatientId(),
                                p.getName(),
                                p.getAge(),
                                p.getGender(),
                                p.getPhone() != null ? p.getPhone() : ""
                        });
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(PatientPanel.this,
                            "Failed to load patients: " + ex.getMessage(),
                            "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void onEditSelected() {
        int selectedRow = patientTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select a patient from the table to edit.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Patient selected = currentPatients.get(selectedRow);
        showPatientDialog(selected);
    }

    private void onDeleteSelected() {
        int selectedRow = patientTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select a patient from the table to delete.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Patient selected = currentPatients.get(selectedRow);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete patient:\n" +
                "ID #" + selected.getPatientId() + " - " + selected.getName() + "?\n\n" +
                "This action cannot be undone.",
                "Confirm Patient Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    patientService.deletePatient(selected.getPatientId());
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        JOptionPane.showMessageDialog(PatientPanel.this,
                                "Patient successfully deleted.",
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                        loadPatients(searchField.getText());
                        if (onDataChanged != null) {
                            onDataChanged.run();
                        }
                    } catch (Exception ex) {
                        String msg = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
                        JOptionPane.showMessageDialog(PatientPanel.this,
                                msg, "Cannot Delete Patient", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            worker.execute();
        }
    }

    /**
     * Opens Add or Edit Patient modal dialog.
     */
    private void showPatientDialog(Patient existingPatient) {
        boolean isEdit = (existingPatient != null);
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                isEdit ? "Edit Patient Details" : "Register New Patient", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(440, 380);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(24, 28, 24, 28));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 4, 6, 4);

        // Fields
        JTextField nameField = UITheme.createTextField(20);
        JSpinner ageSpinner = new JSpinner(new SpinnerNumberModel(
                isEdit ? existingPatient.getAge() : 30, 0, 130, 1));
        JComboBox<String> genderCombo = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        JTextField phoneField = UITheme.createTextField(20);

        if (isEdit) {
            nameField.setText(existingPatient.getName());
            genderCombo.setSelectedItem(existingPatient.getGender());
            phoneField.setText(existingPatient.getPhone());
        }

        int row = 0;
        addFormRow(formPanel, gbc, row++, "Full Name *:", nameField);
        addFormRow(formPanel, gbc, row++, "Age *:", ageSpinner);
        addFormRow(formPanel, gbc, row++, "Gender *:", genderCombo);
        addFormRow(formPanel, gbc, row++, "Phone Number:", phoneField);

        // Dialog Footer Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 14));
        buttonPanel.setBackground(new Color(248, 250, 252));
        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER_COLOR));

        JButton cancelBtn = UITheme.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton saveBtn = UITheme.createPrimaryButton(isEdit ? "Update Patient" : "Save Patient");
        saveBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            int age = (Integer) ageSpinner.getValue();
            String gender = (String) genderCombo.getSelectedItem();
            String phone = phoneField.getText().trim();

            try {
                if (isEdit) {
                    existingPatient.setName(name);
                    existingPatient.setAge(age);
                    existingPatient.setGender(gender);
                    existingPatient.setPhone(phone);
                    patientService.updatePatient(existingPatient);
                } else {
                    Patient newP = new Patient(name, age, gender, phone);
                    patientService.registerPatient(newP);
                }

                dialog.dispose();
                JOptionPane.showMessageDialog(this,
                        isEdit ? "Patient updated successfully." : "Patient registered successfully.",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                loadPatients(searchField.getText());
                if (onDataChanged != null) {
                    onDataChanged.run();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog,
                        ex.getMessage(),
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, JComponent field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(UITheme.FONT_BOLD);
        lbl.setForeground(UITheme.TEXT_PRIMARY);
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        panel.add(field, gbc);
    }
}

